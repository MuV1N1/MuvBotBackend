package de.muv1n.muvbot.api;

import de.muv1n.muvbot.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        logger.debug("Received login request: {}", payload);
        String code = payload.get("code");
        if (code == null) {
            return ResponseEntity.badRequest().body("Missing code");
        }
        return ResponseEntity.ok(authService.handleLogin(code));
    }
}
