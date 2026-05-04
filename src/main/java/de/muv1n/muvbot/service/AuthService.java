package de.muv1n.muvbot.service;

import de.muv1n.muvbot.entity.User;
import de.muv1n.muvbot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${discord.bot.token}")
    private String botToken;

    // These should be in application.yml or .env
    @Value("${discord.client.id}")
    private String clientId;

    @Value("${discord.client.secret}")
    private String clientSecret;

    @Value("${discord.redirect.uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> handleLogin(String code) {
        // 1. Exchange code for token
        String tokenUrl = "https://discord.com/api/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> body = response.getBody();
            
            String accessToken = (String) body.get("access_token");
            String refreshToken = (String) body.get("refresh_token");
            Integer expiresIn = (Integer) body.get("expires_in");

            logger.debug("Access Token: {}", accessToken);

            String userUrl = "https://discord.com/api/users/@me";
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, org.springframework.http.HttpMethod.GET, userRequest, Map.class);
            Map<String, Object> userBody = userResponse.getBody();
            
            String discordId = (String) userBody.get("id");
            String username = (String) userBody.get("username");
            
            User user = userRepository.findByDiscordId(discordId).orElse(new User());
            user.setDiscordId(discordId);
            user.setUsername(username);
            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            user.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            
            userRepository.save(user);

            return Map.of(
                "token", accessToken, 
                "user", Map.of("id", discordId, "username", username)
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Login failed");
        }
    }
}
