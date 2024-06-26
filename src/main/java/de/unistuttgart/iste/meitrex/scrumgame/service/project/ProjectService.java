package de.unistuttgart.iste.meitrex.scrumgame.service.project;

import de.unistuttgart.iste.meitrex.common.exception.MeitrexNotFoundException;
import de.unistuttgart.iste.meitrex.common.service.AbstractCrudService;
import de.unistuttgart.iste.meitrex.generated.dto.CreateProjectInput;
import de.unistuttgart.iste.meitrex.generated.dto.Project;
import de.unistuttgart.iste.meitrex.generated.dto.ProjectMutation;
import de.unistuttgart.iste.meitrex.generated.dto.UpdateProjectInput;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project.ProjectEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.mapper.ProjectMapping;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing projects.
 *
 * @see ProjectMapping project mapping details
 */
@Service
@Slf4j
public class ProjectService extends AbstractCrudService<UUID, ProjectEntity, Project> {

    private final ProjectInitializerService projectInitializerService;
    private final ProjectRepository projectRepository;

    public ProjectService(
            ProjectRepository repository,
            ModelMapper modelMapper,
            ProjectInitializerService projectInitializerService) {
        super(repository, modelMapper, ProjectEntity.class, Project.class);
        this.projectInitializerService = projectInitializerService;
        this.projectRepository = repository;
    }

    /**
     * Get all projects.
     *
     * @return a list of all projects.
     */
    public List<Project> getAllProjects() {
        return getAll();
    }

    /**
     * @param id the id of the project to get.
     * @return the project with the given id, or an empty optional if
     * no project with the given id exists.
     */
    public Optional<Project> findProject(UUID id) {
        return find(id);
    }

    /**
     * @param projectId the id of the project to get.
     * @return the project with the given id.
     * @throws MeitrexNotFoundException if no project with the given id exists.
     */
    public Project getProjectOrThrow(UUID projectId) {
        return getOrThrow(projectId);
    }

    /**
     * Create a new project.
     * AUTHORIZATION: Requires the global CREATE_PROJECT privilege.
     *
     * @param input the input data for the new project.
     * @return the created project.
     */
    @PreAuthorize("@auth.hasPrivilege(@globalPrivileges.CREATE_PROJECT)")
    public Project createProject(CreateProjectInput input) {
        ProjectEntity projectEntity = createEntity(input);

        projectInitializerService.initializeNewProject(projectEntity);

        return convertToDto(projectEntity);
    }

    public ProjectEntity getProjectEntity(UUID projectId) {
        return getRepository().findByIdOrThrow(projectId);
    }

    /**
     * Update a project.
     * AUTHORIZATION: Requires the UPDATE_PROJECT privilege for the project.
     *
     * @param projectId the id of the project to update.
     * @param input     the input data for the update.
     * @return the updated project.
     */
    @PreAuthorize("@auth.hasPrivilege(@projectPrivileges.UPDATE_PROJECT, #projectId)")
    public Project updateProject(UUID projectId, UpdateProjectInput input) {
        return update(projectId, input);
    }

    @PreAuthorize("@auth.hasPrivilege(@projectPrivileges.DELETE_PROJECT, #projectId)")
    @Transactional()
    public boolean deleteProject(UUID projectId) {
        log.info("Deleting project with id {}", projectId);
        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            projectRepository.flush(); // Ensure the delete is committed
            log.info("Project with id {} deleted successfully", projectId);
            return true;
        } else {
            log.warn("Project with id {} does not exist", projectId);
            return false;
        }
    }

    /**
     * Create a project mutation object for the given project,
     * which serves as a facade for project mutation operations.
     * AUTHORIZATION: No authorization check is performed here.
     * The authorization checks are performed in the sub-mutations.
     *
     * @param projectId the id of the project to mutate.
     * @return a project mutation object for the given project.
     */
    public ProjectMutation mutateProject(UUID projectId) {
        return new ProjectMutation(getProjectOrThrow(projectId));
    }

}
