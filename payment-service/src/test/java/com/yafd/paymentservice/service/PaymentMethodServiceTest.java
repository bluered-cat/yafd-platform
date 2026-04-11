package com.yafd.paymentservice.service;

import com.yafd.paymentservice.dto.PaymentMethodRequest;
import com.yafd.paymentservice.dto.PaymentMethodResponse;
import com.yafd.paymentservice.entity.PaymentMethod;
import com.yafd.paymentservice.enums.PaymentMethodType;
import com.yafd.paymentservice.repository.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private PaymentMethodRepository repository;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    // -------------------------------------------------------------------------
    // addPaymentMethod()
    // -------------------------------------------------------------------------

    @Test
    void addPaymentMethod_shouldSaveWithCorrectFields() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setUserId("uid-001");
        request.setType("CREDIT_CARD");
        request.setLabel("Personal Card");
        request.setLastFour("4242");
        request.setIsDefault(true);

        PaymentMethod saved = buildMethod(1L, "uid-001", PaymentMethodType.CREDIT_CARD, "Personal Card", true);
        when(repository.save(any(PaymentMethod.class))).thenReturn(saved);

        PaymentMethodResponse response = paymentMethodService.addPaymentMethod(request);

        assertThat(response.getUserId()).isEqualTo("uid-001");
        assertThat(response.getType()).isEqualTo("CREDIT_CARD");
        assertThat(response.getLabel()).isEqualTo("Personal Card");
        assertThat(response.getIsDefault()).isTrue();
    }

    @Test
    void addPaymentMethod_shouldDefaultIsDefaultToFalse_whenNotProvided() {
        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setUserId("uid-001");
        request.setType("DEBIT_CARD");
        request.setLabel("Work Card");

        ArgumentCaptor<PaymentMethod> captor = ArgumentCaptor.forClass(PaymentMethod.class);
        PaymentMethod saved = buildMethod(2L, "uid-001", PaymentMethodType.DEBIT_CARD, "Work Card", false);
        when(repository.save(captor.capture())).thenReturn(saved);

        paymentMethodService.addPaymentMethod(request);

        assertThat(captor.getValue().getIsDefault()).isFalse();
    }

    // -------------------------------------------------------------------------
    // getByUserId()
    // -------------------------------------------------------------------------

    @Test
    void getByUserId_shouldReturnAllMethodsForUser() {
        List<PaymentMethod> methods = List.of(
                buildMethod(1L, "uid-001", PaymentMethodType.CREDIT_CARD, "Personal Card", true),
                buildMethod(2L, "uid-001", PaymentMethodType.E_WALLET, "GrabPay", false)
        );
        when(repository.findByUserId("uid-001")).thenReturn(methods);

        List<PaymentMethodResponse> result = paymentMethodService.getByUserId("uid-001");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(PaymentMethodResponse::getUserId).containsOnly("uid-001");
    }

    // -------------------------------------------------------------------------
    // getById()
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnMethod_whenFound() {
        PaymentMethod method = buildMethod(1L, "uid-001", PaymentMethodType.CREDIT_CARD, "Personal Card", true);
        when(repository.findById(1L)).thenReturn(Optional.of(method));

        PaymentMethodResponse response = paymentMethodService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLabel()).isEqualTo("Personal Card");
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.getById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment method not found");
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldCallRepositoryDelete_whenMethodExists() {
        when(repository.existsById(1L)).thenReturn(true);

        paymentMethodService.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenMethodNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> paymentMethodService.delete(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Payment method not found");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private PaymentMethod buildMethod(Long id, String userId, PaymentMethodType type, String label, boolean isDefault) {
        return PaymentMethod.builder()
                .id(id)
                .userId(userId)
                .type(type)
                .label(label)
                .isDefault(isDefault)
                .build();
    }
}
