package com.webstore.service.whatsapp.list.impl;

import com.webstore.service.whatsapp.list.ListActionStrategy;
import com.webstore.service.whatsapp.flow.CartFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CartListActionStrategy implements ListActionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CartListActionStrategy.class);

    private final CartFlowService cartFlowService;

    public CartListActionStrategy(CartFlowService cartFlowService) {
        this.cartFlowService = cartFlowService;
    }

    @Override
    public boolean supports(String listId) {
        return listId != null && (
                listId.equals("view_cart") ||
                        listId.equals("checkout_cart") ||
                        listId.equals("clear_cart") ||
                        listId.equals("clear_all_cart") ||
                        listId.equals("back_to_cart") ||
                        listId.equals("back_to_edit_cart") ||
                        listId.startsWith("edit_item_") ||
                        listId.startsWith("cart_next_p") ||
                        listId.startsWith("cart_prev_p") ||
                        listId.startsWith("edit_cart_next_p") ||
                        listId.startsWith("edit_cart_prev_p") ||
                        listId.startsWith("cart_qty_inc_") ||
                        listId.startsWith("cart_qty_dec_") ||
                        listId.startsWith("cart_remove_")
        );
    }

    @Override
    public void handle(String phoneNumberId, String from, String listId) {
        logger.info("Handling cart list action: {} for user: {}", listId, from);

        try {
            // Basic cart actions
            if (listId.equals("view_cart")) {
                cartFlowService.viewCart(phoneNumberId, from);
            }
            else if (listId.equals("checkout_cart")) {
                cartFlowService.startCheckout(phoneNumberId, from);
            }
            else if (listId.equals("clear_cart") || listId.equals("clear_all_cart")) {
                cartFlowService.clearCart(phoneNumberId, from);
            }
            else if (listId.equals("back_to_cart")) {
                cartFlowService.viewCart(phoneNumberId, from);
            }
            else if (listId.equals("back_to_edit_cart")) {
                cartFlowService.showEditCartOptions(phoneNumberId, from, 1);
            }

            // Edit specific cart item
            else if (listId.startsWith("edit_item_")) {
                String productIdStr = listId.substring("edit_item_".length());
                try {
                    Integer productId = Integer.valueOf(productIdStr);
                    cartFlowService.showItemQuantityOptions(phoneNumberId, from, productId);
                } catch (NumberFormatException e) {
                    logger.error("Invalid product ID in edit_item action: {}", productIdStr);
                }
            }

            // Cart pagination
            else if (listId.startsWith("cart_next_p") || listId.startsWith("cart_prev_p") ||
                    listId.startsWith("edit_cart_next_p") || listId.startsWith("edit_cart_prev_p")) {
                cartFlowService.handleCartPageNavigation(phoneNumberId, from, listId);
            }

            // Cart item quantity modifications
            else if (listId.startsWith("cart_qty_inc_")) {
                String productIdStr = listId.substring("cart_qty_inc_".length());
                try {
                    Integer productId = Integer.valueOf(productIdStr);
                    cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "increase");
                } catch (NumberFormatException e) {
                    logger.error("Invalid product ID in cart_qty_inc action: {}", productIdStr);
                }
            }
            else if (listId.startsWith("cart_qty_dec_")) {
                String productIdStr = listId.substring("cart_qty_dec_".length());
                try {
                    Integer productId = Integer.valueOf(productIdStr);
                    cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "decrease");
                } catch (NumberFormatException e) {
                    logger.error("Invalid product ID in cart_qty_dec action: {}", productIdStr);
                }
            }
            else if (listId.startsWith("cart_remove_")) {
                String productIdStr = listId.substring("cart_remove_".length());
                try {
                    Integer productId = Integer.valueOf(productIdStr);
                    cartFlowService.handleCartQuantityChange(phoneNumberId, from, productId, "remove");
                } catch (NumberFormatException e) {
                    logger.error("Invalid product ID in cart_remove action: {}", productIdStr);
                }
            }

            else {
                logger.warn("Unhandled cart list action: {}", listId);
            }

        } catch (Exception e) {
            logger.error("Error handling cart list action: {} for user: {}", listId, from, e);
            // Could send an error message to user here
        }
    }
}