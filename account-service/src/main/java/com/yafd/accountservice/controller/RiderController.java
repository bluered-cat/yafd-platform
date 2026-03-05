package com.yafd.accountservice.controller;

import com.yafd.accountservice.dto.RiderResponse;
import com.yafd.accountservice.service.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @GetMapping("/available")
    public ResponseEntity<RiderResponse> getAvailableRider() {
        return ResponseEntity.ok(riderService.getAvailableRider());
    }

    @PutMapping("/{riderId}/availability")
    public ResponseEntity<RiderResponse> updateAvailability(
            @PathVariable Long riderId,
            @RequestBody Map<String, Boolean> body) {
        Boolean available = body.get("available");
        return ResponseEntity.ok(riderService.updateAvailability(riderId, available));
    }
}
