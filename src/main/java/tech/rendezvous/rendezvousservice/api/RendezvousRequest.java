package tech.rendezvous.rendezvousservice.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record RendezvousRequest(
        @NotNull(message = "The owner Id must be defined.")
        Long ownerId,

        @NotBlank(message = "The name of must be defined and not blank.")
        String name
) {
}
