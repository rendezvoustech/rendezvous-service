package tech.rendezvous.rendezvousservice.api;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record RendezvousRequest(
        @NotNull
        Long ownerId,

        @NotBlank
        String name
) {
}
