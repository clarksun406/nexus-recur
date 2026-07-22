package com.nexus.recur.application.service;

import com.nexus.recur.domain.model.SubscriptionEvent;
import com.nexus.recur.domain.repository.SubscriptionEventRepository;
import com.nexus.recur.infrastructure.support.IdGenerator;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private final SubscriptionEventRepository eventRepository;
    private final IdGenerator idGenerator;

    public EventService(SubscriptionEventRepository eventRepository, IdGenerator idGenerator) {
        this.eventRepository = eventRepository;
        this.idGenerator = idGenerator;
    }

    public void record(String subscriptionId, String eventType, String source, String rawPayload) {
        SubscriptionEvent event = new SubscriptionEvent();
        event.setId(idGenerator.next("evt"));
        event.setSubscriptionId(subscriptionId);
        event.setEventType(eventType);
        event.setSource(source);
        event.setRawPayload(rawPayload);
        eventRepository.save(event);
    }
}
