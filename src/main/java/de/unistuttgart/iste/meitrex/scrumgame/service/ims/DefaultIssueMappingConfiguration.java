package de.unistuttgart.iste.meitrex.scrumgame.service.ims;

import de.unistuttgart.iste.meitrex.generated.dto.ImsSettings;
import de.unistuttgart.iste.meitrex.generated.dto.IssuePriorityConfiguration;
import de.unistuttgart.iste.meitrex.generated.dto.Project;
import de.unistuttgart.iste.meitrex.scrumgame.service.ims.gropius.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.service.ims.gropius.IssuePriorityMapping;
import de.unistuttgart.iste.meitrex.scrumgame.service.ims.gropius.IssueStateMapping;
import de.unistuttgart.iste.meitrex.scrumgame.service.ims.gropius.IssueTypeMapping;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.*;

@RequiredArgsConstructor(staticName = "of")
public class DefaultIssueMappingConfiguration implements GropiusIssueMappingConfiguration {

    private final Project project;
    private final String gropiusUrl;

    @Override
    public UUID getScrumGameProjectId() {
        return project.getId();
    }

    @Override
    public String getImsProjectId() {
        return getImsSettings().getImsProjectId();
    }

    private ImsSettings getImsSettings() {
        return project.getProjectSettings().getImsSettings();
    }

    public IssueStateMapping getIssueStateConverter() {
        return new IssueStateMapping(getImsSettings().getIssueStates());
    }

    @Override
    public String getSprintFieldName() {
        return getImsSettings().getSprintFieldName();
    }

    @Override
    public String getEstimationTemplateFieldName() {
        return getImsSettings().getEffortEstimationFieldName();
    }

    @Override
    public String getIssueTemplateId() {
        return getImsSettings().getImsIssueTemplateId();
    }

    @Override
    public IssuePriorityMapping getIssuePriorityMapping() {
        return new IssuePriorityMapping(
                getImsSettings().getIssuePriorities().stream()
                        .collect(Collectors.toMap(
                                IssuePriorityConfiguration::getImsPriorityId,
                                IssuePriorityConfiguration::getIssuePriority)));
    }

    @Override
    public IssueTypeMapping getIssueTypeMapping() {
        return new IssueTypeMapping(getImsSettings().getIssueTypes());
    }

    @Override
    public String getGropiusBaseUrl() {
        return Objects.requireNonNull(gropiusUrl, "Gropius URL must be set in application.properties");
    }
}
