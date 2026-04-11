package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.AddressRequest;
import com.yafd.accountservice.dto.AddressResponse;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.entity.Address;
import com.yafd.accountservice.enums.AccountRole;
import com.yafd.accountservice.repository.AccountRepository;
import com.yafd.accountservice.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AddressService addressService;

    // -------------------------------------------------------------------------
    // addAddress()
    // -------------------------------------------------------------------------

    @Test
    void addAddress_shouldSaveAndReturnAddress() {
        Account account = buildAccount(1L, "uid-001");
        when(accountRepository.findByFirebaseUid("uid-001")).thenReturn(Optional.of(account));

        Address saved = buildAddress(1L, account, "Home", "123 Main St", "Singapore", "123456");
        when(addressRepository.save(any(Address.class))).thenReturn(saved);

        AddressRequest request = buildAddressRequest("Home", "123 Main St", "Singapore", "123456");
        AddressResponse response = addressService.addAddress("uid-001", request);

        assertThat(response.getLabel()).isEqualTo("Home");
        assertThat(response.getStreet()).isEqualTo("123 Main St");
        assertThat(response.getCity()).isEqualTo("Singapore");
        assertThat(response.getPostalCode()).isEqualTo("123456");
    }

    @Test
    void addAddress_shouldThrowException_whenAccountNotFound() {
        when(accountRepository.findByFirebaseUid("uid-unknown")).thenReturn(Optional.empty());

        AddressRequest request = buildAddressRequest("Home", "123 Main St", "Singapore", "123456");

        assertThatThrownBy(() -> addressService.addAddress("uid-unknown", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found");
    }

    // -------------------------------------------------------------------------
    // getAddresses()
    // -------------------------------------------------------------------------

    @Test
    void getAddresses_shouldReturnAllAddressesForAccount() {
        Account account = buildAccount(1L, "uid-001");
        when(accountRepository.findByFirebaseUid("uid-001")).thenReturn(Optional.of(account));

        List<Address> addresses = List.of(
                buildAddress(1L, account, "Home", "123 Main St", "Singapore", "123456"),
                buildAddress(2L, account, "Work", "456 Office Rd", "Singapore", "654321")
        );
        when(addressRepository.findByAccountId(1L)).thenReturn(addresses);

        List<AddressResponse> result = addressService.getAddresses("uid-001");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AddressResponse::getLabel).containsExactly("Home", "Work");
    }

    @Test
    void getAddresses_shouldThrowException_whenAccountNotFound() {
        when(accountRepository.findByFirebaseUid("uid-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddresses("uid-unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Account not found");
    }

    // -------------------------------------------------------------------------
    // getAddress()
    // -------------------------------------------------------------------------

    @Test
    void getAddress_shouldReturnAddress_whenFound() {
        Account account = buildAccount(1L, "uid-001");
        Address address = buildAddress(1L, account, "Home", "123 Main St", "Singapore", "123456");
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        AddressResponse response = addressService.getAddress("uid-001", 1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getLabel()).isEqualTo("Home");
    }

    @Test
    void getAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress("uid-001", 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Address not found");
    }

    // -------------------------------------------------------------------------
    // updateAddress()
    // -------------------------------------------------------------------------

    @Test
    void updateAddress_shouldUpdateFieldsWhenProvided() {
        Account account = buildAccount(1L, "uid-001");
        Address existing = buildAddress(1L, account, "Home", "123 Main St", "Singapore", "123456");
        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));

        Address updated = buildAddress(1L, account, "New Home", "789 New Rd", "Singapore", "999999");
        when(addressRepository.save(any(Address.class))).thenReturn(updated);

        AddressRequest request = buildAddressRequest("New Home", "789 New Rd", "Singapore", "999999");
        AddressResponse response = addressService.updateAddress("uid-001", 1L, request);

        assertThat(response.getLabel()).isEqualTo("New Home");
        assertThat(response.getStreet()).isEqualTo("789 New Rd");
        assertThat(response.getPostalCode()).isEqualTo("999999");
    }

    // -------------------------------------------------------------------------
    // deleteAddress()
    // -------------------------------------------------------------------------

    @Test
    void deleteAddress_shouldCallRepositoryDeleteById() {
        addressService.deleteAddress("uid-001", 1L);

        verify(addressRepository).deleteById(1L);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Account buildAccount(Long id, String firebaseUid) {
        return Account.builder()
                .id(id)
                .firebaseUid(firebaseUid)
                .email("test@example.com")
                .name("Test User")
                .role(AccountRole.CUSTOMER)
                .build();
    }

    private Address buildAddress(Long id, Account account, String label, String street, String city, String postalCode) {
        return Address.builder()
                .id(id)
                .account(account)
                .label(label)
                .street(street)
                .city(city)
                .postalCode(postalCode)
                .isDefault(false)
                .build();
    }

    private AddressRequest buildAddressRequest(String label, String street, String city, String postalCode) {
        AddressRequest request = new AddressRequest();
        request.setLabel(label);
        request.setStreet(street);
        request.setCity(city);
        request.setPostalCode(postalCode);
        return request;
    }
}
