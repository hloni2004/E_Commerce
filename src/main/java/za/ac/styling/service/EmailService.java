package za.ac.styling.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;
import za.ac.styling.domain.OrderStatus;
import za.ac.styling.domain.Product;
import za.ac.styling.domain.ProductColourSize;

import java.text.SimpleDateFormat;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOrderConfirmationEmail(Order order) {
        try {
            if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
                System.err.println("Cannot send email: Order or user data is null");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setFrom("hloniyacho@gmail.com", "E-Commerce Store");
            helper.setReplyTo("hloniyacho@gmail.com");
            helper.setSubject("Order Confirmation - " + order.getOrderNumber());
            helper.setText(buildOrderConfirmationEmail(order), true);

            mailSender.send(message);
            System.out.println("Order confirmation email sent successfully to: " + order.getUser().getEmail());
        } catch (Exception e) {
            // Log error but don't fail the order
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildOrderConfirmationEmail(Order order) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #000; color: #fff; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; letter-spacing: 2px; }");
        html.append(".content { background-color: #fff; padding: 30px; }");
        html.append(".order-info { background-color: #f5f5f5; padding: 20px; margin: 20px 0; border-radius: 5px; }");
        html.append(".item { border-bottom: 1px solid #ddd; padding: 15px 0; }");
        html.append(".item:last-child { border-bottom: none; }");
        html.append(".total { background-color: #000; color: #fff; padding: 20px; text-align: right; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append("td { padding: 8px 0; }");
        html.append(".text-right { text-align: right; }");
        html.append(".bold { font-weight: bold; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>Thank you for your order!</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<h2>Order Confirmation</h2>");
        String firstName = order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "Customer";
        html.append("<p>Hi ").append(firstName).append(",</p>");
        html.append("<p>Your order has been successfully placed and is being processed.</p>");

        // Order Info
        html.append("<div class='order-info'>");
        html.append("<table>");
        html.append("<tr><td><strong>Order Number:</strong></td><td>").append(order.getOrderNumber())
                .append("</td></tr>");
        html.append("<tr><td><strong>Order Date:</strong></td><td>").append(dateFormat.format(order.getOrderDate()))
                .append("</td></tr>");
        html.append("<tr><td><strong>Status:</strong></td><td>").append(order.getStatus()).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        // Order Items
        html.append("<h3>Order Details</h3>");
        for (OrderItem item : order.getItems()) {
            html.append("<div class='item'>");
            html.append("<table>");
            html.append("<tr>");
            html.append("<td class='bold'>").append(item.getProduct().getName()).append("</td>");
            html.append("<td class='text-right'>R").append(String.format("%.2f", item.getSubtotal())).append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td>").append(item.getColour().getName()).append(" / ")
                    .append(item.getColourSize().getSizeName()).append("</td>");
            html.append("<td class='text-right'>Qty: ").append(item.getQuantity()).append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td></td>");
            html.append("<td class='text-right'>R").append(String.format("%.2f", item.getPrice())).append(" each</td>");
            html.append("</tr>");
            html.append("</table>");
            html.append("</div>");
        }

        // Totals
        html.append("<div style='margin-top: 30px;'>");
        html.append("<table>");
        html.append("<tr><td>Subtotal:</td><td class='text-right'>R").append(String.format("%.2f", order.getSubtotal()))
                .append("</td></tr>");
        html.append("<tr><td>Shipping (").append(order.getShippingMethod().getName())
                .append("):</td><td class='text-right'>R").append(String.format("%.2f", order.getShippingCost()))
                .append("</td></tr>");
        html.append("<tr><td>Tax (15% VAT):</td><td class='text-right'>R")
                .append(String.format("%.2f", order.getTaxAmount())).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='total'>");
        html.append("<h2 style='margin: 0;'>Total: R").append(String.format("%.2f", order.getTotalAmount()))
                .append("</h2>");
        html.append("</div>");

        // Shipping Address
        html.append("<h3>Shipping Address</h3>");
        html.append("<p>");
        html.append(order.getShippingAddress().getFullName()).append("<br>");
        html.append(order.getShippingAddress().getAddressLine1()).append("<br>");
        if (order.getShippingAddress().getAddressLine2() != null
                && !order.getShippingAddress().getAddressLine2().isEmpty()) {
            html.append(order.getShippingAddress().getAddressLine2()).append("<br>");
        }
        html.append(order.getShippingAddress().getCity()).append(", ");
        html.append(order.getShippingAddress().getProvince()).append(" ");
        html.append(order.getShippingAddress().getPostalCode()).append("<br>");
        html.append(order.getShippingAddress().getCountry()).append("<br>");
        html.append("Phone: ").append(order.getShippingAddress().getPhone());
        html.append("</p>");

        html.append(
                "<p style='margin-top: 30px;'>We'll send you a shipping confirmation email as soon as your order ships.</p>");
        html.append("<p>If you have any questions, please contact us.</p>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Thank you for shopping with MAISON LUXE</p>");
        html.append("<p>This is an automated email, please do not reply.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public void sendOrderStatusChangeEmail(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
                System.err.println("Cannot send email: Order or user data is null");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setFrom("hloniyacho@gmail.com", "E-Commerce Store");
            helper.setReplyTo("hloniyacho@gmail.com");
            helper.setSubject("Order Status Update - " + order.getOrderNumber());
            helper.setText(buildOrderStatusChangeEmail(order, oldStatus, newStatus), true);

            mailSender.send(message);
            System.out.println("Order status change email sent successfully to: " + order.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send order status change email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildOrderStatusChangeEmail(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #000; color: #fff; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; letter-spacing: 2px; }");
        html.append(".content { background-color: #fff; padding: 30px; }");
        html.append(
                ".status-box { background-color: #f5f5f5; padding: 20px; margin: 20px 0; border-radius: 5px; border-left: 4px solid #000; }");
        html.append(".status-old { color: #999; text-decoration: line-through; }");
        html.append(".status-new { color: #000; font-weight: bold; font-size: 18px; }");
        html.append(".info { margin: 15px 0; }");
        html.append(".footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>Order Status Updated</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<h2>Order Status Update</h2>");
        String firstName = order.getUser().getFirstName() != null ? order.getUser().getFirstName() : "Customer";
        html.append("<p>Hi ").append(firstName).append(",</p>");
        html.append("<p>The status of your order has been updated. Please see the details below:</p>");

        // Status Box
        html.append("<div class='status-box'>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Previous Status:</strong> <span class='status-old'>").append(oldStatus)
                .append("</span></p>");
        html.append("<p><strong>Current Status:</strong> <span class='status-new'>").append(newStatus)
                .append("</span></p>");
        html.append("<p><strong>Updated On:</strong> ").append(dateFormat.format(new java.util.Date())).append("</p>");
        html.append("</div>");

        // Status-specific messages
        html.append("<div class='info'>");
        switch (newStatus) {
            case CONFIRMED:
                html.append("<p>Thank you! Your order has been confirmed and is being prepared for shipment.</p>");
                html.append("<p>We'll send you a shipping notification as soon as your order is on its way.</p>");
                break;
            case PROCESSING:
                html.append("<p>Your order is now being processed and prepared for shipment.</p>");
                html.append("<p>Expected shipping date: Within 2-3 business days.</p>");
                break;
            case SHIPPED:
                html.append("<p>Great news! Your order has been shipped!</p>");
                html.append("<p>You can track your package using the shipping information provided.</p>");
                html.append("<p>Expected delivery: 5-7 business days.</p>");
                break;
            case DELIVERED:
                html.append("<p>Your order has been successfully delivered!</p>");
                html.append("<p>If you have any issues with your order, please contact our support team.</p>");
                break;
            case CANCELLED:
                html.append("<p>Your order has been cancelled.</p>");
                html.append("<p>If you did not authorize this cancellation, please contact us immediately.</p>");
                break;
            case RETURNED:
                html.append("<p>Your return has been processed successfully.</p>");
                html.append(
                        "<p>Refund will be credited back to your original payment method within 5-7 business days.</p>");
                break;
            default:
                html.append("<p>Your order status has been updated to: ").append(newStatus).append("</p>");
        }
        html.append("</div>");

        html.append("<h3>Order Details</h3>");
        html.append("<p><strong>Order Total:</strong> R").append(String.format("%.2f", order.getTotalAmount()))
                .append("</p>");

        html.append("<p style='margin-top: 30px;'>If you have any questions, please don't hesitate to contact us.</p>");
        html.append("<p>Thank you for shopping with MAISON LUXE!</p>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email notification, please do not reply.</p>");
        html.append("<p>© MAISON LUXE - All Rights Reserved</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public void sendLowStockAlert(Product product, ProductColourSize size, int currentStock, int reorderLevel) {
        try {
            if (product == null || size == null) {
                System.err.println("Cannot send email: Product or size data is null");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("hloniyacho@gmail.com");
            helper.setFrom("hloniyacho@gmail.com", "E-Commerce Store");
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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("hloniyacho@gmail.com", "MAISON LUXE");
            helper.setReplyTo("hloniyacho@gmail.com");
            helper.setTo(to);
            helper.setSubject("Password Reset Request - MAISON LUXE");
            helper.setText(buildPasswordResetEmailWithOTP(resetLink, userName, otpCode), true);
            
            mailSender.send(message);
            System.out.println("Password reset email with OTP sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
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
        html.append("<p style='word-break: break-all; color: #666; font-size: 12px; background: #fff; padding: 10px; border-radius: 4px;'>");
        html.append(resetLink);
        html.append("</p>");
        
        // Warning
        html.append("<div class='warning'>");
        html.append("<strong>⏱️ This code and link will expire in 1 hour.</strong>");
        html.append("</div>");
        
        html.append("<p><strong>Security Note:</strong> Never share this verification code with anyone. MAISON LUXE will never ask you for this code.</p>");
        html.append("<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>");
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
            
            helper.setFrom("hloniyacho@gmail.com", "MAISON LUXE");
            helper.setReplyTo("hloniyacho@gmail.com");
            helper.setTo(to);
            helper.setSubject("Password Reset Request - MAISON LUXE");
            helper.setText(buildPasswordResetEmail(resetLink, userName), true);
            
            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + to);
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
        html.append("<p style='word-break: break-all; color: #666; font-size: 12px; background: #fff; padding: 10px; border-radius: 4px;'>");
        html.append(resetLink);
        html.append("</p>");
        
        // Warning
        html.append("<div class='warning'>");
        html.append("<strong>⏱️ This link will expire in 1 hour.</strong>");
        html.append("</div>");
        
        html.append("<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>");
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

