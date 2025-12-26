package za.ac.styling.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.styling.service.EmailService;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
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