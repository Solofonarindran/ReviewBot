package fr.reviewbot.infrastructure.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Statut", description = "Vérifier que l'application répond")
public class StatusController {

    @GetMapping("/status")
    @Operation(summary = "État de l'application",
            description = "Indique aussi si les threads virtuelles sont bien actives.")
    public StatusResponse status() {
        return new StatusResponse(
                "ReviewBot",
                "UP",
                Runtime.version().toString(),
                Thread.currentThread().isVirtual());
    }
}
