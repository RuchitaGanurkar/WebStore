package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;
import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.repository.cart.CartStatusRepository;
import com.webstore.service.cart.CartProductStatusService;
import com.webstore.service.cart.CartStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class CartStatusServiceImplementation implements CartStatusService {

    private final CartStatusRepository cartStatusRepository;


    public CartStatusResponseDto getCartStatusById(Integer statusId) {
        CartStatus cartStatus = cartStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Cart status not found with id: " + statusId));
        return mapToResponseDto(cartStatus);
    }

    public List<CartStatusResponseDto> getAllCartStatuses() {
        List<CartStatus> statuses = cartStatusRepository.findAll();
        return statuses.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public CartStatusResponseDto updateCartStatus(Integer statusId, CartStatusRequestDto
            requestDto) {
        CartStatus cartStatus = cartStatusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Cart status not found with id: " + statusId));

        CartStatusType statusType = CartStatusType.valueOf(requestDto.getStatusName().toUpperCase());
        cartStatus.setStatusName(statusType);
        cartStatus.setUpdatedAt(LocalDateTime.now());

        CartStatus updatedStatus = cartStatusRepository.save(cartStatus);
        return mapToResponseDto(updatedStatus);
    }

    public void deleteCartStatus(Integer statusId) {
        if (!cartStatusRepository.existsById(statusId)) {
            throw new RuntimeException("Cart status not found with id: " + statusId);
        }
        cartStatusRepository.deleteById(statusId);
    }

    private CartStatusResponseDto mapToResponseDto(CartStatus cartStatus) {
        CartStatusResponseDto responseDto = new CartStatusResponseDto();
        responseDto.setStatusId(cartStatus.getStatusId());
        responseDto.setStatusName(cartStatus.getStatusName().name());
        responseDto.setCreatedAt(cartStatus.getCreatedAt());
        responseDto.setUpdatedAt(cartStatus.getUpdatedAt());
        return responseDto;
    }

}
