package za.ac.styling.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import za.ac.styling.domain.Order;
import za.ac.styling.domain.OrderItem;

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
        html.append("<tr><td><strong>Order Number:</strong></td><td>").append(order.getOrderNumber()).append("</td></tr>");
        html.append("<tr><td><strong>Order Date:</strong></td><td>").append(dateFormat.format(order.getOrderDate())).append("</td></tr>");
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
            html.append("<td>").append(item.getColour().getName()).append(" / ").append(item.getColourSize().getSizeName()).append("</td>");
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
        html.append("<tr><td>Subtotal:</td><td class='text-right'>R").append(String.format("%.2f", order.getSubtotal())).append("</td></tr>");
        html.append("<tr><td>Shipping (").append(order.getShippingMethod().getName()).append("):</td><td class='text-right'>R").append(String.format("%.2f", order.getShippingCost())).append("</td></tr>");
        html.append("<tr><td>Tax (15% VAT):</td><td class='text-right'>R").append(String.format("%.2f", order.getTaxAmount())).append("</td></tr>");
        html.append("</table>");
        html.append("</div>");

        html.append("<div class='total'>");
        html.append("<h2 style='margin: 0;'>Total: R").append(String.format("%.2f", order.getTotalAmount())).append("</h2>");
        html.append("</div>");

        // Shipping Address
        html.append("<h3>Shipping Address</h3>");
        html.append("<p>");
        html.append(order.getShippingAddress().getFullName()).append("<br>");
        html.append(order.getShippingAddress().getAddressLine1()).append("<br>");
        if (order.getShippingAddress().getAddressLine2() != null && !order.getShippingAddress().getAddressLine2().isEmpty()) {
            html.append(order.getShippingAddress().getAddressLine2()).append("<br>");
        }
        html.append(order.getShippingAddress().getCity()).append(", ");
        html.append(order.getShippingAddress().getProvince()).append(" ");
        html.append(order.getShippingAddress().getPostalCode()).append("<br>");
        html.append(order.getShippingAddress().getCountry()).append("<br>");
        html.append("Phone: ").append(order.getShippingAddress().getPhone());
        html.append("</p>");

        html.append("<p style='margin-top: 30px;'>We'll send you a shipping confirmation email as soon as your order ships.</p>");
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
}
