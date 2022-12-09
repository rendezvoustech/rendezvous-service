package tech.rendezvous.rendezvousservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
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
@Import(TestChannelBinderConfiguration.class)
@Testcontainers
class RendezvousServiceApplicationTests {

    // Customer
    private static KeycloakToken bjornTokens;
    // Customer and employee
    private static KeycloakToken isabelleTokens;

    @Container
    private static final KeycloakContainer keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:19.0")
            .withRealmImportFile("test-realm-config.json");
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

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/RendezvousTech");
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s", postgresql.getHost(),
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT), postgresql.getDatabaseName());
    }

    @BeforeAll
    static void generateAccessTokens() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakContainer.getAuthServerUrl() + "realms/RendezvousTech/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();

        isabelleTokens = authenticateWith("isabelle", "password", webClient);
        bjornTokens = authenticateWith("bjorn", "password", webClient);
    }

    @Test
    void whenGetRendezvousThenReturn() {
        Long participantId = 343L;
        Participant participant = new Participant(participantId, "P");
        given(participantClient.getParticipantById(participantId)).willReturn(Mono.just(participant));
        RendezvousRequest rendezvousRequest = new RendezvousRequest(participantId, "Test");

        Rendezvous expectedRendezvous = webTestClient.post().uri("/rendezvous")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).returnResult().getResponseBody();
        assertThat(expectedRendezvous).isNotNull();

        webTestClient.get().uri("/rendezvous")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBodyList(Rendezvous.class).value(orders ->
                        assertThat(orders.stream().filter(rendezvous -> rendezvous.ownerId().equals(participantId))
                                .findAny()).isNotEmpty());
    }

    @Test
    void whenPostRequestAndParticipantExistsThenRendezvousIsAccepted() {
        Long participantId = 343L;
        Participant participant = new Participant(participantId, "P");
        given(participantClient.getParticipantById(participantId)).willReturn(Mono.just(participant));
        RendezvousRequest rendezvousRequest = new RendezvousRequest(participantId, "RRT");

        Rendezvous createdRendezvous= webTestClient.post().uri("/rendezvous")
                .headers(headers -> headers.setBearerAuth(bjornTokens.accessToken()))
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).returnResult().getResponseBody();

        assertThat(createdRendezvous).isNotNull();
        assertThat(createdRendezvous.ownerId()).isEqualTo(rendezvousRequest.ownerId());
        assertThat(createdRendezvous.name()).isEqualTo(rendezvousRequest.name());
        assertThat(createdRendezvous.status()).isEqualTo(RendezvousStatus.ACCEPTED);
    }

    private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "rendezvous-test")
                        .with("username", username)
                        .with("password", password)
                )
                .retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    private record KeycloakToken(String accessToken) {

        @JsonCreator
        private KeycloakToken(@JsonProperty("access_token") final String accessToken) {
            this.accessToken = accessToken;
        }

    }
}
