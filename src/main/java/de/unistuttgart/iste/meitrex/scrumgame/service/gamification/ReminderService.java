package de.unistuttgart.iste.meitrex.scrumgame.service.gamification;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.rulesengine.DefaultEventTypes;
import de.unistuttgart.iste.meitrex.rulesengine.util.DefaultEventPublisher;
import de.unistuttgart.iste.meitrex.scrumgame.controller.event.EventController;
import de.unistuttgart.iste.meitrex.scrumgame.service.project.ProjectService;
import de.unistuttgart.iste.meitrex.scrumgame.service.sprint.SprintService;
import de.unistuttgart.iste.meitrex.scrumgame.service.sprint.SprintStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Service that publishes reminders for sprints that are behind schedule.
 * <p>
 * Note that reminders are only published when a user acesses the event list, see
 * {@link EventController#events(Project, int, int)}. This is due to the fact that
 * an auth token is needed to fetch the issue data from Gropius.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ProjectService        projectService;
    private final SprintService         sprintService;
    private final DefaultEventPublisher eventPublisher;
    private final SprintStatsService    sprintStatsService;

    private final AtomicBoolean reminderScheduled = new AtomicBoolean(true);

    public synchronized void publishRemindersIfScheduled() {
        if (!reminderScheduled.get()) {
            return;
        }
        log.info("Checking for reminders to publish...");
        List<Project> projects = projectService.getAllProjects();

        projects.forEach(this::publishRemindersForProject);

        reminderScheduled.set(false);
    }

    @Scheduled(cron = "0 0 7 * * *") // every day at 7am
    public void scheduleReminders() {
        reminderScheduled.set(true);
    }

    private void publishRemindersForProject(Project project) {
        Optional<Sprint> currentSprint = sprintService.findCurrentSprint(project);
        if (currentSprint.isEmpty()) {
            return;
        }

        SprintStats stats = sprintStatsService.getSprintStats(currentSprint.get());
        if (!isBehindSchedule(stats)) {
            return; // no reminder needed
        }

        Duration timeLeft = getTimeLeft(currentSprint.get());
        if (timeLeft.toDays() <= 2) {
            publishSprintEndReminder(project, currentSprint.get(), stats);
            return;
        }

        // otherwise: publish a reminder to catch up every 2 days
        if (timeLeft.toDays() % 2 == 0) {
            publishCatchUpReminder(project, currentSprint.get(), stats, (int) timeLeft.toDays());
        }

    }

    private Duration getTimeLeft(Sprint sprint) {
        OffsetDateTime sprintEndDate = sprint.getEndDate();
        OffsetDateTime currentDate = OffsetDateTime.now();

        return Duration.between(currentDate, sprintEndDate);
    }

    private boolean isBehindSchedule(SprintStats stats) {
        return stats.getPercentageStoryPointsCompleted() < stats.getPercentageTimeElapsed();
    }

    private void publishSprintEndReminder(Project project, Sprint sprint, SprintStats stats) {
        Integer spPlanned = sprint.getStoryPointsPlanned();
        String message;
        if (spPlanned == null) {
            message = "The sprint is ending soon. You can do it! 🚀";
        } else {
            int spTodo = spPlanned - stats.getTotalStoryPoints();
            message = "The sprint is ending soon. You have " + spTodo + " story points left to do. You can do it! 🚀";
        }

        publishReminder(project, message);
    }

    private void publishCatchUpReminder(Project project, Sprint sprint, SprintStats stats, int daysLeft) {
        Integer spPlanned = sprint.getStoryPointsPlanned();
        String message;
        if (spPlanned == null) {
            message = daysLeft + " days left in the sprint. You can do it! 🚀";
        } else {
            int spTodo = spPlanned - stats.getTotalStoryPoints();
            message = daysLeft + " days left in the sprint. You have " + spTodo +
                      " story points left to do. You can do it! 🚀";
        }

        publishReminder(project, message);
    }

    private void publishReminder(Project project, String message) {
        eventPublisher.publishEvent(CreateEventInput.builder()
                .setProjectId(project.getId())
                .setVisibility(EventVisibility.PUBLIC)
                .setEventTypeIdentifier(DefaultEventTypes.SYSTEM_MESSAGE.getIdentifier())
                .setMessage(message)
                .setEventData(List.of())
                .build());
    }

}
