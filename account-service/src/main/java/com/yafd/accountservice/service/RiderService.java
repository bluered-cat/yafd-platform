package com.yafd.accountservice.service;

import com.yafd.accountservice.dto.RiderResponse;
import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.enums.AccountRole;
import com.yafd.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final AccountRepository accountRepository;

    public RiderResponse getAvailableRider() {
        List<Account> availableRiders = accountRepository.findByRoleAndIsAvailableTrue(AccountRole.RIDER);
        if (availableRiders.isEmpty()) {
            throw new RuntimeException("No available riders");
        }
        return toResponse(availableRiders.get(0));
    }

    @Transactional
    public RiderResponse updateAvailability(Long riderId, boolean available) {
        Account rider = accountRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("Rider not found: " + riderId));

        if (rider.getRole() != AccountRole.RIDER) {
            throw new RuntimeException("Account is not a rider: " + riderId);
        }

        rider.setIsAvailable(available);
        rider = accountRepository.save(rider);
        return toResponse(rider);
    }

    private RiderResponse toResponse(Account rider) {
        return RiderResponse.builder()
                .id(rider.getId())
                .name(rider.getName())
                .phone(rider.getPhone())
                .vehicleType(rider.getVehicleType() != null ? rider.getVehicleType().name() : null)
                .isAvailable(rider.getIsAvailable())
                .build();
    }
}
