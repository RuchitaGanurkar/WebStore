package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.CartProductNotFoundException;
import com.webstore.exception.cart.CartProductStatusNotFoundException;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.cart.CartProductStatusRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.service.cart.CartProductService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


public class CartProductServiceImplementation implements CartProductService {

        private final CartProductRepository cartProductRepository;
        private final CartRepository cartRepository;
        private final CartProductStatusRepository statusRepository;

    public CartProductServiceImplementation(CartProductRepository cartProductRepository, CartRepository cartRepository, CartProductStatusRepository statusRepository) {
        this.cartProductRepository = cartProductRepository;
        this.cartRepository = cartRepository;
        this.statusRepository = statusRepository;
    }

    @Override
        @Transactional
        public CartProductResponseDto createCartProduct(CartProductRequestDto requestDto) {
            Cart cart = cartRepository.findById(requestDto.getCartId())
                    .orElseThrow(() -> new CartNotFoundException(requestDto.getCartId()));

            CartProductStatus status = statusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new CartProductStatusNotFoundException(requestDto.getStatusId()));

            CartProduct cartProduct = new CartProduct();
            cartProduct.setCart(cart);
            cartProduct.setStatus(status);

            CartProduct saved = cartProductRepository.save(cartProduct);

            return mapToDto(saved);
        }

        @Override
        public CartProductResponseDto getCartProductById(Long id) {
            CartProduct cp = cartProductRepository.findById(id)
                    .orElseThrow(() -> new CartProductNotFoundException(id));
            return mapToDto(cp);
        }

        @Override
        public List<CartProductResponseDto> getCartProductsByCartId(Long cartId) {
            List<CartProduct> list = cartProductRepository.findByCartCartIdAndStatus(cartId, getStatus("ADDED"));
            return list.stream().map(this::mapToDto).collect(Collectors.toList());
        }

        @Override
        public List<CartProductResponseDto> getActiveCartProductsByPhoneNumber(String phoneNumber) {
            return cartProductRepository.findActiveProductsByPhoneNumber(phoneNumber)
                    .stream().map(this::mapToDto).collect(Collectors.toList());
        }

        @Override
        public Long countActiveCartProducts(Long cartId) {
            return cartProductRepository.countActiveProductsInCart(cartId);
        }

        @Override
        public void deleteCartProduct(Long id) {
            CartProduct cp = cartProductRepository.findById(id)
                    .orElseThrow(() -> new CartProductNotFoundException(id));
            cartProductRepository.delete(cp);
        }

        // Helper methods
        private CartProductStatus getStatus(String name) {
            return statusRepository.findByStatusName(name)
                    .orElseThrow(() -> new CartProductStatusNotFoundException("Status not found with name: " + name));
        }

        private CartProductResponseDto mapToDto(CartProduct cartProduct) {
            CartProductResponseDto dto = new CartProductResponseDto();
            dto.setCartProductId(cartProduct.getCartProductId());
            dto.setCartId(cartProduct.getCart().getCartId());
            dto.setStatusId(cartProduct.getStatus().getStatusId());
            dto.setCreatedAt(cartProduct.getCreatedAt());
            dto.setUpdatedAt(cartProduct.getUpdatedAt());
            return dto;
        }
    }
