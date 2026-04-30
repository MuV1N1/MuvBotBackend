package de.muv1n.muvbot.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/healthcheck")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Void> healthCheck() { 
        return ResponseEntity.ok().build();
    }
}
