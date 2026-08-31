package com.hokyozu.kyofuse.users.repository;

import com.hokyozu.kyofuse.users.entity.AccountSuccessionRecord;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountSuccessionRecordRepository extends JpaRepository<AccountSuccessionRecord, UUID> {
    List<AccountSuccessionRecord> findAllByUser(User user);
    void deleteAllByUser(User user);
}
