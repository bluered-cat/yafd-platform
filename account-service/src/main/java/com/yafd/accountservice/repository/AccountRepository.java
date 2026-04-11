package com.yafd.accountservice.repository;

import com.yafd.accountservice.entity.Account;
import com.yafd.accountservice.enums.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByFirebaseUid(String firebaseUid);
    List<Account> findByRoleAndIsAvailableTrue(AccountRole role);
}
