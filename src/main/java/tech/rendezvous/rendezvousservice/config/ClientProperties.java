package tech.rendezvous.rendezvousservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.net.URI;

@ConfigurationProperties(prefix = "rendezvous")
public record ClientProperties(
        @NotNull
        URI participantServiceUri
) {
}
