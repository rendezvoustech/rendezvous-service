package tech.rendezvous.rendezvousservice.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import tech.rendezvous.rendezvousservice.config.SecurityConfig;
import tech.rendezvous.rendezvousservice.rendezvous.Rendezvous;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousService;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(RendezvousRestController.class)
@Import(SecurityConfig.class)
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
                .mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .authorities(new SimpleGrantedAuthority("ROLE_customer")))
                .post()
                .uri("/rendezvous/")
                .bodyValue(rendezvousRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Rendezvous.class).value(rendezvous ->
                        {
                            assertThat(rendezvous).isNotNull();
                            assertThat(rendezvous.status()).isEqualTo(RendezvousStatus.REJECTED);
                        });

    }
}
