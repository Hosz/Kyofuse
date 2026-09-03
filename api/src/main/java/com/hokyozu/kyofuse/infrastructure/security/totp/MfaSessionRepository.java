package com.hokyozu.kyofuse.infrastructure.security.totp;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfaSessionRepository extends CrudRepository<MfaSession, String> {
}
