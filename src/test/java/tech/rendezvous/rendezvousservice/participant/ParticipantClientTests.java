package tech.rendezvous.rendezvousservice.participant;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;


public class ParticipantClientTests {
    private MockWebServer mockWebServer;
    private ParticipantClient participantClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").uri().toString())
                .build();
        this.participantClient = new ParticipantClient(webClient);
    }

    @AfterEach
    void clean() throws IOException{
        this.mockWebServer.shutdown();
    }

    @Test
    void whenOwnerExistsThenReturnParticipant() {
        Long ownerId = 353534L;

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setBody("""
                        {
                            "id": %s,
                            "name": "Owner"
                        }
                        """.formatted(ownerId.toString()));

        mockWebServer.enqueue(mockResponse);

        Mono<Participant> participant = participantClient.getParticipantById(ownerId);

        StepVerifier.create(participant)
                .expectNextMatches(
                        p -> p.id().equals(ownerId)
                ).verifyComplete();
    }

    @Test
    void whenOwnerNotExistsThenReturnEmpty() {
        Long ownerId = 353535L;

        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(404);

        mockWebServer.enqueue(mockResponse);

        StepVerifier.create(participantClient.getParticipantById(ownerId))
                .expectNextCount(0)
                .verifyComplete();
    }
}
