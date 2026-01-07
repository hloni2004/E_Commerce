package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.service.EmailService;
import za.ac.styling.service.MiljetEmailClient;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final MiljetEmailClient miljetClient;

    @Autowired
    public EmailController(EmailService emailService, MiljetEmailClient miljetClient) {
        this.emailService = emailService;
        this.miljetClient = miljetClient;
    }

    @GetMapping("/config-check")
    public ResponseEntity<?> checkEmailConfiguration() {
        Map<String, Object> status = new HashMap<>();

        boolean mailjetConfigured = miljetClient.isConfigured();
        status.put("mailjetRestApiConfigured", mailjetConfigured);
        status.put("mailjetEnabled", mailjetConfigured ? "✅ Enabled" : "❌ Disabled or Missing Credentials");

        if (mailjetConfigured) {
            status.put("message", "Mailjet REST API is properly configured and ready to use.");
            status.put("recommendation", "Use POST /api/email/test with {\"email\":\"your@email.com\"} to send a test email.");
        } else {
            status.put("message", "Mailjet REST API is NOT configured. Check application.properties.");
            status.put("requiredProperties", "mailjet.api.key, mailjet.api.secret, mailjet.enabled=true");
        }

        return ResponseEntity.ok(status);
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing 'email' in request body"));
        }

        boolean ok = emailService.sendTestEmail(email);
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Test email sent (check inbox/spam)."));
        } else {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to send test email. Check server logs for details."));
        }
    }
}