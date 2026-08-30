package com.smartcrop.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification_tokens")
public class OTPVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String otp; // 6-digit OTP code

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private boolean verified;

    public OTPVerificationToken() {
    }

    public OTPVerificationToken(String otp, Long userId, LocalDateTime expiryDate) {
        this.otp = otp;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.verified = false;
    }

    public Long getId() {
        return id;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}