package tech.rendezvous.rendezvousservice.api;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.rendezvous.rendezvousservice.rendezvous.Rendezvous;
import tech.rendezvous.rendezvousservice.rendezvous.RendezvousService;

import javax.validation.Valid;

@RestController
@RequestMapping("rendezvous")
public class RendezvousRestController {
    private final RendezvousService rendezvousService;

    public RendezvousRestController(RendezvousService rendezvousService) {
        this.rendezvousService = rendezvousService;
    }

    @GetMapping
    public Flux<Rendezvous> getAllRendezvous() {
        return rendezvousService.getAllRendezvous();
    }

    @PostMapping
    public Mono<Rendezvous> postRendezvous(@RequestBody @Valid RendezvousRequest rendezvousRequest) {
        return rendezvousService.createRendezvous(
                rendezvousRequest.ownerId(), rendezvousRequest.name());
    }
}
