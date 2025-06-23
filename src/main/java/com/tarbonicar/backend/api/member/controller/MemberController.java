package com.tarbonicar.backend.api.member.controller;

import com.tarbonicar.backend.api.jwt.JwtProvider;
import com.tarbonicar.backend.api.member.dto.MemberLoginRequestDto;
import com.tarbonicar.backend.api.member.dto.MemberLoginResponseDto;
import com.tarbonicar.backend.api.member.dto.MemberResponseDto;
import com.tarbonicar.backend.api.member.dto.MemberSignupRequestDto;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.api.member.service.MemberService;
import com.tarbonicar.backend.api.member.service.OAuthService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

}
