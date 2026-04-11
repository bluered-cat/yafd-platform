package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.AccountResponse;
import com.yafd.accountservice.dto.RegisterAccountRequest;
import com.yafd.accountservice.dto.UpdateAccountRequest;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.enums.AccountRole;
import com.yafd.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    // -------------------------------------------------------------------------
    // register()
    // -------------------------------------------------------------------------

    @Test
    void register_shouldCreateAccountWithCorrectFields() {
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .firebaseUid("uid-001")
                .email("customer@example.com")
                .name("Ali Hassan")
                .phone("91234567")
                .role("CUSTOMER")
                .build();

        Account saved = Account.builder()
                .id(1L)
                .firebaseUid("uid-001")
                .email("customer@example.com")
                .name("Ali Hassan")
                .phone("91234567")
                .role(AccountRole.CUSTOMER)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        AccountResponse response = accountService.register(request);

        assertThat(response.getFirebaseUid()).isEqualTo("uid-001");
        assertThat(response.getEmail()).isEqualTo("customer@example.com");
        assertThat(response.getName()).isEqualTo("Ali Hassan");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void register_shouldSetIsAvailableTrue_whenRoleIsRider() {
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .firebaseUid("uid-rider-001")
                .email("rider@example.com")
                .name("Ahmad Rider")
                .role("RIDER")
                .vehicleType("MOTORCYCLE")
                .build();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        Account saved = Account.builder()
                .id(2L)
                .firebaseUid("uid-rider-001")
                .role(AccountRole.RIDER)
                .isAvailable(true)
                .build();
        when(accountRepository.save(captor.capture())).thenReturn(saved);

        accountService.register(request);

        Account capturedAccount = captor.getValue();
        assertThat(capturedAccount.getIsAvailable()).isTrue();
    }

    @Test
    void register_shouldSetIsAvailableNull_whenRoleIsCustomer() {
        RegisterAccountRequest request = RegisterAccountRequest.builder()
                .firebaseUid("uid-002")
                .email("customer2@example.com")
                .name("Siti Rahimah")
                .role("CUSTOMER")
                .build();

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        Account saved = Account.builder().id(3L).role(AccountRole.CUSTOMER).isAvailable(null).build();
        when(accountRepository.save(captor.capture())).thenReturn(saved);

        accountService.register(request);

        Account capturedAccount = captor.getValue();
        assertThat(capturedAccount.getIsAvailable()).isNull();
    }

    // -------------------------------------------------------------------------
    // getByFirebaseUid()
    // -------------------------------------------------------------------------

    @Test
    void getByFirebaseUid_shouldReturnAccountResponse_whenAccountExists() {
        Account account = Account.builder()
                .id(1L)
                .firebaseUid("uid-001")
                .email("customer@example.com")
                .name("Ali Hassan")
                .role(AccountRole.CUSTOMER)
                .build();
        when(accountRepository.findByFirebaseUid("uid-001")).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getByFirebaseUid("uid-001");

        assertThat(response.getFirebaseUid()).isEqualTo("uid-001");
        assertThat(response.getName()).isEqualTo("Ali Hassan");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void getByFirebaseUid_shouldThrowException_whenAccountNotFound() {
        when(accountRepository.findByFirebaseUid("uid-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getByFirebaseUid("uid-unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found");
    }

    // -------------------------------------------------------------------------
    // update()
    // -------------------------------------------------------------------------

    @Test
    void update_shouldUpdateNamePhoneAndEmail_whenFieldsProvided() {
        Account existing = Account.builder()
                .id(1L)
                .firebaseUid("uid-001")
                .name("Old Name")
                .email("old@example.com")
                .phone("90000000")
                .role(AccountRole.CUSTOMER)
                .build();

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setName("New Name");
        request.setPhone("91111111");
        request.setEmail("new@example.com");

        Account updated = Account.builder()
                .id(1L)
                .firebaseUid("uid-001")
                .name("New Name")
                .email("new@example.com")
                .phone("91111111")
                .role(AccountRole.CUSTOMER)
                .build();

        when(accountRepository.findByFirebaseUid("uid-001")).thenReturn(Optional.of(existing));
        when(accountRepository.save(any(Account.class))).thenReturn(updated);

        AccountResponse response = accountService.update("uid-001", request);

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getPhone()).isEqualTo("91111111");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_shouldThrowException_whenAccountNotFound() {
        when(accountRepository.findByFirebaseUid("uid-unknown")).thenReturn(Optional.empty());

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setName("Some Name");

        assertThatThrownBy(() -> accountService.update("uid-unknown", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found");
    }
}
