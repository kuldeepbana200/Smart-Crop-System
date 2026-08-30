package com.smartcrop.auth.repository;

import com.smartcrop.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = false WHERE u.phoneVerified IS NULL")
    void updatePhoneVerifiedNullToFalse();

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.preferredLanguage = 'en' WHERE u.preferredLanguage IS NULL")
    void updatePreferredLanguageNullToEn();
}