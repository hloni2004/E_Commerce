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
import za.ac.styling.domain.ProductImage;
import za.ac.styling.domain.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import za.ac.styling.service.MiljetEmailClient;
import java.io.UnsupportedEncodingException;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

    public void sendOrderInvoice(User user, Order order) {
        String subject = "Your Order Invoice - Order #" + order.getOrderNumber();
        String to = user.getEmail();
        String toName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        String htmlContent = buildInvoiceHtml(user, order);
        sendHtmlEmail(to, toName, subject, htmlContent);
    }

    public void sendOrderStatusUpdate(User user, Order order) {
        String subject = "Order Status Update - Order #" + order.getOrderNumber();
        String to = user.getEmail();
        String toName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        String htmlContent = buildStatusUpdateHtml(user, order);
        sendHtmlEmail(to, toName, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendHtmlEmail(to, null, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String toName, String subject, String htmlContent) {

        if (miljetClient != null && miljetClient.isConfigured()) {
            try {
                miljetClient.sendEmail(to, toName, subject, htmlContent);
                logger.info("Email sent via Mailjet REST API to: {}", to);
                return;
            } catch (Exception e) {
                logger.error("Mailjet REST API send failed, falling back to SMTP: {}", e.getMessage(), e);

            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from");
            }
            if (from == null || from.isBlank()) {
                from = "hloniyacho@gmail.com";
            }
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setFrom(from, senderName);

            mailSender.send(message);
            logger.info("Email sent to {} with subject '{}' via SMTP", to, subject);
        } catch (MailException | jakarta.mail.MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to send email to {} via SMTP: {}", to, e.getMessage(), e);

            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    private String buildInvoiceHtml(User user, Order order) {

        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("en", "ZA"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".email-container { max-width: 650px; margin: 40px auto; background: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #000000 0%, #2d2d2d 100%); color: #ffffff; padding: 40px 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 300; }");
        html.append(".header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; letter-spacing: 1px; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }");
        html.append(".order-info { background: #f9f9f9; border-left: 4px solid #000; padding: 20px; margin: 25px 0; }");
        html.append(".order-info h2 { margin: 0 0 15px 0; font-size: 16px; color: #000; text-transform: uppercase; letter-spacing: 1px; }");
        html.append(".order-details { display: flex; justify-content: space-between; margin-bottom: 10px; }");
        html.append(".order-details span { color: #666; }");
        html.append(".order-details strong { color: #000; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 30px 0; }");
        html.append(".items-table th { background: #000; color: #fff; padding: 15px; text-align: left; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 1px; }");
        html.append(".items-table td { padding: 15px; border-bottom: 1px solid #eee; color: #333; vertical-align: middle; }");
        html.append(".items-table tr:last-child td { border-bottom: none; }");
        html.append(".items-table .item-name { font-weight: 500; }");
        html.append(".items-table .text-right { text-align: right; }");
        html.append(".items-table .text-center { text-align: center; }");
        html.append(".product-image { width: 80px; height: 80px; object-fit: cover; border-radius: 5px; display: block; }");
        html.append(".totals { margin: 30px 0; border-top: 2px solid #000; padding-top: 20px; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin: 10px 0; font-size: 15px; }");
        html.append(".total-row.grand-total { font-size: 20px; font-weight: bold; color: #000; margin-top: 15px; padding-top: 15px; border-top: 2px solid #000; }");
        html.append(".shipping-info { background: #f9f9f9; padding: 25px; margin: 30px 0; border-radius: 5px; }");
        html.append(".shipping-info h3 { margin: 0 0 15px 0; font-size: 16px; color: #000; text-transform: uppercase; letter-spacing: 1px; }");
        html.append(".shipping-info p { margin: 5px 0; color: #555; line-height: 1.6; }");
        html.append(".cta-button { display: inline-block; padding: 15px 40px; background: #000; color: #fff; text-decoration: none; border-radius: 5px; margin: 30px 0; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; }");
        html.append(".cta-button:hover { background: #333; }");
        html.append(".footer { background: #f9f9f9; padding: 30px; text-align: center; border-top: 1px solid #ddd; }");
        html.append(".footer p { margin: 8px 0; color: #666; font-size: 13px; }");
        html.append(".footer .social-links { margin: 20px 0; }");
        html.append(".footer .social-links a { color: #000; margin: 0 10px; text-decoration: none; }");
        html.append("@media only screen and (max-width: 600px) {");
        html.append("  .content { padding: 20px 15px; }");
        html.append("  .header { padding: 30px 15px; }");
        html.append("  .items-table th, .items-table td { padding: 10px 8px; font-size: 13px; }");
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='email-container'>");

        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>LUXURY FASHION & LIFESTYLE</p>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<p class='greeting'>Dear ").append(user.getFirstName()).append(",</p>");
        html.append("<p>Thank you for your order! We're excited to confirm that we've received your purchase and are processing it with care.</p>");

        html.append("<div class='order-info'>");
        html.append("<h2>Order Confirmation</h2>");
        html.append("<div class='order-details'>");
        html.append("<span>Order Number:</span>");
        html.append("<strong>").append(order.getOrderNumber()).append("</strong>");
        html.append("</div>");
        html.append("<div class='order-details'>");
        html.append("<span>Order Date:</span>");
        html.append("<strong>").append(dateFormat.format(order.getOrderDate())).append("</strong>");
        html.append("</div>");
        html.append("<div class='order-details'>");
        html.append("<span>Order Status:</span>");
        html.append("<strong style='color: #28a745;'>").append(order.getStatus()).append("</strong>");
        html.append("</div>");
        html.append("</div>");

        html.append("<table class='items-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>Image</th>");
        html.append("<th>Item</th>");
        html.append("<th class='text-center'>Quantity</th>");
        html.append("<th class='text-right'>Price</th>");
        html.append("<th class='text-right'>Subtotal</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            String imageUrl = "";

            if (product.getPrimaryImage() != null && product.getPrimaryImage().getSupabaseUrl() != null && !product.getPrimaryImage().getSupabaseUrl().isEmpty()) {
                imageUrl = product.getPrimaryImage().getSupabaseUrl();
            } else if (product.getPrimaryImage() != null && product.getPrimaryImage().getImageUrl() != null) {
                imageUrl = product.getPrimaryImage().getImageUrl();
            } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                imageUrl = product.getImages().stream().findFirst()
                    .map(ProductImage::getSupabaseUrl)
                    .orElse("");
            }

            html.append("<tr>");

            html.append("<td>");
            if (!imageUrl.isEmpty()) {
                html.append("<img src='").append(imageUrl).append("' alt='").append(product.getName())
                        .append("' class='product-image' />");
            } else {
                html.append("<div style='width:80px;height:80px;background:#f0f0f0;border-radius:5px;display:flex;align-items:center;justify-content:center;color:#999;font-size:12px;'>No Image</div>");
            }
            html.append("</td>");
            html.append("<td class='item-name'>").append(product.getName()).append("</td>");
            html.append("<td class='text-center'>").append(item.getQuantity()).append("</td>");
            html.append("<td class='text-right'>").append(currency.format(item.getPrice())).append("</td>");
            html.append("<td class='text-right'>").append(currency.format(item.getSubtotal())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        html.append("<div class='totals'>");
        html.append("<div class='total-row'>");
        html.append("<span>Subtotal:</span>");
        html.append("<span>").append(currency.format(order.getSubtotal())).append("</span>");
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>Shipping:</span>");
        html.append("<span>").append(currency.format(order.getShippingCost())).append("</span>");
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>Tax (15% VAT):</span>");
        html.append("<span>").append(currency.format(order.getTaxAmount())).append("</span>");
        html.append("</div>");
        if (order.getDiscountAmount() > 0) {
            html.append("<div class='total-row' style='color: #28a745;'>");
            html.append("<span>Discount:</span>");
            html.append("<span>-").append(currency.format(order.getDiscountAmount())).append("</span>");
            html.append("</div>");
        }
        html.append("<div class='total-row grand-total'>");
        html.append("<span>Total:</span>");
        html.append("<span>").append(currency.format(order.getTotalAmount())).append("</span>");
        html.append("</div>");
        html.append("</div>");

        if (order.getShippingAddress() != null) {
            html.append("<div class='shipping-info'>");
            html.append("<h3>Shipping Address</h3>");
            html.append("<p><strong>").append(order.getShippingAddress().getFullName()).append("</strong></p>");
            html.append("<p>").append(order.getShippingAddress().getAddressLine1()).append("</p>");
            if (order.getShippingAddress().getAddressLine2() != null && !order.getShippingAddress().getAddressLine2().isEmpty()) {
                html.append("<p>").append(order.getShippingAddress().getAddressLine2()).append("</p>");
            }
            html.append("<p>").append(order.getShippingAddress().getCity()).append(", ")
                    .append(order.getShippingAddress().getProvince()).append(" ")
                    .append(order.getShippingAddress().getPostalCode()).append("</p>");
            html.append("<p>").append(order.getShippingAddress().getPhone()).append("</p>");
            html.append("</div>");

            html.append("<div class='shipping-info'>");
            html.append("<h3>Delivery Information</h3>");
            html.append("<p><strong>Method:</strong> ").append(order.getShippingMethod().getName()).append("</p>");
            html.append("<p><strong>Estimated Delivery:</strong> ").append(order.getShippingMethod().getEstimatedDays()).append(" business days</p>");
            html.append("</div>");
        }

        html.append("<p style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='https://client-hub-portal.vercel.app/orders' class='cta-button'>Track Your Order</a>");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #666; font-size: 14px;'>");
        html.append("We'll send you a shipping confirmation email with tracking details once your order is on its way. ");
        html.append("If you have any questions about your order, feel free to contact our customer service team.");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #333;'>With gratitude,<br><strong>The MAISON LUXE Team</strong></p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<div class='social-links'>");
        html.append("<a href='#'>Facebook</a> | ");
        html.append("<a href='#'>Instagram</a> | ");
        html.append("<a href='#'>Twitter</a>");
        html.append("</div>");
        html.append("<p>Need help? Contact us at <a href='mailto:support@maisonluxe.com'>support@maisonluxe.com</a></p>");
        html.append("<p>&copy; 2026 MAISON LUXE. All rights reserved.</p>");
        html.append("<p style='font-size: 11px; color: #999;'>This email was sent to ").append(user.getEmail()).append("</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String buildStatusUpdateHtml(User user, Order order) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("en", "ZA"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".email-container { max-width: 650px; margin: 40px auto; background: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #000000 0%, #2d2d2d 100%); color: #ffffff; padding: 40px 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 300; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".status-badge { display: inline-block; padding: 10px 20px; border-radius: 5px; font-weight: bold; text-transform: uppercase; font-size: 14px; letter-spacing: 1px; margin: 20px 0; }");
        html.append(".status-pending { background: #fff3cd; color: #856404; }");
        html.append(".status-processing { background: #d1ecf1; color: #0c5460; }");
        html.append(".status-shipped { background: #d4edda; color: #155724; }");
        html.append(".status-delivered { background: #28a745; color: #fff; }");
        html.append(".status-cancelled { background: #f8d7da; color: #721c24; }");
        html.append(".order-info { background: #f9f9f9; border-left: 4px solid #000; padding: 20px; margin: 25px 0; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 30px 0; }");
        html.append(".items-table th { background: #f0f0f0; padding: 12px; text-align: left; font-weight: 600; font-size: 12px; text-transform: uppercase; }");
        html.append(".items-table td { padding: 12px; border-bottom: 1px solid #eee; vertical-align: middle; }");
        html.append(".product-image { width: 60px; height: 60px; object-fit: cover; border-radius: 5px; display: block; }");
        html.append(".footer { background: #f9f9f9; padding: 30px; text-align: center; border-top: 1px solid #ddd; }");
        html.append(".footer p { margin: 8px 0; color: #666; font-size: 13px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='email-container'>");

        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>Order Status Update</p>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<p>Hi ").append(user.getFirstName()).append(",</p>");
        html.append("<p>Your order <strong>#").append(order.getOrderNumber()).append("</strong> status has been updated.</p>");

        String statusClass = "status-" + order.getStatus().toString().toLowerCase().replace("_", "-");
        html.append("<div class='status-badge ").append(statusClass).append("'>");
        html.append(order.getStatus().toString().replace("_", " "));
        html.append("</div>");

        html.append("<div class='order-info'>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Order Date:</strong> ").append(dateFormat.format(order.getOrderDate())).append("</p>");
        html.append("<p><strong>Total Amount:</strong> ").append(currency.format(order.getTotalAmount())).append("</p>");
        html.append("</div>");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            html.append("<h3>Order Items</h3>");
            html.append("<table class='items-table'>");
            html.append("<thead>");
            html.append("<tr>");
            html.append("<th>Image</th>");
            html.append("<th>Item</th>");
            html.append("<th style='text-align:center;'>Qty</th>");
            html.append("</tr>");
            html.append("</thead>");
            html.append("<tbody>");

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                String imageUrl = "";

                if (product.getPrimaryImage() != null && product.getPrimaryImage().getSupabaseUrl() != null && !product.getPrimaryImage().getSupabaseUrl().isEmpty()) {
                    imageUrl = product.getPrimaryImage().getSupabaseUrl();
                } else if (product.getPrimaryImage() != null && product.getPrimaryImage().getImageUrl() != null) {
                    imageUrl = product.getPrimaryImage().getImageUrl();
                } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                    imageUrl = product.getImages().stream().findFirst()
                        .map(ProductImage::getSupabaseUrl)
                        .orElse("");
                }

                html.append("<tr>");

                html.append("<td>");
                if (!imageUrl.isEmpty()) {
                    html.append("<img src='").append(imageUrl).append("' alt='").append(product.getName())
                            .append("' class='product-image' />");
                } else {
                    html.append("<div style='width:60px;height:60px;background:#f0f0f0;border-radius:5px;display:flex;align-items:center;justify-content:center;color:#999;font-size:11px;'>No Image</div>");
                }
                html.append("</td>");
                html.append("<td>").append(product.getName()).append("</td>");
                html.append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>");
                html.append("</tr>");
            }

            html.append("</tbody>");
            html.append("</table>");
        }

        html.append("<p style='margin-top: 30px;'>You can view your order details and track your shipment in your account dashboard.</p>");
        html.append("<p style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='https://client-hub-portal.vercel.app/orders' style='display: inline-block; padding: 12px 30px; background: #000; color: #fff; text-decoration: none; border-radius: 5px; font-weight: 600;'>View Order</a>");
        html.append("</p>");

        html.append("<p style='color: #666; font-size: 14px;'>If you have any questions, feel free to contact our support team.</p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>Need help? Contact us at <a href='mailto:support@maisonluxe.com'>support@maisonluxe.com</a></p>");
        html.append("<p>&copy; 2025 MAISON LUXE. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public boolean sendTestEmail(String to) {
        if (to == null || to.isEmpty()) {
            System.err.println("Cannot send test email: recipient is null/empty");
            return false;
        }

        if (miljetClient != null && miljetClient.isConfigured()) {
            try {
                miljetClient.sendEmail(to, "Test User", "Test Email - E-Commerce Store",
                        "<p>This is a test email from E-Commerce application. If you received this, Mailjet REST API is configured correctly.</p>");
                logger.info("Test email sent via Mailjet REST API to {}", to);
                return true;
            } catch (Exception e) {
                logger.error("Mailjet REST API test email failed: {}", e.getMessage(), e);

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

            String adminEmail = env.getProperty("mail.sender.email", "hloniyacho@gmail.com");
            String senderName = env.getProperty("mail.sender.name", "MAISON LUXE");
            helper.setTo(adminEmail);
            helper.setFrom(adminEmail, senderName);
            helper.setSubject("Low Stock Alert - " + product.getName());
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

        html.append("<div class='header'>");
        html.append("<h1>LOW STOCK ALERT</h1>");
        html.append("<p>Immediate Action Required</p>");
        html.append("</div>");

        html.append("<div class='alert-box'>");
        html.append("<h2>Stock Level Critical</h2>");
        html.append("<p>A product has reached its reorder level. Please review and consider placing a new order.</p>");
        html.append("</div>");

        html.append("<h2>Product Details</h2>");
        html.append("<table class='info-table'>");
        html.append("<tr><td><strong>Product Name:</strong></td><td>").append(product.getName()).append("</td></tr>");
        html.append("<tr><td><strong>SKU:</strong></td><td>").append(product.getSku()).append("</td></tr>");
        html.append("<tr><td><strong>Size:</strong></td><td>").append(size.getSizeName()).append("</td></tr>");
        html.append("<tr><td><strong>Category:</strong></td><td>").append(product.getCategory().getName())
                .append("</td></tr>");
        html.append("</table>");

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

        html.append("<div class='footer'>");
        html.append("<p>This is an automated email notification, please do not reply.</p>");
        html.append("<p>© MAISON LUXE - Inventory Management System</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public void sendPasswordResetEmailWithOTP(String to, String resetLink, String userName, String otpCode) {

        if (miljetClient != null && miljetClient.isConfigured()) {
            try {

                miljetClient.sendEmail(to, userName, "Password Reset Request - MAISON LUXE",
                        buildPasswordResetEmailWithOTP(resetLink, userName, otpCode));
                logger.info("Password reset email with OTP sent via Mailjet REST API to: {}", to);
                return;
            } catch (Exception e) {
                logger.error("Mailjet REST API send failed for password reset: {}", e.getMessage(), e);

            }
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String from = env.getProperty("mail.sender.email");
            if (from == null || from.isBlank()) {
                from = env.getProperty("spring.mail.from");
            }
            if (from == null || from.isBlank()) {
                from = "hloniyacho@gmail.com";
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
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".email-container { max-width: 600px; margin: 40px auto; background: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #000000 0%, #2d2d2d 100%); color: #ffffff; padding: 40px 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 300; }");
        html.append(".header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; letter-spacing: 1px; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }");
        html.append(".icon-shield { text-align: center; margin: 30px 0; font-size: 48px; }");
        html.append(".otp-container { background: linear-gradient(135deg, #f9f9f9 0%, #ffffff 100%); border: 2px solid #000; border-radius: 10px; padding: 30px; margin: 30px 0; text-align: center; }");
        html.append(".otp-label { font-size: 14px; color: #666; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 15px; font-weight: 600; }");
        html.append(".otp-code { font-size: 40px; font-weight: bold; letter-spacing: 12px; color: #000; font-family: 'Courier New', monospace; margin: 15px 0; padding: 20px; background: #fff; border-radius: 8px; display: inline-block; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }");
        html.append(".otp-validity { font-size: 13px; color: #666; margin-top: 15px; }");
        html.append(".info-box { background: #fff3cd; border-left: 4px solid #ffc107; padding: 20px; margin: 25px 0; border-radius: 4px; }");
        html.append(".info-box h3 { margin: 0 0 10px 0; font-size: 16px; color: #856404; }");
        html.append(".info-box p { margin: 8px 0; color: #856404; font-size: 14px; line-height: 1.6; }");
        html.append(".warning-box { background: #f8d7da; border-left: 4px solid #dc3545; padding: 20px; margin: 25px 0; border-radius: 4px; }");
        html.append(".warning-box h3 { margin: 0 0 10px 0; font-size: 16px; color: #721c24; }");
        html.append(".warning-box p { margin: 8px 0; color: #721c24; font-size: 14px; line-height: 1.6; }");
        html.append(".steps { margin: 30px 0; }");
        html.append(".step { display: flex; margin: 20px 0; align-items: flex-start; }");
        html.append(".step-number { background: #000; color: #fff; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 15px; flex-shrink: 0; }");
        html.append(".step-content { flex: 1; }");
        html.append(".step-content h4 { margin: 0 0 5px 0; font-size: 15px; color: #000; }");
        html.append(".step-content p { margin: 0; color: #666; font-size: 14px; }");
        html.append(".security-tips { background: #d1ecf1; border-left: 4px solid #17a2b8; padding: 20px; margin: 25px 0; border-radius: 4px; }");
        html.append(".security-tips h3 { margin: 0 0 15px 0; font-size: 16px; color: #0c5460; }");
        html.append(".security-tips ul { margin: 0; padding-left: 20px; color: #0c5460; }");
        html.append(".security-tips li { margin: 8px 0; font-size: 14px; line-height: 1.6; }");
        html.append(".cta-button { display: inline-block; padding: 15px 40px; background: #000; color: #fff; text-decoration: none; border-radius: 5px; margin: 20px 0; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; }");
        html.append(".cta-button:hover { background: #333; }");
        html.append(".footer { background: #f9f9f9; padding: 30px; text-align: center; border-top: 1px solid #ddd; }");
        html.append(".footer p { margin: 8px 0; color: #666; font-size: 13px; }");
        html.append(".footer .social-links { margin: 20px 0; }");
        html.append(".footer .social-links a { color: #000; margin: 0 10px; text-decoration: none; }");
        html.append("@media only screen and (max-width: 600px) {");
        html.append("  .content { padding: 20px 15px; }");
        html.append("  .header { padding: 30px 15px; }");
        html.append("  .otp-code { font-size: 32px; letter-spacing: 8px; }");
        html.append("  .step { flex-direction: column; }");
        html.append("  .step-number { margin-bottom: 10px; }");
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='email-container'>");

        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>LUXURY FASHION & LIFESTYLE</p>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<div class='icon-shield'></div>");
        html.append("<p class='greeting'>Dear ").append(userName).append(",</p>");
        html.append("<p>We received a request to reset the password for your MAISON LUXE account. To ensure your account security, please use the verification code below:</p>");

        html.append("<div class='otp-container'>");
        html.append("<div class='otp-label'>Your Verification Code</div>");
        html.append("<div class='otp-code'>").append(otpCode).append("</div>");
        html.append("<div class='otp-validity'>This code expires in 15 minutes</div>");
        html.append("</div>");

        html.append("<div class='steps'>");
        html.append("<h3 style='margin-bottom: 20px; color: #000;'>How to Reset Your Password:</h3>");

        html.append("<div class='step'>");
        html.append("<div class='step-number'>1</div>");
        html.append("<div class='step-content'>");
        html.append("<h4>Enter the Code</h4>");
        html.append("<p>Copy the 6-digit code above and paste it into the password reset form on our website.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step'>");
        html.append("<div class='step-number'>2</div>");
        html.append("<div class='step-content'>");
        html.append("<h4>Create New Password</h4>");
        html.append("<p>Choose a strong, unique password that you haven't used before. Use a mix of letters, numbers, and symbols.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='step'>");
        html.append("<div class='step-number'>3</div>");
        html.append("<div class='step-content'>");
        html.append("<h4>Confirm & Login</h4>");
        html.append("<p>Confirm your new password and log in to your account with your updated credentials.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='info-box'>");
        html.append("<h3>Quick Action Required</h3>");
        html.append("<p>For your security, this verification code will expire in <strong>15 minutes</strong>. If you don't complete the password reset within this time, you'll need to request a new code.</p>");
        html.append("</div>");

        html.append("<div class='warning-box'>");
        html.append("<h3>Didn't Request This?</h3>");
        html.append("<p>If you didn't request a password reset, please ignore this email. Your password will remain unchanged and your account is secure.</p>");
        html.append("<p><strong>However, if you suspect unauthorized access:</strong></p>");
        html.append("<p>• Contact our security team immediately at <a href='mailto:security@maisonluxe.com' style='color: #721c24;'>security@maisonluxe.com</a></p>");
        html.append("<p>• Change your password as a precaution</p>");
        html.append("<p>• Review your recent account activity</p>");
        html.append("</div>");

        html.append("<div class='security-tips'>");
        html.append("<h3>Security Best Practices</h3>");
        html.append("<ul>");
        html.append("<li>Never share your verification code with anyone, including MAISON LUXE staff</li>");
        html.append("<li>Use a unique password that you don't use on other websites</li>");
        html.append("<li>Enable two-factor authentication for added security</li>");
        html.append("<li>Be wary of phishing emails asking for your personal information</li>");
        html.append("<li>Keep your contact information up to date</li>");
        html.append("</ul>");
        html.append("</div>");

        html.append("<p style='text-align: center; margin: 30px 0;'>");
        html.append("<a href='").append(resetLink).append("' class='cta-button'>Reset Password Now</a>");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #666; font-size: 14px;'>");
        html.append("If you're having trouble resetting your password or have any questions, our customer support team is here to help 24/7.");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #333;'>Stay secure,<br><strong>The MAISON LUXE Security Team</strong></p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<div class='social-links'>");
        html.append("<a href='#'>Facebook</a> | ");
        html.append("<a href='#'>Instagram</a> | ");
        html.append("<a href='#'>Twitter</a>");
        html.append("</div>");
        html.append("<p>Need help? Contact us at <a href='mailto:support@maisonluxe.com'>support@maisonluxe.com</a></p>");
        html.append("<p>&copy; 2026 MAISON LUXE. All rights reserved.</p>");
        html.append("<p style='font-size: 11px; color: #999;'>This email was sent to the registered email address</p>");
        html.append("<p style='font-size: 11px; color: #999; margin-top: 15px;'>For security reasons, this is an automated message. Please do not reply to this email.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

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

        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("</div>");

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

        html.append("<div class='warning'>");
        html.append("<strong>⏱️ This link will expire in 1 hour.</strong>");
        html.append("</div>");

        html.append(
                "<p>If you didn't request a password reset, you can safely ignore this email. Your password will remain unchanged.</p>");
        html.append("<p style='margin-top: 30px;'>Best regards,<br><strong>The MAISON LUXE Team</strong></p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("<p>&copy; 2025 MAISON LUXE. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to MAISON LUXE - Let's Get Started!";
        String to = user.getEmail();
        String toName = user.getFirstName() != null ? user.getFirstName() : user.getUsername();
        String htmlContent = buildWelcomeEmailHtml(user);
        sendHtmlEmail(to, toName, subject, htmlContent);
    }

    private String buildWelcomeEmailHtml(User user) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='en'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }");
        html.append(".email-container { max-width: 600px; margin: 40px auto; background: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #000000 0%, #2d2d2d 100%); color: #ffffff; padding: 40px 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 32px; letter-spacing: 3px; font-weight: 300; }");
        html.append(".header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; letter-spacing: 1px; }");
        html.append(".content { padding: 40px 30px; }");
        html.append(".greeting { font-size: 24px; color: #000; margin-bottom: 20px; font-weight: 600; }");
        html.append(".welcome-icon { text-align: center; margin: 30px 0; font-size: 48px; }");
        html.append(".intro-text { font-size: 16px; color: #333; line-height: 1.8; margin: 20px 0; }");
        html.append(".features { margin: 30px 0; }");
        html.append(".feature { display: flex; margin: 20px 0; align-items: flex-start; }");
        html.append(".feature-icon { background: #000; color: #fff; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; margin-right: 15px; flex-shrink: 0; font-size: 20px; }");
        html.append(".feature-content { flex: 1; }");
        html.append(".feature-content h3 { margin: 0 0 5px 0; font-size: 16px; color: #000; }");
        html.append(".feature-content p { margin: 0; color: #666; font-size: 14px; line-height: 1.6; }");
        html.append(".cta-button { display: inline-block; padding: 15px 40px; background: #000; color: #fff; text-decoration: none; border-radius: 5px; margin: 30px 0; font-weight: 600; letter-spacing: 1px; text-transform: uppercase; }");
        html.append(".cta-button:hover { background: #333; }");
        html.append(".highlight-box { background: #f9f9f9; border-left: 4px solid #000; padding: 20px; margin: 25px 0; border-radius: 4px; }");
        html.append(".highlight-box p { margin: 5px 0; color: #333; font-size: 15px; line-height: 1.6; }");
        html.append(".footer { background: #f9f9f9; padding: 30px; text-align: center; border-top: 1px solid #ddd; }");
        html.append(".footer p { margin: 8px 0; color: #666; font-size: 13px; }");
        html.append(".footer .social-links { margin: 20px 0; }");
        html.append(".footer .social-links a { color: #000; margin: 0 10px; text-decoration: none; font-weight: 600; }");
        html.append("@media only screen and (max-width: 600px) {");
        html.append("  .content { padding: 20px 15px; }");
        html.append("  .header { padding: 30px 15px; }");
        html.append("  .greeting { font-size: 20px; }");
        html.append("}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='email-container'>");

        html.append("<div class='header'>");
        html.append("<h1>MAISON LUXE</h1>");
        html.append("<p>LUXURY FASHION & LIFESTYLE</p>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<div class='welcome-icon'></div>");
        html.append("<p class='greeting'>Welcome, ").append(user.getFirstName() != null ? user.getFirstName() : user.getUsername()).append("!</p>");
        html.append("<p class='intro-text'>Thank you for joining <strong>MAISON LUXE</strong> – where luxury meets elegance. We're thrilled to have you as part of our exclusive community!</p>");
        html.append("<p class='intro-text'>Your account has been successfully created, and you're now ready to explore our curated collection of premium fashion and lifestyle products.</p>");

        html.append("<div class='highlight-box'>");
        html.append("<p><strong>Your Account Details:</strong></p>");
        html.append("<p>Email: <strong>").append(user.getEmail()).append("</strong></p>");
        html.append("<p>Member Since: <strong>").append(java.time.LocalDate.now().toString()).append("</strong></p>");
        html.append("</div>");

        html.append("<div class='features'>");
        html.append("<h3 style='margin-bottom: 20px; color: #000; text-align: center;'>What You Can Do Now:</h3>");

        html.append("<div class='feature'>");
        html.append("<div class='feature-icon'></div>");
        html.append("<div class='feature-content'>");
        html.append("<h3>Shop Luxury Collections</h3>");
        html.append("<p>Browse our exclusive range of premium products, from fashion to lifestyle essentials.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='feature'>");
        html.append("<div class='feature-icon'></div>");
        html.append("<div class='feature-content'>");
        html.append("<h3>Exclusive Offers</h3>");
        html.append("<p>Get access to members-only deals, seasonal promotions, and special discounts.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='feature'>");
        html.append("<div class='feature-icon'></div>");
        html.append("<div class='feature-content'>");
        html.append("<h3>Track Your Orders</h3>");
        html.append("<p>Monitor your purchases in real-time and manage your delivery preferences.</p>");
        html.append("</div>");
        html.append("</div>");

        html.append("<div class='feature'>");
        html.append("<div class='feature-icon'></div>");
        html.append("<div class='feature-content'>");
        html.append("<h3>Personalized Experience</h3>");
        html.append("<p>Save your favorites, manage addresses, and enjoy a tailored shopping experience.</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");

        html.append("<p style='text-align: center; margin: 40px 0 20px 0;'>");
        html.append("<a href='https://client-hub-portal.vercel.app/shop' class='cta-button'>Start Shopping Now</a>");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #666; font-size: 14px; text-align: center;'>");
        html.append("Need help getting started? Our customer support team is available 24/7 to assist you.");
        html.append("</p>");

        html.append("<p style='margin-top: 30px; color: #333; text-align: center;'>Welcome to luxury,<br><strong>The MAISON LUXE Team</strong></p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<div class='social-links'>");
        html.append("<a href='#'>Facebook</a> | ");
        html.append("<a href='#'>Instagram</a> | ");
        html.append("<a href='#'>Twitter</a>");
        html.append("</div>");
        html.append("<p>Need help? Contact us at <a href='mailto:support@maisonluxe.com'>support@maisonluxe.com</a></p>");
        html.append("<p>&copy; 2026 MAISON LUXE. All rights reserved.</p>");
        html.append("<p style='font-size: 11px; color: #999;'>This email was sent to ").append(user.getEmail()).append("</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}

