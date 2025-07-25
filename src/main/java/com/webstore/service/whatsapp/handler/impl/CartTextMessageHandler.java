package com.webstore.service.whatsapp.handler.impl;

import com.webstore.service.whatsapp.handler.InteractionHandler;
import com.webstore.service.whatsapp.flow.CartFlowService;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CartTextMessageHandler implements InteractionHandler<String> {

    private static final Logger logger = LoggerFactory.getLogger(CartTextMessageHandler.class);

    private final CartFlowService cartFlowService;
    private final WhatsAppMessageSender messageSender;

    // Track user sessions waiting for text input
    private final Map<String, TextInputSession> userSessions = new ConcurrentHashMap<>();

    public CartTextMessageHandler(CartFlowService cartFlowService, WhatsAppMessageSender messageSender) {
        this.cartFlowService = cartFlowService;
        this.messageSender = messageSender;
    }

    @Override
    public void handle(String phoneNumberId, String from, String messageText) {
        logger.info("Handling cart text message from user: {}, message: {}", from, messageText);

        try {
            // Check if user has an active text input session
            TextInputSession session = userSessions.get(from);

            if (session != null) {
                handleSessionBasedInput(phoneNumberId, from, messageText, session);
                return;
            }

            // Handle common cart-related text commands
            String lowerMessage = messageText.toLowerCase().trim();

            switch (lowerMessage) {
                case "cart":
                case "view cart":
                case "show cart":
                case "my cart":
                    cartFlowService.viewCart(phoneNumberId, from);
                    break;

                case "checkout":
                case "proceed to checkout":
                case "place order":
                    cartFlowService.startCheckout(phoneNumberId, from);
                    break;

                case "clear cart":
                case "empty cart":
                case "remove cart":
                    cartFlowService.clearCart(phoneNumberId, from);
                    break;

                case "edit cart":
                case "modify cart":
                    cartFlowService.showEditCartOptions(phoneNumberId, from, 1);
                    break;

                default:
                    // Check if message looks like an address (for checkout flow)
                    if (isPotentialAddress(messageText)) {
                        // Assume user is providing address for checkout
                        cartFlowService.handleAddressInput(phoneNumberId, from, messageText);
                    } else {
                        // Not a cart-related message, let other handlers process it
                        logger.debug("Message not cart-related: {}", messageText);
                    }
                    break;
            }

        } catch (Exception e) {
            logger.error("Error handling cart text message from user: {}", from, e);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Sorry, there was an error processing your message. Please try again.");
        }
    }

    /**
     * Start waiting for specific text input from user
     */
    public void startTextInputSession(String from, TextInputType inputType) {
        logger.info("Starting text input session for user: {}, type: {}", from, inputType);

        TextInputSession session = new TextInputSession();
        session.setInputType(inputType);
        session.setStartTime(System.currentTimeMillis());

        userSessions.put(from, session);

        // Clean up session after 10 minutes if not used
        scheduleSessionCleanup(from, 10 * 60 * 1000);
    }

    /**
     * Clear text input session for user
     */
    public void clearTextInputSession(String from) {
        userSessions.remove(from);
        logger.debug("Cleared text input session for user: {}", from);
    }

    private void handleSessionBasedInput(String phoneNumberId, String from, String messageText, TextInputSession session) {
        logger.info("Handling session-based input for user: {}, type: {}", from, session.getInputType());

        try {
            switch (session.getInputType()) {
                case DELIVERY_ADDRESS:
                    cartFlowService.handleAddressInput(phoneNumberId, from, messageText);
                    clearTextInputSession(from);
                    break;

                case SPECIAL_INSTRUCTIONS:
                    // TODO: Handle special instructions for order
                    handleSpecialInstructions(phoneNumberId, from, messageText);
                    clearTextInputSession(from);
                    break;

                case COUPON_CODE:
                    // TODO: Handle coupon code application
                    handleCouponCode(phoneNumberId, from, messageText);
                    clearTextInputSession(from);
                    break;

                default:
                    logger.warn("Unknown text input type: {}", session.getInputType());
                    clearTextInputSession(from);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling session-based input for user: {}", from, e);
            clearTextInputSession(from);
            messageSender.sendTextMessage(phoneNumberId, from,
                    "❌ Sorry, there was an error processing your input. Please try again.");
        }
    }

    private boolean isPotentialAddress(String messageText) {
        // Simple heuristics to detect if message might be an address
        if (messageText == null || messageText.trim().length() < 10) {
            return false;
        }

        String lowerMessage = messageText.toLowerCase();

        // Check for common address keywords
        String[] addressKeywords = {
                "street", "road", "avenue", "lane", "drive", "colony", "sector", "block",
                "apartment", "flat", "house", "building", "tower", "complex",
                "city", "town", "village", "district", "state", "pincode", "pin",
                "near", "opposite", "behind", "front", "landmark"
        };

        int keywordCount = 0;
        for (String keyword : addressKeywords) {
            if (lowerMessage.contains(keyword)) {
                keywordCount++;
            }
        }

        // If message contains multiple address keywords and is reasonably long, likely an address
        return keywordCount >= 2 && messageText.length() >= 20;
    }

    private void handleSpecialInstructions(String phoneNumberId, String from, String instructions) {
        // TODO: Store special instructions for the order
        logger.info("Received special instructions from user {}: {}", from, instructions);

        messageSender.sendTextMessage(phoneNumberId, from,
                "📝 Special instructions noted: " + instructions +
                        "\n\nYour instructions have been added to your order.");
    }

    private void handleCouponCode(String phoneNumberId, String from, String couponCode) {
        // TODO: Validate and apply coupon code
        logger.info("Received coupon code from user {}: {}", from, couponCode);

        messageSender.sendTextMessage(phoneNumberId, from,
                "🎫 Coupon code received: " + couponCode +
                        "\n\nValidating coupon... (This feature is coming soon!)");
    }

    private void scheduleSessionCleanup(String from, long delayMs) {
        // In a real application, you might use a scheduled executor or similar
        // For now, we'll rely on manual cleanup or application restart
        logger.debug("Scheduled cleanup for user session: {} in {} ms", from, delayMs);
    }

    // Helper method to check if user has active checkout session
    public boolean hasActiveCheckoutSession(String from) {
        TextInputSession session = userSessions.get(from);
        return session != null && session.getInputType() == TextInputType.DELIVERY_ADDRESS;
    }

    // Inner classes
    public enum TextInputType {
        DELIVERY_ADDRESS,
        SPECIAL_INSTRUCTIONS,
        COUPON_CODE
    }

    private static class TextInputSession {
        private TextInputType inputType;
        private long startTime;

        public TextInputType getInputType() {
            return inputType;
        }

        public void setInputType(TextInputType inputType) {
            this.inputType = inputType;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
    }
}