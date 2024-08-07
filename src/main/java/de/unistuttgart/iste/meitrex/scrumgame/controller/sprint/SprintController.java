package de.unistuttgart.iste.meitrex.scrumgame.controller.sprint;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.scrumgame.service.sprint.SprintService;
import de.unistuttgart.iste.meitrex.scrumgame.service.sprint.SprintStatsService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class SprintController {

    private final SprintService      sprintService;
    private final SprintStatsService sprintStatsService;

    @SchemaMapping
    public Sprint createSprint(ProjectMutation projectMutation, @Argument CreateSprintInput input) {
        return sprintService.createNewSprint(projectMutation.getProject().getId(), input);
    }

    @SchemaMapping
    public Sprint updateSprint(ProjectMutation projectMutation,
            @Argument int number,
            @Argument UpdateSprintInput input) {
        return sprintService.updateSprint(projectMutation.getProject().getId(), number, input);
    }

    @SchemaMapping
    public List<Sprint> sprints(Project project) {
        return sprintService.getSprints(project.getId());
    }

    @SchemaMapping
    @Nullable
    public Sprint currentSprint(Project project) {
        return sprintService.findCurrentSprint(project).orElse(null);
    }

    @SchemaMapping
    @Nullable
    public Sprint previousSprint(Project project) {
        return sprintService.findPreviousSprint(project).orElse(null);
    }

    @SchemaMapping
    public Sprint sprint(Issue issue) {
        return sprintService.getSprint(issue.getProjectId(), issue.getSprintNumber());
    }

    @SchemaMapping
    public SprintStats stats(Sprint sprint) {
        return sprintStatsService.getSprintStats(sprint);
    }

    @SchemaMapping
    public SprintUserStats userStatsByUserId(SprintStats stats, @Argument UUID userId) {
        return sprintStatsService.getUserStatsByUserId(stats, userId);
    }
}
