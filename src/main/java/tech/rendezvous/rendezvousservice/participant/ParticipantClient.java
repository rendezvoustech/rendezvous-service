package tech.rendezvous.rendezvousservice.participant;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class ParticipantClient {
    private static final String PARTICIPANTS_ROOT_API = "/participants/";
    private final WebClient webClient;

    public ParticipantClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Participant> getParticipantById(Long participantId) {
        return webClient
                .get()
                .uri(PARTICIPANTS_ROOT_API + participantId)
                .retrieve()
                .bodyToMono(Participant.class)
                .timeout(Duration.ofSeconds(3), Mono.empty())
                .onErrorResume(WebClientResponseException.NotFound.class,
                        exception -> Mono.empty())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
                .onErrorResume(Exception.class, exception -> Mono.empty());
    }
}
