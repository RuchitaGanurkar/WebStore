package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductRequestDto;
import com.webstore.dto.response.cart.CartProductResponseDto;
import com.webstore.entity.cart.Cart;
import com.webstore.entity.cart.CartProduct;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.entity.product.Product;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.exception.cart.CartNotFoundException;
import com.webstore.exception.cart.CartProductNotFoundException;
import com.webstore.exception.cart.CartProductStatusNotFoundException;
import com.webstore.exception.product.ProductNotFoundException;
import com.webstore.repository.cart.CartProductRepository;
import com.webstore.repository.cart.CartProductStatusRepository;
import com.webstore.repository.cart.CartRepository;
import com.webstore.repository.product.ProductRepository;
import com.webstore.service.cart.CartProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartProductServiceImplementation implements CartProductService {

    private final CartProductRepository cartProductRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartProductStatusRepository statusRepository;

    public CartProductServiceImplementation(CartProductRepository cartProductRepository,
                                            CartRepository cartRepository,
                                            ProductRepository productRepository,
                                            CartProductStatusRepository statusRepository) {
        this.cartProductRepository = cartProductRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    @Transactional
    public CartProductResponseDto createCartProduct(CartProductRequestDto requestDto) {
        Cart cart = cartRepository.findById(requestDto.getCartId())
                .orElseThrow(() -> new CartNotFoundException(requestDto.getCartId()));

        Product product = productRepository.findById(Math.toIntExact(requestDto.getProductId()))
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + requestDto.getProductId()));

        CartProductStatus status = statusRepository.findById(requestDto.getStatusId())
                .orElseThrow(() -> new CartProductStatusNotFoundException(requestDto.getStatusId()));

        CartProduct cartProduct = new CartProduct();
        cartProduct.setCart(cart);
        cartProduct.setProduct(product);
        cartProduct.setQuantity(requestDto.getQuantity());
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
        List<CartProduct> list = cartProductRepository.findByCartIdAndStatusName(cartId, CartProductStatusType.valueOf("ADDED"));
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<CartProductResponseDto> getActiveCartProductsByPhoneNumber(String phoneNumber) {
        return cartProductRepository.findByPhoneAndStatusName(phoneNumber, CartProductStatusType.valueOf("ADDED"))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public Long countActiveCartProducts(Long cartId) {
        return cartProductRepository.countByCartIdAndStatusName(cartId, CartProductStatusType.valueOf("ADDED"));
    }

    @Override
    public void deleteCartProduct(Long id) {
        CartProduct cp = cartProductRepository.findById(id)
                .orElseThrow(() -> new CartProductNotFoundException(id));
        cartProductRepository.delete(cp);
    }

    @Override
    @Transactional
    public CartProductResponseDto updateCartProduct(Long cartProductId, CartProductRequestDto requestDto) {
        CartProduct existing = cartProductRepository.findById(cartProductId)
                .orElseThrow(() -> new CartProductNotFoundException(cartProductId));

        if (requestDto.getQuantity() != null && requestDto.getQuantity() > 0) {
            existing.setQuantity(requestDto.getQuantity());
        }

        if (requestDto.getStatusId() != null) {
            CartProductStatus status = statusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new CartProductStatusNotFoundException(requestDto.getStatusId()));
            existing.setStatus(status);
        }

        CartProduct updated = cartProductRepository.save(existing);
        return mapToDto(updated);
    }

    // Helper methods
    private CartProductResponseDto mapToDto(CartProduct cartProduct) {
        CartProductResponseDto dto = new CartProductResponseDto();
        dto.setCartProductId(cartProduct.getCartProductId());
        dto.setCartId(cartProduct.getCart().getCartId());
        dto.setProductId(Long.valueOf(cartProduct.getProduct().getProductId()));
        dto.setQuantity(cartProduct.getQuantity());
        dto.setStatusId(cartProduct.getStatus().getStatusId());
        dto.setCreatedAt(cartProduct.getCreatedAt());
        dto.setUpdatedAt(cartProduct.getUpdatedAt());
        return dto;
    }
}
