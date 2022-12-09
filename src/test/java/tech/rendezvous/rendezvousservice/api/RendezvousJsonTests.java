package tech.rendezvous.rendezvousservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import tech.rendezvous.rendezvousservice.rendezvous.Rendezvous;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousStatus;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class RendezvousJsonTests {
    @Autowired
    private JacksonTester<Rendezvous> json;

    @Test
    void testSerialize() throws Exception {
        var rendezvous = new Rendezvous(394L, 455L, "Rendezvous Name", List.of(12L, 13L), RendezvousStatus.ACCEPTED, Instant.now(), Instant.now(), 21);
        var jsonContent = json.write(rendezvous);
        assertThat(jsonContent).extractingJsonPathNumberValue("@.id")
                .isEqualTo(rendezvous.id().intValue());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.ownerId")
                .isEqualTo(rendezvous.ownerId().intValue());
        assertThat(jsonContent).extractingJsonPathStringValue("@.name")
                .isEqualTo(rendezvous.name());
        assertThat(jsonContent).extractingJsonPathArrayValue("@.participantIds")
                .isEqualTo(rendezvous.participantIds().stream().map(l -> l.intValue()).collect(Collectors.toList()));
        assertThat(jsonContent).extractingJsonPathStringValue("@.status")
                .isEqualTo(rendezvous.status().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.createdDate")
                .isEqualTo(rendezvous.createdDate().toString());
        assertThat(jsonContent).extractingJsonPathStringValue("@.lastModifiedDate")
                .isEqualTo(rendezvous.lastModifiedDate().toString());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.version")
                .isEqualTo(rendezvous.version());
    }
}

