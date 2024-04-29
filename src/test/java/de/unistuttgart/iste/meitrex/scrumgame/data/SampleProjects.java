package de.unistuttgart.iste.meitrex.scrumgame.data;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.project.*;

import java.util.List;

public class SampleProjects {

    private SampleProjects() {
    }


    public static CreateProjectInput getSampleCreateProjectInput() {
        return CreateProjectInput.builder()
                .setName("Test Project")
                .setDescription("Test Description")
                .setProjectSettings(ProjectSettingsInput.builder()
                        .setCodeRepositorySettings(CodeRepositorySettingsInput.builder()
                                .setCodeRepositoryName("Test Repository")
                                .build())
                        .setImsSettings(ImsSettingsInput.builder()
                                .setImsName("Test IMS")
                                .setImsProjectId("Test IMS Project ID")
                                .setIssueStates(List.of(
                                        IssueStateInput.builder()
                                                .setName("Test State")
                                                .setImsStateId("Test IMS State ID")
                                                .setType(IssueStateType.SPRINT_BACKLOG)
                                                .build(),
                                        IssueStateInput.builder()
                                                .setName("Test State 2")
                                                .setImsStateId("Test IMS State ID 2")
                                                .setType(IssueStateType.IN_PROGRESS)
                                                .build()))
                                .build())
                        .build())
                .build();
    }

    public static UpdateProjectInput getSampleUpdateProjectInput() {
        return UpdateProjectInput.builder()
                .setName("Updated Test Project")
                .setDescription("Updated Test Project")
                .setProjectSettings(ProjectSettingsInput.builder()
                        .setCodeRepositorySettings(CodeRepositorySettingsInput.builder()
                                .setCodeRepositoryName("Updated Test IMS")
                                .build())
                        .setImsSettings(ImsSettingsInput.builder()
                                .setImsName("Updated Test IMS")
                                .setImsProjectId("Updated Test IMS Project ID")
                                .setIssueStates(List.of(
                                        IssueStateInput.builder()
                                                .setName("Updated Test State")
                                                .setImsStateId("Updated Test IMS State ID")
                                                .setType(IssueStateType.DONE)
                                                .build(),
                                        IssueStateInput.builder()
                                                .setName("Updated Test State 2")
                                                .setImsStateId("Updated Test IMS State ID 2")
                                                .setType(IssueStateType.DONE_SPRINT)
                                                .build()))
                                .build())
                        .build())
                .build();
    }

    public static ProjectEntity.ProjectEntityBuilder sampleProjectBuilder() {
        return ProjectEntity.builder()
                .name("Test Project")
                .description("Test Description")
                .projectSettings(ProjectSettingsEntity.builder()
                        .codeRepositorySettings(CodeRepositorySettingsEntity.builder()
                                .codeRepositoryName("Test Repository")
                                .build())
                        .imsSettings(ImsSettingsEntity.builder()
                                .imsName("Test IMS")
                                .imsProjectId("Test IMS Project ID")
                                .issueStates(List.of(
                                        IssueStateEmbeddable.builder()
                                                .name("Test State")
                                                .imsStateId("Test IMS State ID")
                                                .type(IssueStateType.SPRINT_BACKLOG)
                                                .build(),
                                        IssueStateEmbeddable.builder()
                                                .name("Test State 2")
                                                .imsStateId("Test IMS State ID 2")
                                                .type(IssueStateType.IN_PROGRESS)
                                                .build()))
                                .build())
                        .build());
    }
}