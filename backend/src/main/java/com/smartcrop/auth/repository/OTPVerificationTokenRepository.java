package com.smartcrop.auth.repository;

import com.smartcrop.auth.entity.OTPVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPVerificationTokenRepository extends JpaRepository<OTPVerificationToken, Long> {
    Optional<OTPVerificationToken> findByOtp(String otp);
}