package com.example.tailorapp.service;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.Payments;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class WhatsAppService {

    /**
     * Generate WhatsApp click-to-chat link with pre-filled message
     * @param client The client to send message to
     * @param payment The payment/order details
     * @return WhatsApp click-to-chat URL
     */
    public String generateReadyForPickupLink(Client client, Payments payment) {
        // Get WhatsApp number (use whatsAppNo if available, otherwise mobile)
        String phoneNumber = client.getWhatsAppNo() != null && !client.getWhatsAppNo().trim().isEmpty()
            ? client.getWhatsAppNo()
            : client.getMobile();

        // Clean phone number (remove spaces, dashes, brackets, plus sign)
        String cleanPhone = cleanPhoneNumber(phoneNumber);

        // Create message
        String message = createReadyForPickupMessage(client, payment);

        // Encode message for URL
        String encodedMessage = encodeMessage(message);

        // Generate WhatsApp link
        return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
    }

    /**
     * Generate reminder message link for orders not picked up
     */
    public String generateReminderLink(Client client, Payments payment) {
        String phoneNumber = client.getWhatsAppNo() != null && !client.getWhatsAppNo().trim().isEmpty()
            ? client.getWhatsAppNo()
            : client.getMobile();

        String cleanPhone = cleanPhoneNumber(phoneNumber);
        String message = createReminderMessage(client, payment);
        String encodedMessage = encodeMessage(message);

        return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
    }

    /**
     * Create ready-for-pickup message
     */
    private String createReadyForPickupMessage(Client client, Payments payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        StringBuilder message = new StringBuilder();
        message.append("*Stitch & Style - Order Ready* 🎉\n\n");
        message.append("Assalam o Alaikum *").append(client.getName()).append("*,\n\n");
        message.append("Good news! Your dress order is ready for pickup.\n\n");

        // Add order details
        message.append("*Order Details:*\n");
        if (payment.getDressCount() != null && payment.getDressCount() > 0) {
            message.append("• Dresses: ").append(payment.getDressCount()).append("\n");
        }
        if (payment.getWaistcoatCount() != null && payment.getWaistcoatCount() > 0) {
            message.append("• Waistcoats: ").append(payment.getWaistcoatCount()).append("\n");
        }

        if (payment.getReturnDate() != null) {
            message.append("\n*Expected Pickup Date:* ").append(payment.getReturnDate().format(formatter)).append("\n");
        }

        message.append("\nPlease collect it at your earliest convenience.\n\n");
        message.append("Thank you for choosing Stitch & Style! 👔✨");

        return message.toString();
    }

    /**
     * Create reminder message for late pickup
     */
    private String createReminderMessage(Client client, Payments payment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        StringBuilder message = new StringBuilder();
        message.append("*Stitch & Style - Pickup Reminder* ⏰\n\n");
        message.append("Assalam o Alaikum *").append(client.getName()).append("*,\n\n");
        message.append("This is a friendly reminder that your dress order has been ready and waiting for pickup");

        if (payment.getReturnDate() != null) {
            message.append(" since *").append(payment.getReturnDate().format(formatter)).append("*");
        }
        message.append(".\n\n");

        message.append("Please collect it as soon as possible.\n\n");
        message.append("Thank you! 🙏");

        return message.toString();
    }

    /**
     * Clean phone number by removing all non-digit characters
     * Keeps country code if present
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }

        // Remove all non-digit characters except plus at the start
        String cleaned = phone.replaceAll("[^0-9+]", "");

        // If it starts with +, keep it, otherwise just digits
        if (cleaned.startsWith("+")) {
            return cleaned;
        }

        // If it starts with 0 and is Pakistani number, replace with +92
        if (cleaned.startsWith("0") && cleaned.length() == 11) {
            return "92" + cleaned.substring(1);
        }

        // If it doesn't have country code and is 10 digits, assume Pakistan
        if (cleaned.length() == 10) {
            return "92" + cleaned;
        }

        return cleaned;
    }

    /**
     * URL encode the message
     */
    private String encodeMessage(String message) {
        try {
            return URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // This should never happen with UTF-8
            return message.replace(" ", "%20");
        }
    }
}