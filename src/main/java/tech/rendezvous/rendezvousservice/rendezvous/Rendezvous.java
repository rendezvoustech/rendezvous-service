package tech.rendezvous.rendezvousservice.rendezvous;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;

@Table("rendezvous")
public record Rendezvous(
    @Id
    Long id,
    Long ownerId,
    String name,
    List<Long> participantIds,
    RendezvousStatus status,

    @CreatedDate
    Instant createdDate,

    @LastModifiedDate
    Instant lastModifiedDate,

    @Version
    int version) {
    public static Rendezvous of(
        Long ownerId, String name, List<Long> participantIds, RendezvousStatus status
    ) {
        return new Rendezvous(
                null, ownerId, name, participantIds, status,
                null, null, 0);
    }
}
