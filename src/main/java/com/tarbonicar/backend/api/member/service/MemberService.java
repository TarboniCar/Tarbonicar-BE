package com.tarbonicar.backend.api.member.service;

import com.tarbonicar.backend.api.jwt.JwtProvider;
import com.tarbonicar.backend.api.member.dto.KakaoUserInfoDto;
import com.tarbonicar.backend.api.member.dto.MemberLoginRequestDto;
import com.tarbonicar.backend.api.member.dto.MemberLoginResponseDto;
import com.tarbonicar.backend.api.member.dto.MemberSignupRequestDto;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import com.tarbonicar.backend.api.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuthService oAuthService;
    private final JwtProvider jwtProvider;

    // 이메일 회원가입 메서드
    @Transactional
    public void signup(MemberSignupRequestDto requestDto) {

        // 만약 이미 해당 이메일로 가입된 정보가 있다면 예외처리
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BadRequestException(ErrorStatus.ALREADY_EMAIL_EXIST_EXCEPTION.getMessage());
        }

        // 비밀번호랑 비밀번호 재확인 값이 다를 경우 예외처리
        if (!requestDto.getPassword().equals(requestDto.getCheckedPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String imageUrl = requestDto.getProfileImage();

        Member member = requestDto.toEntity(encodedPassword, imageUrl);
        memberRepository.save(member);
    }

    @Transactional
    public Map<String, Object> kakaoLogin(String kakaoAccessToken) {

        // 카카오 액세스 토큰이 null이거나 빈 문자열일 경우 예외 처리
        if (kakaoAccessToken == null || kakaoAccessToken.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }

        // 카카오 액세스 토큰을 사용해서 사용자 정보 가져오기
        KakaoUserInfoDto userInfo = oAuthService.getKakaoUserInfo(kakaoAccessToken);

        // 사용자 정보 저장
        Member member = memberRepository.findBySocialId(userInfo.getId())
                .orElseGet(() -> kakaoRegister(userInfo));  // 없으면 회원가입

        // 인증 객체 생성 (비밀번호 없이 Social 인증 사용자용)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                member.getEmail(), null,
                List.of(() -> "ROLE_USER")
        );

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        // 로그인 시 응답 데이터 구성
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);

        return result;
    }

    // 새 유저 회원가입 처리
    private Member kakaoRegister(KakaoUserInfoDto dto) {
        Member member = Member.builder()
                .socialId(dto.getId())
                .email("kakao_" + dto.getId() + "@social.com")
                .nickname(dto.getNickname())
                .profileImage(dto.getProfileImage())
                .socialType("KAKAO")
                .build();

        return memberRepository.save(member);
    }

    @Transactional
    public MemberLoginResponseDto login(MemberLoginRequestDto memberLoginRequestDto) {

        // 회원 조회
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));

        // 비밀번호 일치 확인
        if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }
        // 인증 객체 생성 (Spring Security용)
        Authentication authentication = memberLoginRequestDto.toAuthentication();

        // JWT 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        return new MemberLoginResponseDto(accessToken, refreshToken);
    }

    public Member getMemberInfo(String email) {

        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));
        return member;
    }


}
