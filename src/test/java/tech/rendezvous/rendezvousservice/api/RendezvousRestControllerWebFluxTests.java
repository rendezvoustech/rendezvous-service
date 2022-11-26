package tech.rendezvous.rendezvousservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import tech.rendezvous.rendezvousservice.rendezvous.Rendezvous;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousService;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(RendezvousRestController.class)
public class RendezvousRestControllerWebFluxTests {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private RendezvousService rendezvousService;

    @Test
    void whenOwnerNotDefinedThenRejectRendezvous() {
        var rendezvousRequest = new RendezvousRequest(123L, "Rendez");
        var expectedRendezvous = RendezvousService.buildRejectedRendezvous(
                rendezvousRequest.ownerId(), rendezvousRequest.name()
        );

        given (rendezvousService.createRendezvous(
                rendezvousRequest.ownerId(), rendezvousRequest.name())
        ).willReturn(Mono.just(expectedRendezvous));

        webClient
                .post()
                .uri("/rendezvous/")
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).value(rendezvous -> assertThat(rendezvous.status()).isEqualTo(RendezvousStatus.REJECTED));
    }
}
