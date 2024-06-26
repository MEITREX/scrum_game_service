package de.unistuttgart.iste.meitrex.scrumgame.service.meeting;

import de.unistuttgart.iste.meitrex.common.exception.MeitrexNotFoundException;
import de.unistuttgart.iste.meitrex.common.service.AbstractCrudService;
import de.unistuttgart.iste.meitrex.generated.dto.MeetingType;
import de.unistuttgart.iste.meitrex.generated.dto.Project;
import de.unistuttgart.iste.meitrex.generated.dto.StandupMeeting;
import de.unistuttgart.iste.meitrex.generated.dto.StandupMeetingInput;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.meeting.standup.StandupMeetingEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.meeting.standup.StandupMeetingSettingsEmbeddable;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.StandupMeetingRepository;
import de.unistuttgart.iste.meitrex.scrumgame.service.project.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.*;

@Service
@Slf4j
public class StandupMeetingService extends AbstractCrudService<UUID, StandupMeetingEntity, StandupMeeting> {

    private final StandupMeetingRepository standupMeetingRepository;
    private final MeetingService           meetingService;
    private final ProjectService           projectService;

    public StandupMeetingService(
            ModelMapper modelMapper,
            StandupMeetingRepository standupMeetingRepository,
            MeetingService meetingService,
            ProjectService projectService
    ) {
        super(standupMeetingRepository, modelMapper, StandupMeetingEntity.class, StandupMeeting.class);
        this.standupMeetingRepository = standupMeetingRepository;
        this.meetingService = meetingService;
        this.projectService = projectService;
    }

    public StandupMeeting createStandupMeeting(Project project, StandupMeetingInput input) {
        StandupMeetingEntity standupMeeting = new StandupMeetingEntity();

        standupMeeting.setStandupMeetingSettings(
                getModelMapper().map(input.getStandupMeetingSettings(), StandupMeetingSettingsEmbeddable.class));
        standupMeeting.setMeetingType(MeetingType.STANDUP);
        standupMeeting.setProject(projectService.getProjectEntity(project.getId()));
        standupMeeting.setAttendees(meetingService.initMeetingAttendees(input.getMeetingLeaderId()));
        standupMeeting.setActive(true);

        var result = convertToDto(standupMeetingRepository.save(standupMeeting));

        meetingService.publishMeetingUpdated(result);

        return result;
    }

    public Optional<StandupMeeting> findActiveStandupMeeting(UUID projectId) {
        return standupMeetingRepository.findFirstByProjectIdAndActive(projectId, true)
                .map(this::convertToDto);
    }

    public StandupMeeting changeCurrentAttendee(Project project, UUID attendeeId) {
        return updateStandupMeeting(project.getId(), standupMeeting -> {
            if (standupMeeting.getAttendees().stream().noneMatch(attendee -> attendee.getUserId().equals(attendeeId))) {
                throw new MeitrexNotFoundException("Attendee not found in standup meeting");
            }
            standupMeeting.setCurrentAttendee(attendeeId);
        });
    }

    public Flux<StandupMeeting> getStandupMeetingUpdatedSubscription(UUID projectId) {
        return meetingService.getMeetingUpdates(projectId, MeetingType.STANDUP, StandupMeeting.class);
    }

    public StandupMeeting finishStandupMeeting(Project project) {
        StandupMeeting result = updateStandupMeeting(project.getId(),
                standupMeeting -> standupMeeting.setActive(false));

        meetingService.publishMeetingFinishedEvents(result);

        return result;
    }

    public StandupMeeting startStandupMeeting(Project project) {
        return updateStandupMeeting(project.getId(), standupMeeting -> {
            ArrayList<UUID> attendeeIds = new ArrayList<>(standupMeeting.getAttendees().size());
            standupMeeting.getAttendees().forEach(attendee -> attendeeIds.add(attendee.getUserId()));

            standupMeeting.setOrder(attendeeIds);
            Collections.shuffle(standupMeeting.getOrder());

            if (!standupMeeting.getOrder().isEmpty()) {
                standupMeeting.setCurrentAttendee(standupMeeting.getOrder().getFirst());
            }
        });
    }

    private Optional<StandupMeetingEntity> findActiveStandupMeetingEntity(UUID projectId) {
        return standupMeetingRepository.findFirstByProjectIdAndActive(projectId, true);
    }

    private synchronized StandupMeeting updateStandupMeeting(
            UUID projectId,
            Consumer<StandupMeetingEntity> modifier
    ) {
        StandupMeetingEntity standupMeeting = findActiveStandupMeetingEntity(projectId)
                .orElseThrow(() -> new MeitrexNotFoundException("No active standup meeting found"));

        modifier.accept(standupMeeting);

        StandupMeeting result = convertToDto(standupMeetingRepository.save(standupMeeting));

        meetingService.publishMeetingUpdated(result);

        return result;
    }

}
