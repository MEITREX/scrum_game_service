package de.unistuttgart.iste.meitrex.scrumgame.api.project;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.generated.dto.CreateProjectInput;
import de.unistuttgart.iste.meitrex.generated.dto.GlobalPrivilege;
import de.unistuttgart.iste.meitrex.generated.dto.Project;
import de.unistuttgart.iste.meitrex.generated.dto.ProjectPrivilege;
import de.unistuttgart.iste.meitrex.scrumgame.data.SampleGlobalUsers;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project.ProjectEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.user.GlobalUserEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.user.UserInProjectEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.user.UserProjectId;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.GlobalUserRepository;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.ProjectRepository;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.SprintRepository;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.UserInProjectRepository;
import de.unistuttgart.iste.meitrex.scrumgame.service.auth.AuthService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

import static de.unistuttgart.iste.meitrex.common.testutil.MeitrexMatchers.causedBy;
import static de.unistuttgart.iste.meitrex.common.testutil.MeitrexMatchers.containsError;
import static de.unistuttgart.iste.meitrex.common.util.GraphQlUtil.gql;
import static de.unistuttgart.iste.meitrex.scrumgame.data.SampleProjects.getSampleCreateProjectInput;
import static de.unistuttgart.iste.meitrex.scrumgame.fragments.ProjectFragments.BASE_PROJECT_FRAGMENT;
import static de.unistuttgart.iste.meitrex.scrumgame.matchers.ProjectMatcher.matchingProjectInput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@GraphQlApiTest
class MutationCreateProjectTest {

    @Autowired
    private ProjectRepository    projectRepository;
    @Autowired
    private UserInProjectRepository userInProjectRepository;
    @Autowired
    private GlobalUserRepository globalUserRepository;
    @Autowired
    private SprintRepository     sprintRepository;
    @MockBean
    private AuthService          authService;

    @Test
    @Transactional
    @Commit
    void testCreateProject(GraphQlTester graphQlTester) {
        // arrange
        GlobalUserEntity user = createUser();
        when(authService.hasPrivilege(GlobalPrivilege.CREATE_PROJECT)).thenReturn(true);
        when(authService.getCurrentUserId()).thenReturn(user.getId());

        CreateProjectInput input = getSampleCreateProjectInput();
        String mutation = getCreateProjectMutation();

        // act
        Project result = graphQlTester.document(mutation)
                .variable("input", input)
                .execute()
                .path("createProject")
                .entity(Project.class)
                .get();

        // assert
        assertThat(result, matchingProjectInput(input));

        ProjectEntity projectEntity = projectRepository.findById(result.getId()).orElseThrow();
        assertThat(projectEntity, matchingProjectInput(input));

        // verify
        verify(authService, atLeastOnce()).hasPrivilege(GlobalPrivilege.CREATE_PROJECT);
        verify(authService, atLeastOnce()).getCurrentUserId();
    }

    @Test
    void testCreateProjectNotAuthorized(GraphQlTester graphQlTester) {
        // arrange
        GlobalUserEntity user = createUser();
        when(authService.hasPrivilege(GlobalPrivilege.CREATE_PROJECT)).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(user.getId());

        String mutation = getCreateProjectMutation();
        CreateProjectInput input = getSampleCreateProjectInput();

        // act & assert
        graphQlTester.document(mutation)
                .variable("input", input)
                .execute()
                .errors()
                .satisfy(errors ->
                        assertThat(errors, containsError(causedBy(AuthorizationDeniedException.class))));
    }

    @Test
    @Transactional
    @Commit
    void testCreateProjectCorrectInitialized(GraphQlTester graphQlTester) {
        // arrange
        GlobalUserEntity user = createUser();
        when(authService.hasPrivilege(GlobalPrivilege.CREATE_PROJECT)).thenReturn(true);
        when(authService.getCurrentUserId()).thenReturn(user.getId());

        String mutation = getCreateProjectMutation();
        CreateProjectInput input = getSampleCreateProjectInput();
        input.setStartingSprintNumber(4);

        // act
        graphQlTester.document(mutation).variable("input", input).executeAndVerify();

        // assert
        ProjectEntity projectEntity = projectRepository.findAll().getFirst();
        UserInProjectEntity userInProjectEntity
                = userInProjectRepository.findByIdOrThrow(new UserProjectId(user.getId(), projectEntity.getId()));

        // check that user has admin role in his created project
        assertThat(userInProjectEntity.getRoles(), hasSize(1));
        assertThat(userInProjectEntity.getRoles().getFirst().getProjectPrivileges(),
                containsInAnyOrder(ProjectPrivilege.values()));

        // assert that sprints were created
        assertThat(sprintRepository.findAllByProjectIdOrderByNumber(projectEntity.getId()), hasSize(3));

        // verify
        verify(authService, atLeastOnce()).hasPrivilege(GlobalPrivilege.CREATE_PROJECT);
        verify(authService, atLeastOnce()).getCurrentUserId();
    }

    private GlobalUserEntity createUser() {
        return globalUserRepository.save(SampleGlobalUsers.sampleGlobalUserEntityBuilder().build());
    }

    private String getCreateProjectMutation() {
        return gql("""
                           mutation($input: CreateProjectInput!) {
                               createProject(input: $input) {
                                   ...ProjectFragment
                               }
                           }""" + BASE_PROJECT_FRAGMENT);
    }
}
