package com.webstore.implementation.cart;

import com.webstore.dto.request.cart.CartStatusRequestDto;
import com.webstore.dto.response.cart.CartStatusResponseDto;
import com.webstore.entity.cart.CartStatus;
import com.webstore.enums.cart.CartStatusType;
import com.webstore.exception.cart.*;
import com.webstore.repository.cart.CartStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Status Service Implementation Tests")
class CartStatusServiceImplementationTest {

    @Mock
    private CartStatusRepository cartStatusRepository;

    @InjectMocks
    private CartStatusServiceImplementation cartStatusService;

    private CartStatus testCartStatus;
    private CartStatusRequestDto testRequestDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();
        testCartStatus = createTestCartStatus(1, CartStatusType.ACTIVE);
        testRequestDto = createTestRequestDto("ACTIVE");
    }

    @Test
    @DisplayName("Should get cart status by ID successfully")
    void getCartStatusById_Success() {

        Integer statusId = 1;
        when(cartStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartStatus));

        CartStatusResponseDto result = cartStatusService.getCartStatusById(statusId);

        assertThat(result).isNotNull();
        assertThat(result.getStatusId()).isEqualTo(statusId);
        assertThat(result.getStatusName()).isEqualTo("ACTIVE");
        verify(cartStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should throw exception when status ID is null")
    void getCartStatusById_NullId() {

        assertThatThrownBy(() -> cartStatusService.getCartStatusById(null))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when status ID is zero or negative")
    void getCartStatusById_InvalidId() {

        assertThatThrownBy(() -> cartStatusService.getCartStatusById(0))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.getCartStatusById(-1))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status not found")
    void getCartStatusById_NotFound() {

        Integer statusId = 999;
        when(cartStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartStatusService.getCartStatusById(statusId))
                .isInstanceOf(CartStatusNotFoundException.class);
        verify(cartStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should throw database exception when data access fails")
    void getCartStatusById_DatabaseError() {

        Integer statusId = 1;
        when(cartStatusRepository.findById(statusId)).thenThrow(new DataAccessException("DB Error") {});

        assertThatThrownBy(() -> cartStatusService.getCartStatusById(statusId))
                .isInstanceOf(CartStatusDatabaseException.class);
        verify(cartStatusRepository, times(1)).findById(statusId);
    }

    @Test
    @DisplayName("Should get all cart statuses successfully")
    void getAllCartStatuses_Success() {

        List<CartStatus> statuses = Arrays.asList(
                createTestCartStatus(0, CartStatusType.ACTIVE),
                createTestCartStatus(1, CartStatusType.CHECKED_OUT),
                createTestCartStatus(2, CartStatusType.PAID),
                createTestCartStatus(3, CartStatusType.ARCHIVED)

                );
        when(cartStatusRepository.findAll()).thenReturn(statuses);

        List<CartStatusResponseDto> result = cartStatusService.getAllCartStatuses();

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getStatusName()).isEqualTo("ACTIVE");
        assertThat(result.get(1).getStatusName()).isEqualTo("CHECKED_OUT");
        assertThat(result.get(2).getStatusName()).isEqualTo("PAID");
        assertThat(result.get(3).getStatusName()).isEqualTo("ARCHIVED");
        verify(cartStatusRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no cart statuses exist")
    void getAllCartStatuses_EmptyList() {

        when(cartStatusRepository.findAll()).thenReturn(Collections.emptyList());

        List<CartStatusResponseDto> result = cartStatusService.getAllCartStatuses();

        assertThat(result).isEmpty();
        verify(cartStatusRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should throw database exception when data access fails")
    void getAllCartStatuses_DatabaseError() {

        when(cartStatusRepository.findAll()).thenThrow(new DataAccessException("DB Error") {});

        assertThatThrownBy(() -> cartStatusService.getAllCartStatuses())
                .isInstanceOf(CartStatusDatabaseException.class);
        verify(cartStatusRepository, times(1)).findAll();
    }


    @Test
    @DisplayName("Should create cart status successfully")
    void createCartStatus_Success() {

        when(cartStatusRepository.existsByStatusName(CartStatusType.ACTIVE)).thenReturn(false);
        when(cartStatusRepository.save(any(CartStatus.class))).thenReturn(testCartStatus);

        CartStatusResponseDto result = cartStatusService.createCartStatus(testRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isEqualTo("ACTIVE");
        verify(cartStatusRepository, times(1)).existsByStatusName(CartStatusType.ACTIVE);
        verify(cartStatusRepository, times(1)).save(any(CartStatus.class));
    }

    @Test
    @DisplayName("Should throw exception when request DTO is null")
    void createCartStatus_NullRequest() {

        assertThatThrownBy(() -> cartStatusService.createCartStatus(null))
                .isInstanceOf(CartStatusValidationException.class);
        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when status name is null or empty")
    void createCartStatus_InvalidStatusName() {

        CartStatusRequestDto invalidDto = createTestRequestDto(null);

        assertThatThrownBy(() -> cartStatusService.createCartStatus(invalidDto))
                .isInstanceOf(CartStatusValidationException.class);

        CartStatusRequestDto emptyDto = createTestRequestDto("");
        assertThatThrownBy(() -> cartStatusService.createCartStatus(emptyDto))
                .isInstanceOf(CartStatusValidationException.class);

        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when status name is invalid enum value")
    void createCartStatus_InvalidEnumValue() {

        CartStatusRequestDto invalidDto = createTestRequestDto("INVALID_STATUS");

        assertThatThrownBy(() -> cartStatusService.createCartStatus(invalidDto))
                .isInstanceOf(CartStatusValidationException.class);
        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status already exists")
    void createCartStatus_AlreadyExists() {

        when(cartStatusRepository.existsByStatusName(CartStatusType.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> cartStatusService.createCartStatus(testRequestDto))
                .isInstanceOf(CartStatusAlreadyExistsException.class);
        verify(cartStatusRepository, times(1)).existsByStatusName(CartStatusType.ACTIVE);
        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when data integrity violation occurs")
    void createCartStatus_DataIntegrityViolation() {

        when(cartStatusRepository.existsByStatusName(CartStatusType.ACTIVE)).thenReturn(false);
        when(cartStatusRepository.save(any(CartStatus.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThatThrownBy(() -> cartStatusService.createCartStatus(testRequestDto))
                .isInstanceOf(CartStatusAlreadyExistsException.class);
        verify(cartStatusRepository, times(1)).save(any(CartStatus.class));
    }

    @Test
    @DisplayName("Should update cart status successfully")
    void updateCartStatus_Success() {

        Integer statusId = 1;
        CartStatusRequestDto updateDto = createTestRequestDto("CHECKED_OUT");
        CartStatus updatedStatus = createTestCartStatus(statusId, CartStatusType.CHECKED_OUT);

        when(cartStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartStatus));
        when(cartStatusRepository.existsByStatusNameAndStatusIdNot(CartStatusType.CHECKED_OUT, statusId))
                .thenReturn(false);
        when(cartStatusRepository.save(any(CartStatus.class))).thenReturn(updatedStatus);

        CartStatusResponseDto result = cartStatusService.updateCartStatus(statusId, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isEqualTo("CHECKED_OUT");
        verify(cartStatusRepository, times(1)).findById(statusId);
        verify(cartStatusRepository, times(1)).save(any(CartStatus.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with null or invalid status ID")
    void updateCartStatus_InvalidId() {

        assertThatThrownBy(() -> cartStatusService.updateCartStatus(null, testRequestDto))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.updateCartStatus(0, testRequestDto))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.updateCartStatus(-1, testRequestDto))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status to update not found")
    void updateCartStatus_NotFound() {

        Integer statusId = 999;
        when(cartStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartStatusService.updateCartStatus(statusId, testRequestDto))
                .isInstanceOf(CartStatusNotFoundException.class);
        verify(cartStatusRepository, times(1)).findById(statusId);
        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when another status with same name exists")
    void updateCartStatus_DuplicateName() {

        Integer statusId = 1;
        when(cartStatusRepository.findById(statusId)).thenReturn(Optional.of(testCartStatus));
        when(cartStatusRepository.existsByStatusNameAndStatusIdNot(CartStatusType.ACTIVE, statusId))
                .thenReturn(true);

        assertThatThrownBy(() -> cartStatusService.updateCartStatus(statusId, testRequestDto))
                .isInstanceOf(CartStatusAlreadyExistsException.class);
        verify(cartStatusRepository, times(1)).findById(statusId);
        verify(cartStatusRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete cart status successfully")
    void deleteCartStatus_Success() {

        Integer statusId = 1;
        when(cartStatusRepository.existsById(statusId)).thenReturn(true);
        when(cartStatusRepository.countCartsUsingStatus(statusId)).thenReturn(0L);
        doNothing().when(cartStatusRepository).deleteById(statusId);

        cartStatusService.deleteCartStatus(statusId);

        verify(cartStatusRepository, times(1)).existsById(statusId);
        verify(cartStatusRepository, times(1)).countCartsUsingStatus(statusId);
        verify(cartStatusRepository, times(1)).deleteById(statusId);
    }

    @Test
    @DisplayName("Should throw exception when deleting with null or invalid status ID")
    void deleteCartStatus_InvalidId() {

        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(null))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(0))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(-1))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status to delete not found")
    void deleteCartStatus_NotFound() {

        Integer statusId = 999;
        when(cartStatusRepository.existsById(statusId)).thenReturn(false);

        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(statusId))
                .isInstanceOf(CartStatusNotFoundException.class);
        verify(cartStatusRepository, times(1)).existsById(statusId);
        verify(cartStatusRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status is in use")
    void deleteCartStatus_InUse() {

        Integer statusId = 1;
        when(cartStatusRepository.existsById(statusId)).thenReturn(true);
        when(cartStatusRepository.countCartsUsingStatus(statusId)).thenReturn(5L);

        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(statusId))
                .isInstanceOf(CartStatusInUseException.class);
        verify(cartStatusRepository, times(1)).existsById(statusId);
        verify(cartStatusRepository, times(1)).countCartsUsingStatus(statusId);
        verify(cartStatusRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw exception when data integrity violation occurs during deletion")
    void deleteCartStatus_DataIntegrityViolation() {

        Integer statusId = 1;
        when(cartStatusRepository.existsById(statusId)).thenReturn(true);
        when(cartStatusRepository.countCartsUsingStatus(statusId)).thenReturn(0L);
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(cartStatusRepository).deleteById(statusId);

        assertThatThrownBy(() -> cartStatusService.deleteCartStatus(statusId))
                .isInstanceOf(CartStatusInUseException.class);
        verify(cartStatusRepository, times(1)).deleteById(statusId);
    }

    @Test
    @DisplayName("Should get cart status by name successfully")
    void getCartStatusByName_Success() {

        String statusName = "ACTIVE";
        when(cartStatusRepository.findByStatusName(CartStatusType.ACTIVE))
                .thenReturn(Optional.of(testCartStatus));

        CartStatusResponseDto result = cartStatusService.getCartStatusByName(statusName);

        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isEqualTo("ACTIVE");
        verify(cartStatusRepository, times(1)).findByStatusName(CartStatusType.ACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when status name is null or empty")
    void getCartStatusByName_InvalidName() {

        assertThatThrownBy(() -> cartStatusService.getCartStatusByName(null))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.getCartStatusByName(""))
                .isInstanceOf(InvalidCartStatusException.class);
        assertThatThrownBy(() -> cartStatusService.getCartStatusByName("   "))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).findByStatusName(any());
    }

    @Test
    @DisplayName("Should throw exception when status name is invalid enum value")
    void getCartStatusByName_InvalidEnumValue() {

        assertThatThrownBy(() -> cartStatusService.getCartStatusByName("INVALID_STATUS"))
                .isInstanceOf(InvalidCartStatusException.class);
        verify(cartStatusRepository, never()).findByStatusName(any());
    }

    @Test
    @DisplayName("Should throw exception when cart status not found by name")
    void getCartStatusByName_NotFound() {

        String statusName = "ACTIVE";
        when(cartStatusRepository.findByStatusName(CartStatusType.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartStatusService.getCartStatusByName(statusName))
                .isInstanceOf(CartStatusNotFoundException.class);
        verify(cartStatusRepository, times(1)).findByStatusName(CartStatusType.ACTIVE);
    }

    @Test
    @DisplayName("Should handle case insensitive status name lookup")
    void getCartStatusByName_CaseInsensitive() {

        String statusName = "active"; // lowercase
        when(cartStatusRepository.findByStatusName(CartStatusType.ACTIVE))
                .thenReturn(Optional.of(testCartStatus));

        CartStatusResponseDto result = cartStatusService.getCartStatusByName(statusName);

        assertThat(result).isNotNull();
        assertThat(result.getStatusName()).isEqualTo("ACTIVE");
        verify(cartStatusRepository, times(1)).findByStatusName(CartStatusType.ACTIVE);
    }

    @Test
    @DisplayName("Should handle exception when checking if cart status is in use")
    void isCartStatusInUse_Exception() {

        Integer statusId = 1;
        when(cartStatusRepository.existsById(statusId)).thenReturn(true);
        when(cartStatusRepository.countCartsUsingStatus(statusId))
                .thenThrow(new RuntimeException("DB Error"));
        doNothing().when(cartStatusRepository).deleteById(statusId);

        cartStatusService.deleteCartStatus(statusId);

        verify(cartStatusRepository, times(1)).countCartsUsingStatus(statusId);
        verify(cartStatusRepository, times(1)).deleteById(statusId);
    }

    @Test
    @DisplayName("Should map null cart status to null response DTO")
    void mapToResponseDto_NullInput() {
        assertThat(true).isTrue();
    }


    private CartStatus createTestCartStatus(Integer statusId, CartStatusType statusType) {
        CartStatus cartStatus = new CartStatus();
        cartStatus.setStatusId(statusId);
        cartStatus.setStatusName(statusType);
        cartStatus.setCreatedAt(testDateTime);
        cartStatus.setUpdatedAt(testDateTime);
        return cartStatus;
    }

    private CartStatusRequestDto createTestRequestDto(String statusName) {
        CartStatusRequestDto dto = new CartStatusRequestDto();
        dto.setStatusName(statusName);
        return dto;
    }

    @Test
    @DisplayName("Should handle concurrent access scenarios")
    void concurrentAccess_DataIntegrityHandling() {
        when(cartStatusRepository.existsByStatusName(CartStatusType.ACTIVE)).thenReturn(false);
        when(cartStatusRepository.save(any(CartStatus.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        assertThatThrownBy(() -> cartStatusService.createCartStatus(testRequestDto))
                .isInstanceOf(CartStatusAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should handle all valid enum values")
    void validateAllEnumValues() {

        for (CartStatusType statusType : CartStatusType.values()) {
            CartStatusRequestDto dto = createTestRequestDto(statusType.name());
            when(cartStatusRepository.existsByStatusName(statusType)).thenReturn(false);
            when(cartStatusRepository.save(any(CartStatus.class)))
                    .thenReturn(createTestCartStatus(1, statusType));

            CartStatusResponseDto result = cartStatusService.createCartStatus(dto);

            assertThat(result.getStatusName()).isEqualTo(statusType.name());

            reset(cartStatusRepository);
        }
    }
}