package za.ac.styling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Mailjet HTTP API client using v3.1 send API
 * Expects MILJET_API_KEY in format: "api_key:secret_key"
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
        String apiKey = env.getProperty("miljet.api.key");
        return apiKey != null && !apiKey.isBlank() && apiKey.contains(":");
    }

    public void sendEmail(String to, String subject, String html) {
        String apiKey = env.getProperty("miljet.api.key");
        if (apiKey == null || apiKey.isBlank() || !apiKey.contains(":")) {
            throw new IllegalStateException(
                    "Mailjet API key not configured correctly. Expected format: 'api_key:secret_key'");
        }

        String url = env.getProperty("miljet.api.url", "https://api.mailjet.com/v3.1/send");
        // Use mail.sender.email, then spring.mail.from as fallback
        String fromEmail = env.getProperty("mail.sender.email");
        if (fromEmail == null || fromEmail.isBlank()) {
            fromEmail = env.getProperty("spring.mail.from", "hloniyacho@gmail.com");
        }
        String fromName = env.getProperty("mail.sender.name", "MAISON LUXE");

        // Mailjet v3.1 expects Basic Auth with api_key:secret_key
        String[] credentials = apiKey.split(":");
        String encodedAuth = Base64.getEncoder().encodeToString(apiKey.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        // Mailjet v3.1 send API format
        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> message = new HashMap<>();
        message.put("From", Map.of(
                "Email", fromEmail,
                "Name", fromName));
        message.put("To", List.of(Map.of("Email", to)));
        message.put("Subject", subject);
        message.put("HTMLPart", html);

        messages.add(message);
        payload.put("Messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            logger.info("Sending email via Mailjet to: {}", to);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                logger.info(" Mailjet email sent successfully to {} (status={})", to, resp.getStatusCode().value());
            } else {
                throw new RuntimeException(
                        "Mailjet API returned status: " + resp.getStatusCode().value() + " body=" + resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Failed to send email via Mailjet to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Mailjet send failed: " + e.getMessage(), e);
        }
    }
}