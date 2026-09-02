package com.hokyozu.kyofuse.auth.repository;

import com.hokyozu.kyofuse.auth.entity.AccountReactivationCode;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountReactivationCodeRepository extends JpaRepository<AccountReactivationCode, UUID> {

    Optional<AccountReactivationCode> findTopByUserOrderByCreatedAtDesc(User user);

    void deleteAllByUser(User user);
}
