package com.webstore.service.whatsapp.button.impl;

import com.webstore.dto.response.product.ProductResponseDto;
import com.webstore.service.whatsapp.business.ProductBusinessService;
import com.webstore.service.whatsapp.button.ButtonActionStrategy;
import com.webstore.service.whatsapp.core.WhatsAppMessageSender;
import com.webstore.service.whatsapp.flow.CartFlowService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddToCartStrategy  implements ButtonActionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AddToCartStrategy.class);

    private final CartFlowService cartFlowService;

    public AddToCartStrategy(CartFlowService cartFlowService) {
        this.cartFlowService = cartFlowService;
    }

    @Override
    public boolean supports(String buttonId) {
        return buttonId != null && (
                buttonId.equals("view_cart") ||
                        buttonId.equals("checkout_cart") ||
                        buttonId.equals("checkout_now") ||
                        buttonId.equals("edit_cart") ||
                        buttonId.equals("clear_cart") ||
                        buttonId.equals("back_to_cart") ||
                        buttonId.equals("back_to_edit_cart") ||
                        buttonId.equals("confirm_order") ||
                        buttonId.equals("cancel_order") ||
                        buttonId.equals("edit_details") ||
                        buttonId.startsWith("edit_cart_p") ||
                        buttonId.startsWith("cart_next_p") ||
                        buttonId.startsWith("cart_prev_p") ||
                        buttonId.startsWith("edit_cart_next_p") ||
                        buttonId.startsWith("edit_cart_prev_p") ||
                        buttonId.startsWith("add_to_cart_") ||
                        buttonId.startsWith("qty_increase_") ||
                        buttonId.startsWith("qty_decrease_") ||
                        buttonId.startsWith("cart_qty_inc_") ||
                        buttonId.startsWith("cart_qty_dec_") ||
                        buttonId.startsWith("cart_remove_") ||
                        buttonId.startsWith("payment_") ||
                        buttonId.equals("edit_address")
        );
    }

    @Override
    public void handle(String phoneNumberId, String from, String buttonId) {
        logger.info("Handling cart button action: {} for user: {}", buttonId, from);

        try {
            // Basic cart actions
            if (buttonId.equals("view_cart")) {
                cartFlowService.viewCart(phoneNumberId, from);
            }
            else if (buttonId.equals("checkout_cart") || buttonId.equals("checkout_now")) {
                cartFlowService.startCheckout(phoneNumberId, from);
            }
            else if (buttonId.equals("clear_cart")) {
                cartFlowService.clearCart(phoneNumberId, from);
            }
            else if (buttonId.equals("back_to_cart")) {
                cartFlowService.viewCart(phoneNumberId, from);
            }
            else if (buttonId.equals("back_to_edit_cart")) {
                cartFlowService.showEditCartOptions(phoneNumberId, from, 1);
            }

            // Cart editing actions
            else if (buttonId.startsWith("edit_cart")) {
                if (buttonId.equals("edit_cart")) {
                    cartFlowService.showEditCartOptions(phoneNumberId, from, 1);
                } else {
                    // Handle pagination in edit cart
                    cartFlowService.handleCartPageNavigation(phoneNumberId, from, buttonId);
                }
            }

            // Cart pagination
            else if (buttonId.startsWith("cart_next_p") || buttonId.startsWith("cart_prev_p") ||
                    buttonId.startsWith("edit_cart_next_p") || buttonId.startsWith("edit_cart_prev_p")) {
                cartFlowService.handleCartPageNavigation(phoneNumberId, from, buttonId);
            }

            // Add to cart from product selection
            else if (buttonId.startsWith("add_to_cart_")) {
                String productIdStr = buttonId.substring("add_to_cart_".length());
                Integer productId = Integer.valueOf(productIdStr);
                cartFlowService.addToCart(phoneNumberId, from, productId);
            }

            // Quantity selection buttons (from product selection)
            else if (buttonId.startsWith("qty_increase_") || buttonId.startsWith("qty_decrease_")) {
                handleQuantityAdjustment(phoneNumberId, from, buttonId);
            }

            // Cart item quantity modification
            else if (buttonId.startsWith("cart_qty_inc_")) {
                String productIdStr = buttonId.substring("cart_qty_inc_".length());
                Integer productId = Integer.valueOf(productIdStr);
                cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "increase");
            }
            else if (buttonId.startsWith("cart_qty_dec_")) {
                String productIdStr = buttonId.substring("cart_qty_dec_".length());
                Integer productId = Integer.valueOf(productIdStr);
                cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "decrease");
            }
            else if (buttonId.startsWith("cart_remove_")) {
                String productIdStr = buttonId.substring("cart_remove_".length());
                Integer productId = Integer.valueOf(productIdStr);
                cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "remove");
            }

            // Payment method selection
            else if (buttonId.startsWith("payment_")) {
                String paymentMethod = buttonId.substring("payment_".length());
                cartFlowService.selectPaymentMethod(phoneNumberId, from, paymentMethod);
            }
            else if (buttonId.equals("edit_address")) {
                // TODO: Handle address editing - for now, restart checkout
                cartFlowService.startCheckout(phoneNumberId, from);
            }

            // Order confirmation actions
            else if (buttonId.equals("confirm_order")) {
                cartFlowService.submitOrder(phoneNumberId, from);
            }
            else if (buttonId.equals("cancel_order")) {
                cartFlowService.viewCart(phoneNumberId, from);
            }
            else if (buttonId.equals("edit_details")) {
                // TODO: Handle detail editing - for now, restart checkout
                cartFlowService.startCheckout(phoneNumberId, from);
            }

            else {
                logger.warn("Unhandled cart button action: {}", buttonId);
            }

        } catch (Exception e) {
            logger.error("Error handling cart button action: {} for user: {}", buttonId, from, e);
            // Could send an error message to user here
        }
    }

    private void handleQuantityAdjustment(String phoneNumberId, String from, String buttonId) {
        try {
            // Parse button ID: qty_increase_123_5 or qty_decrease_123_5
            String[] parts = buttonId.split("_");
            if (parts.length >= 4) {
                String action = parts[1]; // "increase" or "decrease"
                Integer productId = Integer.valueOf(parts[2]);
                Integer currentQuantity = Integer.valueOf(parts[3]);

                cartFlowService.adjustQuantity(phoneNumberId, from, productId, action, currentQuantity);
            } else {
                logger.error("Invalid quantity adjustment button format: {}", buttonId);
            }
        } catch (Exception e) {
            logger.error("Error parsing quantity adjustment button: {}", buttonId, e);
        }
    }
}
