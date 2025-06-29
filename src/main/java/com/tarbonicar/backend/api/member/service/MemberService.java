package com.tarbonicar.backend.api.member.service;

import com.tarbonicar.backend.api.article.repository.ArticleLikeRepository;
import com.tarbonicar.backend.api.article.repository.ArticleRepository;
import com.tarbonicar.backend.api.article.service.ArticleService;
import com.tarbonicar.backend.api.aws.s3.service.S3Service;
import com.tarbonicar.backend.api.comment.repository.CommentRepository;
import com.tarbonicar.backend.api.jwt.JwtProvider;
import com.tarbonicar.backend.api.member.dto.*;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import com.tarbonicar.backend.api.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuthService oAuthService;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;

    private final ArticleLikeRepository articleLikeRepository;
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

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

        member.updateRefreshtoken(refreshToken);

        return new MemberLoginResponseDto(accessToken, refreshToken);
    }

    public Member getMemberInfo(String email) {

        Member member = memberRepository.findByEmail(email).orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBER_EXCEPTION.getMessage()));
        return member;
    }

    public boolean isEmailDuplicate(String email) {

        return memberRepository.existsByEmail(email.trim());
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(String email, String nickname) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.MEMBER_NOT_FOUND_EXCEPTION.getMessage()));
        member.setNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String email, PasswordUpdateRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException(ErrorStatus.PASSWORD_MISMATCH_EXCEPTION.getMessage());
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.MEMBER_NOT_FOUND_EXCEPTION.getMessage()));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        member.setPassword(encodedPassword);
    }

    // 프로필 이미지 변경
    @Transactional
    public String updateProfileImage(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("파일이 비어 있습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("회원 정보를 찾을 수 없습니다."));

        try {
            String imageUrl = s3Service.uploadImage(file); // 이 메서드가 실제 S3 업로드
            member.setProfileImage(imageUrl);
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    @Transactional
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(ErrorStatus.MEMBER_NOT_FOUND_EXCEPTION.getMessage()));

        commentRepository.deleteByMember(member);
        List<Long> likedArticleIds = articleLikeRepository.findLikedArticleIdsByMemberId(member.getId());
        if (!likedArticleIds.isEmpty()) {
            articleRepository.decreaseLikeCount(likedArticleIds);
        }
        articleLikeRepository.deleteByMemberId(member.getId());
        articleRepository.deleteByMember(member);

        // 마지막으로 회원 정보 삭제
        memberRepository.delete(member);
    }

    // 토큰 
    public TokenResponseDto reissueToken(TokenRequestDto tokenRequestDto){

        String refreshToken = tokenRequestDto.getRefreshToken();

        if(!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 RefreshToken");
        }

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("유효하지 않은 리프레시"));

        Authentication authentication = toAuthentication(member.getEmail(), member.getPassword());

        String newAccessToken = jwtProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtProvider.generateRefreshToken(member.getEmail());

        member.updateRefreshtoken(newRefreshToken);

        return new TokenResponseDto(newAccessToken,newRefreshToken);

    }

    public UsernamePasswordAuthenticationToken toAuthentication(String email, String password) {
        return new UsernamePasswordAuthenticationToken(email, password);
    }

}
