package com.tarbonicar.backend.api.passwordReset.service;

import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetDTO;
import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetConfirmDTO;
import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetRequestDTO;
import com.tarbonicar.backend.api.passwordReset.entity.PasswordReset;
import com.tarbonicar.backend.api.passwordReset.repository.PasswordResetRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    @Value("${spring.mail.username}")
    private String serviceEmail;

    private final JavaMailSender mailSender;
    private final MemberRepository memberRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;

    // 인증 코드 발송
    public void requestPasswordResetCode(PasswordResetRequestDTO dto) {
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));

        String resetCode = generateRandomCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        PasswordReset pr = PasswordReset.builder()
                .email(dto.getEmail())
                .code(resetCode)
                .expirationTime(expirationTime)
                .isVerified(false)
                .build();
        passwordResetRepository.save(pr);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(String.format("타보니까 <%s>", serviceEmail));
        msg.setTo(dto.getEmail());
        msg.setSubject("타보니까 비밀번호 재설정 링크");
        msg.setText("비밀번호를 재설정하려면 아래 링크를 클릭하세요:\n\n"
                + "https://www.tarbonicar.kro.kr/reset-password-confirm?code=" + resetCode
                + "\n\n(5분간 유효)");
        mailSender.send(msg);
    }

    // 인증 코드 검증
    @Transactional
    public void resetVerifyPasswordResetCode(PasswordResetConfirmDTO dto) {
        PasswordReset pr = passwordResetRepository.findByCode(dto.getCode())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.INVALID_PASSWORD_RESET_CODE_EXCEPTION.getMessage()));

        if (pr.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorStatus.EXPIRED_PASSWORD_RESET_CODE_EXCEPTION.getMessage());
        }

        pr.markVerified();
        passwordResetRepository.save(pr);
    }

    // 비밀번호 초기화
    @Transactional
    public void resetPassword(PasswordResetDTO dto) {
        PasswordReset pr = passwordResetRepository.findByCode(dto.getCode())
                .orElseThrow(() -> new BadRequestException(ErrorStatus.INVALID_PASSWORD_RESET_CODE_EXCEPTION.getMessage()));

        if (!pr.isVerified()) {
            throw new BadRequestException(ErrorStatus.UNVERIFIED_PASSWORD_RESET_CODE_EXCEPTION.getMessage());
        }
        if (pr.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException(ErrorStatus.EXPIRED_PASSWORD_RESET_CODE_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(pr.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));

        String encoded = passwordEncoder.encode(dto.getNewPassword());

        member.updatePassword(encoded);
        memberRepository.save(member);

        passwordResetRepository.delete(pr);
    }

    // 랜덤 코드 생성
    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
