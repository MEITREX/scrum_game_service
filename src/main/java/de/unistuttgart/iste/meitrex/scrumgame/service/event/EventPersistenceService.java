package de.unistuttgart.iste.meitrex.scrumgame.service.event;

import de.unistuttgart.iste.meitrex.common.service.AbstractCrudService;
import de.unistuttgart.iste.meitrex.generated.dto.CreateEventInput;
import de.unistuttgart.iste.meitrex.generated.dto.DefaultEvent;
import de.unistuttgart.iste.meitrex.generated.dto.Event;
import de.unistuttgart.iste.meitrex.rulesengine.util.EventPersistence;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.entity.events.EventEntity;
import de.unistuttgart.iste.meitrex.scrumgame.persistence.repository.EventRepository;
import lombok.AccessLevel;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service responsible for persisting and retrieving events.
 */
@Service
@Getter(AccessLevel.PROTECTED)
public class EventPersistenceService
        extends AbstractCrudService<UUID, EventEntity, DefaultEvent>
        implements EventPersistence<Event, CreateEventInput> {

    private final EventRepository repository;
    private final EventFactory    eventFactory;

    public EventPersistenceService(EventRepository repository, EventFactory eventFactory, ModelMapper modelMapper) {
        super(repository, modelMapper, EventEntity.class, DefaultEvent.class);
        this.repository = repository;
        this.eventFactory = eventFactory;
    }

    @Override
    public boolean exists(CreateEventInput eventRequest) {
        return eventRequest.getId() != null && repository.existsById(eventRequest.getId());
    }

    @Override
    public Event getEvent(CreateEventInput event) {
        return getOrThrow(event.getId());
    }

    @Override
    protected DefaultEvent convertToDto(EventEntity entity) {
        return eventFactory.createDefaultEvent(entity);
    }

    @Override
    public Event persistEvent(CreateEventInput eventRequest) {
        if (exists(eventRequest)) {
            return getOrThrow(eventRequest.getId());
        }

        EventEntity eventEntity = eventFactory.createEventEntity(eventRequest);
        eventEntity = repository.save(eventEntity);
        return convertToDto(eventEntity);
    }
}
