package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartProductStatusRequestDto;
import com.webstore.dto.response.cart.CartProductStatusResponseDto;
import com.webstore.entity.cart.CartProductStatus;
import com.webstore.enums.cart.CartProductStatusType;
import com.webstore.repository.cart.CartProductStatusRepository;
import com.webstore.service.cart.CartProductStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartProductStatusServiceImplementation implements CartProductStatusService {

    private final CartProductStatusRepository cartProductStatusRepository;

    @Override
    public CartProductStatusResponseDto getCartProductStatusById(Integer statusId) {
        CartProductStatus cartProductStatus = cartProductStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Cart product status not found with id: " + statusId));
        return mapToResponseDto(cartProductStatus);
    }

    @Override
    public List<CartProductStatusResponseDto> getAllCartProductStatuses() {
        return cartProductStatusRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartProductStatusResponseDto updateCartProductStatus(Integer statusId, CartProductStatusRequestDto requestDto) {
        CartProductStatus cartProductStatus = cartProductStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Cart product status not found with id: " + statusId));

        CartProductStatusType statusType = CartProductStatusType.valueOf(requestDto.getStatusName().toUpperCase());
        cartProductStatus.setStatusName(statusType);
        cartProductStatus.setUpdatedAt(LocalDateTime.now());

        CartProductStatus updatedStatus = cartProductStatusRepository.save(cartProductStatus);
        return mapToResponseDto(updatedStatus);
    }

    @Override
    public void deleteCartProductStatus(Integer statusId) {
        if (!cartProductStatusRepository.existsById(statusId)) {
            throw new RuntimeException("Cart product status not found with id: " + statusId);
        }
        cartProductStatusRepository.deleteById(statusId);
    }

    // ✅ Utility mapper
    private CartProductStatusResponseDto mapToResponseDto(CartProductStatus cartProductStatus) {
        CartProductStatusResponseDto responseDto = new CartProductStatusResponseDto();
        responseDto.setStatusId(cartProductStatus.getStatusId());
        responseDto.setStatusName(cartProductStatus.getStatusName().name());
        responseDto.setCreatedAt(cartProductStatus.getCreatedAt());
        responseDto.setUpdatedAt(cartProductStatus.getUpdatedAt());
        return responseDto;
    }
}
