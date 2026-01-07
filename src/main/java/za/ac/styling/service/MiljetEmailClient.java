package za.ac.styling.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

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
        String apiKey = env.getProperty("mailjet.api.key");
        String apiSecret = env.getProperty("mailjet.api.secret");
        boolean enabled = "true".equalsIgnoreCase(env.getProperty("mailjet.enabled", "true"));
        return enabled && apiKey != null && !apiKey.isBlank() && apiSecret != null && !apiSecret.isBlank();
    }

    public void sendEmail(String to, String subject, String html) {
        sendEmail(to, null, subject, html);
    }

    public void sendEmail(String to, String toName, String subject, String html) {
        String apiKey = env.getProperty("mailjet.api.key");
        String apiSecret = env.getProperty("mailjet.api.secret");

        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            logger.error("Mailjet API credentials not configured. Check 'mailjet.api.key' and 'mailjet.api.secret'");
            throw new IllegalStateException(
                    "Mailjet API credentials not configured. Both API key and secret are required.");
        }

        String url = env.getProperty("mailjet.api.url", "https://api.mailjet.com/v3.1/send");

        String fromEmail = env.getProperty("mail.sender.email");
        if (fromEmail == null || fromEmail.isBlank()) {
            fromEmail = env.getProperty("spring.mail.from", "hloniyacho@gmail.com");
        }
        String fromName = env.getProperty("mail.sender.name", "MAISON LUXE");

        logger.info("Mailjet configuration - URL: {}, From: {} <{}>, To: {} <{}>", url, fromName, fromEmail, toName != null ? toName : "User", to);

        String credentials = apiKey + "" + apiSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> message = new HashMap<>();
        message.put("From", Map.of(
                "Email", fromEmail,
                "Name", fromName));

        Map<String, String> recipient = new HashMap<>();
        recipient.put("Email", to);
        if (toName != null && !toName.isBlank()) {
            recipient.put("Name", toName);
        }
        message.put("To", List.of(recipient));

        message.put("Subject", subject);
        message.put("HTMLPart", html);

        messages.add(message);
        payload.put("Messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            logger.info("Sending email via Mailjet API to: {}", to);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ Mailjet email sent successfully to {} (status={})", to, resp.getStatusCode().value());
            } else {
                logger.error("Mailjet API error - Status: {}, Body: {}", resp.getStatusCode().value(), resp.getBody());
                throw new RuntimeException(
                        "Mailjet API returned status: " + resp.getStatusCode().value() + " body=" + resp.getBody());
            }
        } catch (Exception e) {
            logger.error("Failed to send email via Mailjet to {}: {} - {}", to, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new RuntimeException("Mailjet send failed: " + e.getMessage(), e);
        }
    }
}