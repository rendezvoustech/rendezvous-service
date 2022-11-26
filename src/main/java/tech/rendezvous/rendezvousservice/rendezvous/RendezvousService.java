package tech.rendezvous.rendezvousservice.rendezvous;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.rendezvous.rendezvousservice.participant.Participant;
import tech.rendezvous.rendezvousservice.participant.ParticipantClient;

import java.util.Collections;

@Service
public class RendezvousService {
    private final RendezvousRepository rendezvousRepository;
    private final ParticipantClient participantClient;

    public RendezvousService(ParticipantClient participantClient, RendezvousRepository rendezvousRepository) {
        this.rendezvousRepository = rendezvousRepository;
        this.participantClient = participantClient;
    }

    public Flux<Rendezvous> getAllRendezvous() {
        return rendezvousRepository.findAll();
    }

    public Mono<Rendezvous> createRendezvous(Long ownerId, String name) {
        return participantClient.getParticipantById(ownerId)
                .map(owner -> buildAcceptedRendezvous(owner, name))
                .defaultIfEmpty(buildRejectedRendezvous(ownerId, name))
                .flatMap(rendezvousRepository::save);
    }

    public static Rendezvous buildAcceptedRendezvous(Participant participant, String name) {
        return Rendezvous.of(participant.id(), name, Collections.emptyList(), RendezvousStatus.ACCEPTED);
    }

    public static Rendezvous buildRejectedRendezvous(Long ownerId, String name) {
        return Rendezvous.of(ownerId, name, Collections.emptyList(), RendezvousStatus.REJECTED);
    }
}
