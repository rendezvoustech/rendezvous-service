package tech.rendezvous.rendezvousservice.rendezvous;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RendezvousRepository extends ReactiveCrudRepository<Rendezvous, Long> {
}
