package com.yafd.accountservice.entity;

import com.yafd.accountservice.enums.AccountRole;
import com.yafd.accountservice.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    private String firebaseUid;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", length = 30)
    private VehicleType vehicleType;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
