package za.ac.styling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple HTTP client to send emails via Miljet API when configured.
 * This client expects a JSON payload with keys: `to`, `subject`, `html`.
 * It sets header `Authorization: Bearer <MILJET_API_KEY>` by default.
 */
@Component
public class MiljetEmailClient {
    private static final Logger logger = LoggerFactory.getLogger(MiljetEmailClient.class);
    private final Environment env;
    private final RestTemplate restTemplate;

    @Autowired
    public MiljetEmailClient(Environment env) {
        this.env = env;
        this.restTemplate = new RestTemplate();
    }

    public boolean isConfigured() {
        return env.getProperty("miljet.api.key") != null && !env.getProperty("miljet.api.key").isBlank();
    }

    public void sendEmail(String to, String subject, String html) {
        String apiKey = env.getProperty("miljet.api.key");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Miljet API key not configured");
        }

        String url = env.getProperty("miljet.api.url", "https://api.miljet.email/v1/send");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("to", to);
        payload.put("subject", subject);
        payload.put("html", html);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                logger.info("Miljet email sent to {} (status={})", to, resp.getStatusCodeValue());
            } else {
                throw new RuntimeException(
                        "Miljet API returned status: " + resp.getStatusCodeValue() + " body=" + resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Failed to send email via Miljet to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Miljet send failed: " + e.getMessage(), e);
        }
    }
}
