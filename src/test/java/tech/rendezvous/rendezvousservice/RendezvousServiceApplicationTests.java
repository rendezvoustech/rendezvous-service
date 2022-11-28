package tech.rendezvous.rendezvousservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import tech.rendezvous.rendezvousservice.api.RendezvousRequest;
import tech.rendezvous.rendezvousservice.participant.Participant;
import tech.rendezvous.rendezvousservice.participant.ParticipantClient;
import tech.rendezvous.rendezvousservice.rendezvous.Rendezvous;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RendezvousServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ParticipantClient participantClient;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", RendezvousServiceApplicationTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
    }

    @Test
    void whenGetRendezvousThenReturn() {
        Long participantId = 343L;
        Participant participant = new Participant(participantId, "P");
        given(participantClient.getParticipantById(participantId)).willReturn(Mono.just(participant));
        RendezvousRequest rendezvousRequest = new RendezvousRequest(participantId, "Test");
        Rendezvous expectedRendezvous = webTestClient.post().uri("/rendezvous")
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).returnResult().getResponseBody();
        assertThat(expectedRendezvous).isNotNull();

        webTestClient.get().uri("/rendezvous")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Rendezvous.class).value(orders -> assertThat(orders.stream().filter(rendezvous -> rendezvous.ownerId().equals(participantId)).findAny()).isNotEmpty());
    }

    @Test
    void whenPostRequestAndParticipantExistsThenRendezvousIsAccepted() {
        Long participantId = 343L;
        Participant participant = new Participant(participantId, "P");
        given(participantClient.getParticipantById(participantId)).willReturn(Mono.just(participant));
        RendezvousRequest rendezvousRequest = new RendezvousRequest(participantId, "RRT");

        Rendezvous createdRendezvous= webTestClient.post().uri("/rendezvous")
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).returnResult().getResponseBody();

        assertThat(createdRendezvous).isNotNull();
        assertThat(createdRendezvous.ownerId()).isEqualTo(rendezvousRequest.ownerId());
        assertThat(createdRendezvous.name()).isEqualTo(rendezvousRequest.name());
        assertThat(createdRendezvous.status()).isEqualTo(RendezvousStatus.ACCEPTED);
    }
}
