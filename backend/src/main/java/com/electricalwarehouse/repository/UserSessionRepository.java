package com.electricalwarehouse.repository;

import com.electricalwarehouse.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByRefreshTokenHashAndRevokedFalse(String refreshTokenHash);

    @Modifying
    @Query("update UserSession s set s.revoked = true where s.user.id = :userId and s.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);
}
