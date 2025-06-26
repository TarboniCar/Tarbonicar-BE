package com.tarbonicar.backend.api.member.controller;

import com.tarbonicar.backend.api.jwt.JwtProvider;
import com.tarbonicar.backend.api.member.dto.*;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.api.member.service.MemberService;
import com.tarbonicar.backend.api.member.service.OAuthService;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Tag(name="Member", description = "Member 관련 API 입니다.")
public class MemberController {

    private final MemberService memberService;
    private final OAuthService oAuthService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "이메일 회원가입 API", description = "회원정보를 받아 사용자를 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.success_only(SuccessStatus.SEND_REGISTER_SUCCESS);
    }

    // 카카오 인가코드로 액세스토큰 발급
    @Operation(summary = "카카오 AccessToken 발급 API", description = "카카오 인가 코드를 사용하여 AccessToken을 발급받습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "AccessToken 발급 성공")
    })
    @GetMapping("/kakao-accesstoken")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAccessToken(@RequestParam("code") String code) {
        String token = oAuthService.getKakaoAccessToken(code);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_ACCESS_TOKEN_SUCCESS, Map.of("accessToken", token));
    }

    // 카카오 액세스토큰으로 로그인
    @Operation(summary = "카카오 로그인 API", description = "카카오 AccessToken으로 사용자 정보를 조회하고 회원가입 또는 로그인을 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카카오 로그인 성공")
    })
    @PostMapping("/kakao-login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        Map<String, Object> result = memberService.kakaoLogin(token);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_LOGIN_SUCCESS, result);
    }

    @Operation(summary = "로그인 API", description = "이메일로 로그인을 처리합니다.")
    @PostMapping("/login")
    public  ResponseEntity<?> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        MemberLoginResponseDto memberLoginResponseDto = memberService.login(memberLoginRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, memberLoginResponseDto);
    }

    @Operation(summary = "사용자 정보 조회 API", description = "사용자 정보를 조회합니다.")
    @GetMapping("/user-info")
    public ResponseEntity<?> getMemberInfo(@AuthenticationPrincipal User principal) {

        Member member = memberService.getMemberInfo(principal.getUsername());
        MemberResponseDto memberResponseDto = MemberResponseDto.of(member);
        return  ApiResponse.success(SuccessStatus.SEND_LOGIN_SUCCESS, memberResponseDto);
    }

    @Operation(summary = "이메일 중복 확인 API", description = "입력한 이메일이 이미 가입된 이메일인지 확인합니다.")
    @GetMapping("/email-check")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = memberService.isEmailDuplicate(email);
        return ApiResponse.success(SuccessStatus.CHECK_EMAIL_SUCCESS, isDuplicate);
    }

    // 마이페이지 닉네임 변경
    @Operation(summary = "닉네임 변경 API", description = "사용자의 닉네임을 수정합니다.")
    @PutMapping("/nickname")
    public ResponseEntity<ApiResponse<Void>> updateNickname(@Valid @RequestBody NicknameUpdateRequestDto requestDto) {
        String newNickname = requestDto.getNickname();
        // JWT에서 인증된 사용자 이메일 추출
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        // 닉네임 변경 서비스 호출
        memberService.updateNickname(userName, newNickname);

        // 디버깅용 로그 출력
        System.out.println("🔧 닉네임 변경 요청 들어옴");
        System.out.println("📨 유저네임: " + userName);
        System.out.println("📝 변경할 닉네임: " + newNickname);

        return ApiResponse.success(SuccessStatus.UPDATE_NICKNAME_SUCCESS, null);
    }


    // 마이페이지 비밀번호 변경
    @Operation(summary = "비밀번호 변경 API", description = "사용자의 비밀번호를 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody @Valid PasswordUpdateRequestDto requestDto) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.updatePassword(userEmail, requestDto);
        return ApiResponse.success(SuccessStatus.UPDATE_PASSWORD_SUCCESS, null);
    }

    @Operation(summary = "프로필 이미지 업로드 API", description = "파일을 업로드하고 S3 URL을 반환합니다.")
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, String>>> updateProfileImage(
            @RequestPart("file") MultipartFile file
    ) {
        String email = getCurrentUserEmail(); // 또는 임의로 "test@example.com"
        String imageUrl = memberService.updateProfileImage(email, file);

        return ApiResponse.success(SuccessStatus.UPDATE_PROFILE_IMAGE_SUCCESS, Map.of("imageUrl", imageUrl));
    }

    @Operation(summary = "회원 탈퇴 API", description = "현재 로그인한 회원을 탈퇴 처리합니다.")
    @DeleteMapping("/delete")
    public
    ResponseEntity<ApiResponse<Void>> deleteMember() {
        String email = getCurrentUserEmail(); // 여기서 SecurityContext에서 추출
        memberService.deleteMember(email);
        return ApiResponse.success_only(SuccessStatus.DELETE_MEMBER_SUCCESS);
    }

    // 현재 로그인된 사용자의 email 추출
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // JWT 인증 시 이메일이 principal로 저장되어 있어야 함
    }

    @Operation(summary = "회원 정보 확인", description = "테스트용으로 현재 사용자 정보 반환")
    @GetMapping("/info")
    public ResponseEntity<Member> getMemberInfo() {
        String email = getCurrentUserEmail(); // 하드코딩된 이메일 사용
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("사용자 없음"));
        return ResponseEntity.ok(member);
    }


    @Operation(summary = "토큰 재발급", description = "refreshToken을 이용해서 accessToken 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponseDto>> reissue(@RequestBody TokenRequestDto tokenRequestDto){

        TokenResponseDto tokenResponseDto = memberService.reissueToken(tokenRequestDto);
        return ApiResponse.success(SuccessStatus.SEND_TOKEN_REISSUE_SUCCESS, tokenResponseDto);
    }
}
