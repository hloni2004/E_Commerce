package za.ac.styling.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColourSize;
import za.ac.styling.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import za.ac.styling.service.MiljetEmailClient;
import java.io.UnsupportedEncodingException;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Service for sending transactional emails (order invoice, status updates)
 * using Brevo (Sendinblue) SMTP.
 * All email failures are logged but do not break order processing.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final Environment env;
    @Autowired
    @Nullable
    private MiljetEmailClient miljetClient;

    @Autowired
    public EmailService(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    /**
     * Sends a professional HTML invoice email to the user after order placement.
     * 
     * @param user  The recipient user.
     * @param order The order details.
     */
    public void sendOrderInvoice(User user, Order order) {
        String subject = "Your Order Invoice - Order #" + order.getOrderNumber();
        String to = user.getEmail();
        String htmlContent = buildInvoiceHtml(user, order);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Sends an order status update email to the user when order status changes.
     * 
     * @param user  The recipient user.
     * @param order The order with updated status.
     */
    public void sendOrderStatusUpdate(User user, Order order) {
        String subject = "Order Status Update - Order #" + order.getOrderNumber();
        String to = user.getEmail();
        String htmlContent = buildStatusUpdateHtml(user, order);
        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * Core method to send an HTML email using JavaMailSender.
     * Logs any failures but does not throw, so order processing is not interrupted.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        // Prefer Miljet HTTP API if configured
        if (miljetClient != null && miljetClient.isConfigured()) {
            try {
                miljetClient.sendEmail(to, subject, htmlContent);
                return;
            } catch (Exception e) {
                logger.error("Miljet send failed, falling back to SMTP: {}", e.getMessage(), e);
                // fall through to SMTP fallback
            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            // Set sender from application.properties - use mail.sender.email, fallback to
            // spring.mail.from
            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from");
            }
            if (from == null || from.isBlank()) {
                from = "hloniyacho@gmail.com"; // Default fallback
            }
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setFrom(from, senderName);

            mailSender.send(message);
            logger.info("Email sent to {} with subject '{}' via SMTP", to, subject);
        } catch (MailException | jakarta.mail.MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email to {} via SMTP: {}", to, e.getMessage(), e);
            // Rethrow so caller knows email failed
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a professional HTML invoice for the order.
     */
    private String buildInvoiceHtml(User user, Order order) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        StringBuilder itemsTable = new StringBuilder();
        itemsTable.append("<table style='width:100%;border-collapse:collapse;'>")
                .append("<tr><th style='border:1px solid #ddd;padding:8px;'>Item</th>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>Quantity</th>")
                .append("<th style='border:1px solid #ddd;padding:8px;'>Price</th></tr>");
        for (OrderItem item : order.getItems()) {
            itemsTable.append("<tr>")
                    .append("<td style='border:1px solid #ddd;padding:8px;'>").append(item.getProduct().getName())
                    .append("</td>")
                    .append("<td style='border:1px solid #ddd;padding:8px;text-align:center;'>")
                    .append(item.getQuantity()).append("</td>")
                    .append("<td style='border:1px solid #ddd;padding:8px;text-align:right;'>")
                    .append(currency.format(item.getSubtotal())).append("</td>")
                    .append("</tr>");
        }
        itemsTable.append("</table>");

        return "<div style='font-family:sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2d3748;'>Thank you for your order!</h2>"
                + "<p>Hi " + user.getFirstName() + ",</p>"
                + "<p>Your order <b>#" + order.getOrderNumber()
                + "</b> has been placed successfully. Here is your invoice:</p>"
                + itemsTable
                + "<p style='text-align:right;font-size:1.1em;'><b>Total: "
                + currency.format(order.getTotalAmount()) + "</b></p>"
                + "<hr style='margin:24px 0;'>"
                + "<p style='font-size:0.95em;color:#555;'>If you have any questions, reply to this email or contact our support team.</p>"
                + "</div>";
    }

    /**
     * Builds a professional HTML status update for the order.
     */
    private String buildStatusUpdateHtml(User user, Order order) {
        return "<div style='font-family:sans-serif;max-width:600px;margin:auto;'>"
                + "<h2 style='color:#2d3748;'>Order Status Update</h2>"
                + "<p>Hi " + user.getFirstName() + ",</p>"
                + "<p>Your order <b>#" + order.getOrderNumber() + "</b> status has changed to: "
                + "<span style='color:#3182ce;'>" + order.getStatus() + "</span></p>"
                + "<p>You can view your order details in your account dashboard.</p>"
                + "<hr style='margin:24px 0;'>"
                + "<p style='font-size:0.95em;color:#555;'>If you have any questions, reply to this email or contact our support team.</p>"
                + "</div>";
    }

    // ...existing code...

    /**
     * Send a simple diagnostic/test email to verify SMTP configuration.
     * Returns true if send completed (may still have delivered failures reported by
     * provider).
     */
    public boolean sendTestEmail(String to) {
        if (to == null || to.isEmpty()) {
            System.err.println("Cannot send test email: recipient is null/empty");
            return false;
        }
        // Prefer Miljet if configured
        if (miljetClient != null && miljetClient.isConfigured()) {
            try {
                miljetClient.sendEmail(to, "Test Email - E-Commerce Store",
                        "<p>This is a test email from E-Commerce application. If you received this, SMTP is configured correctly.</p>");
                logger.info("Test email sent via Miljet to {}", to);
                return true;
            } catch (Exception e) {
                logger.error("Miljet test email failed: {}", e.getMessage(), e);
                // fall back to SMTP
            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from", "hloniyacho@gmail.com");
            }
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setFrom(from, senderName);
            helper.setSubject("Test Email - MAISON LUXE");
            helper.setText(
                    "<p>This is a test email from MAISON LUXE. If you received this, email is configured correctly.</p>",
                    true);

            mailSender.send(message);
            logger.info("Test email sent via SMTP to {}", to);
            return true;
        } catch (MailException me) {
            logger.error("MailException sending test email to {}: {}", to, me.getMessage(), me);
            return false;
        } catch (Exception e) {
            logger.error("Failed to send test email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    public void sendLowStockAlert(Product product, ProductColourSize size, int currentStock, int reorderLevel) {
        try {
            if (product == null || size == null) {
                System.err.println("Cannot send email: Product or size data is null");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Send low stock alerts to the admin/store email
            String adminEmail = env.getProperty("mail.sender.email", "hloniyacho@gmail.com");
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setTo(adminEmail);
            helper.setFrom(adminEmail, senderName);
            helper.setSubject("⚠️ Low Stock Alert - " + product.getName());
            helper.setText(buildLowStockAlertEmail(product, size, currentStock, reorderLevel), true);

            mailSender.send(message);
            System.out.println("Low stock alert email sent for product: " + product.getName() +
                    " (Size: " + size.getSizeName() + ", Current Stock: " + currentStock + ")");
        } catch (Exception e) {
            System.err.println("Failed to send low stock alert email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildLowStockAlertEmail(Product product, ProductColourSize size, int currentStock,
            int reorderLevel) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(
                ".header { background-color: #ff6b6b; color: #fff; padding: 30px; text-align: center; border-radius: 5px; }");
        html.append(".header h1 { margin: 0; font-size: 24px; }");
        html.append(
                ".alert-box { background-color: #fff3cd; border: 2px solid #ff9800; padding: 20px; margin: 20px 0; border-radius: 5px; }");
        html.append(".alert-box h2 { color: #ff9800; margin-top: 0; }");
        html.append(".info-table { width: 100%; border-collapse: collapse; }");
        html.append(".info-table td { padding: 12px; border-bottom: 1px solid #ddd; }");
        html.append(".info-table strong { color: #333; }");
        html.append(".status { font-weight: bold; font-size: 18px; color: #ff6b6b; }");
        html.append(
                ".action { background-color: #007bff; color: white; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0; }");
        html.append(".action a { color: white; text-decoration: none; font-weight: bold; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>⚠️ LOW STOCK ALERT</h1>");
        html.append("<p>Immediate Action Required</p>");
        html.append("</div>");

        // Alert
        html.append("<div class='alert-box'>");
        html.append("<h2>Stock Level Critical</h2>");
        html.append("<p>A product has reached its reorder level. Please review and consider placing a new order.</p>");
        html.append("</div>");

        // Product Details
        html.append("<h2>Product Details</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td><strong>Product Name:</strong></td><td>").append(product.getName()).append("</td></tr>");
        html.append("<tr><td><strong>SKU:</strong></td><td>").append(product.getSku()).append("</td></tr>");
        html.append("<tr><td><strong>Size:</strong></td><td>").append(size.getSizeName()).append("</td></tr>");
        html.append("<tr><td><strong>Category:</strong></td><td>").append(product.getCategory().getName())
                .append("</td></tr>");
        html.append("</table>");

        // Stock Status
        html.append("<h2>Stock Status</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr>");
        html.append("<td><strong>Current Stock:</strong></td>");
        html.append("<td class='status'>").append(currentStock).append(" units</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td><strong>Reorder Level:</strong></td>");
        html.append("<td>").append(reorderLevel).append(" units</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td><strong>Status:</strong></td>");
        html.append("<td>");
        if (currentStock == 0) {
            html.append("<span class='status' style='color: #d32f2f;'>OUT OF STOCK</span>");
        } else if (currentStock <= reorderLevel / 2) {
            html.append("<span class='status' style='color: #ff6b6b;'>CRITICAL - Immediate Reorder Needed</span>");
        } else {
            html.append("<span class='status' style='color: #ff9800;'>LOW - Reorder Recommended</span>");
        }
        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");

        html.append("<div class='action'>");
        html.append("<p>Please log in to your admin dashboard to manage inventory and reorder this product.</p>");
        html.append("<p><a href='https://client-hub-portal.vercel.app/admin/products'>View Admin Dashboard</a></p>");
        html.append("</div>");

        html.append("<p style='margin-top: 30px;'>");
        html.append("This is an automated alert to help you maintain optimal inventory levels.<br>");
        html.append("Please take action to prevent stockouts and lost sales.");
        html.append("</p>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email notification, please do not reply.</p>");
        html.append("<p>© MAISON LUXE - Inventory Management System</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Send password reset email with OTP
     */
    public void sendPasswordResetEmailWithOTP(String to, String resetLink, String userName, String otpCode) {
        // Prefer Miljet if configured
        if (miljetClient != null && miljetClient.isConfigured()) {
            try {
                miljetClient.sendEmail(to, "Password Reset Request - MAISON LUXE",
                        buildPasswordResetEmailWithOTP(resetLink, userName, otpCode));
                logger.info("Password reset email with OTP sent via Miljet to: {}", to);
                return;
            } catch (Exception e) {
                logger.error("Miljet send failed for password reset: {}", e.getMessage(), e);
                // fall through to SMTP logic and throw if that also fails
            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Set sender from application.properties - use mail.sender.email
            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from");
            }
            if (from == null || from.isBlank()) {
                from = "hloniyacho@gmail.com"; // Default fallback
            }
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setFrom(from, senderName);
            helper.setReplyTo(from);
            helper.setTo(to);
            helper.setSubject("Password Reset Request - MAISON LUXE");
            helper.setText(buildPasswordResetEmailWithOTP(resetLink, userName, otpCode), true);

            mailSender.send(message);
            logger.info("Password reset email with OTP sent via SMTP to: {}", to);
        } catch (org.springframework.mail.MailException me) {
            logger.error("MailException while sending password reset email to {}: {}", to, me.getMessage(), me);
            logger.error(
                    "Hint: verify SMTP credentials and provider settings (spring.mail.username / spring.mail.password). Use /api/email/test to validate.");
            throw new RuntimeException("Failed to send password reset email", me);
        } catch (Exception e) {
            logger.error("Failed to send password reset email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetEmailWithOTP(String resetLink, String userName, String otpCode) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #000; color: #fff; padding: 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; letter-spacing: 0.2em; }");
        html.append(".content { padding: 30px; background-color: #f9f9f9; }");
        html.append(".otp-box { ");
        html.append("  background-color: #000; ");
        html.append("  color: #fff; ");
        html.append("  font-size: 32px; ");
        html.append("  font-weight: bold; ");
        html.append("  padding: 20px; ");
        html.append("  text-align: center; ");
        html.append("  letter-spacing: 8px; ");
        html.append("  border-radius: 8px; ");
        html.append("  margin: 20px 0; ");
        html.append("}");
        html.append(".button { ");
        html.append("  display: inline-block; ");
        html.append("  padding: 14px 40px; ");
        html.append("  background-color: #000; ");
        html.append("  color: #fff; ");
        html.append("  text-decoration: none; ");
        html.append("  border-radius: 4px; ");
        html.append("  margin: 20px 0; ");
        html.append("  font-weight: bold; ");
        html.append("}");
        html.append(".button:hover { background-color: #333; }");
        html.append(".footer { ");
        html.append("  padding: 20px; ");
        html.append("  text-align: center; ");
        html.append("  font-size: 12px; ");
        html.append("  color: #666; ");
        html.append("  border-top: 1px solid #ddd; ");
        html.append("}");
        html.append(".warning { ");
        html.append("  background-color: #fff3cd; ");
        html.append("  border-left: 4px solid #ffc107; ");
        html.append("  padding: 12px; ");
        html.append("  margin: 20px 0; ");
        html.append("}");
        html.append(".info { ");
        html.append("  background-color: #d1ecf1; ");
        html.append("  border-left: 4px solid #0c5460; ");
        html.append("  padding: 12px; ");
        html.append("  margin: 20px 0; ");
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<h2 style='color: #000;'>Password Reset Request</h2>");
        html.append("<p>Hello ").append(userName).append(",</p>");
        html.append("<p>We received a request to reset your password for your MAISON LUXE account.</p>");

        // OTP Code
        html.append("<div class='info'>");
        html.append("<strong>🔐 Your Verification Code:</strong>");
        html.append("</div>");
        html.append("<div class='otp-box'>");
        html.append(otpCode);
        html.append("</div>");
        html.append("<p style='text-align: center; font-size: 14px; color: #666;'>");
        html.append("Enter this code on the password reset page to verify your identity");
        html.append("</p>");

        html.append("<p>Click the button below to open the password reset page:</p>");
        html.append("<p style='text-align: center;'>");
        html.append("<a href='").append(resetLink).append("' class='button'>Reset Password</a>");
        html.append("</p>");
        html.append("<p>Or copy and paste this link into your browser:</p>");
        html.append(
                "<p style='word-break: break-all; color: #666; font-size: 12px; background: #fff; padding: 10px; border-radius: 4px;'>");
        html.append(resetLink);
        html.append("</p>");

        // Warning
        html.append("<div class='warning'>");
        html.append("<strong>⏱️ This code and link will expire in 1 hour.</strong>");
        html.append("</div>");

        html.append(
                "<p><strong>Security Note:</strong> Never share this verification code with anyone. MAISON LUXE will never ask you for this code.</p>");
        html.append(
                "<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>");
        html.append("<p style='margin-top: 30px;'>Best regards,<br><strong>The MAISON LUXE Team</strong></p>");
        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("<p>&copy; 2025 MAISON LUXE. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetLink, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from", "hloniyacho@gmail.com");
            }
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setFrom(from, senderName);
            helper.setReplyTo(from);
            helper.setTo(to);
            helper.setSubject("Password Reset Request - MAISON LUXE");
            helper.setText(buildPasswordResetEmail(resetLink, userName), true);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetEmail(String resetLink, String userName) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #000; color: #fff; padding: 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; letter-spacing: 0.2em; }");
        html.append(".content { padding: 30px; background-color: #f9f9f9; }");
        html.append(".button { ");
        html.append("  display: inline-block; ");
        html.append("  padding: 14px 40px; ");
        html.append("  background-color: #000; ");
        html.append("  color: #fff; ");
        html.append("  text-decoration: none; ");
        html.append("  border-radius: 4px; ");
        html.append("  margin: 20px 0; ");
        html.append("  font-weight: bold; ");
        html.append("}");
        html.append(".button:hover { background-color: #333; }");
        html.append(".footer { ");
        html.append("  padding: 20px; ");
        html.append("  text-align: center; ");
        html.append("  font-size: 12px; ");
        html.append("  color: #666; ");
        html.append("  border-top: 1px solid #ddd; ");
        html.append("}");
        html.append(".warning { ");
        html.append("  background-color: #fff3cd; ");
        html.append("  border-left: 4px solid #ffc107; ");
        html.append("  padding: 12px; ");
        html.append("  margin: 20px 0; ");
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<h2 style='color: #000;'>Password Reset Request</h2>");
        html.append("<p>Hello ").append(userName).append(",</p>");
        html.append("<p>We received a request to reset your password for your MAISON LUXE account.</p>");
        html.append("<p>Click the button below to create a new password:</p>");
        html.append("<p style='text-align: center;'>");
        html.append("<a href='").append(resetLink).append("' class='button'>Reset Password</a>");
        html.append("</p>");
        html.append("<p>Or copy and paste this link into your browser:</p>");
        html.append(
                "<p style='word-break: break-all; color: #666; font-size: 12px; background: #fff; padding: 10px; border-radius: 4px;'>");
        html.append(resetLink);
        html.append("</p>");

        // Warning
        html.append("<div class='warning'>");
        html.append("<strong>⏱️ This link will expire in 1 hour.</strong>");
        html.append("</div>");

        html.append(
                "<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>");
        html.append("<p style='margin-top: 30px;'>Best regards,<br><strong>The MAISON LUXE Team</strong></p>");
        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("<p>&copy; 2025 MAISON LUXE. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
