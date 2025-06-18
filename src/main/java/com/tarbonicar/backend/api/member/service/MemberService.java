package com.tarbonicar.backend.api.member.service;

import com.tarbonicar.backend.api.member.dto.MemberSignupRequestDto;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(MemberSignupRequestDto requestDto) {
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_EMAIL_EXIST_EXCEPTION.getMessage());
        }

        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        Member member = requestDto.toEntity(encodedPassword, null);
        memberRepository.save(member);
    }
}
