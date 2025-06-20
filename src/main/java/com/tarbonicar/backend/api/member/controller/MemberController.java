package com.tarbonicar.backend.api.member.controller;

import com.tarbonicar.backend.api.member.dto.MemberSignupRequestDto;
import com.tarbonicar.backend.api.member.service.MemberService;
import com.tarbonicar.backend.api.member.service.OAuthService;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.ErrorStatus;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Tag(name="Member", description = "Member 관련 API 입니다.")
public class MemberController {

    private final MemberService memberService;
    private final OAuthService oAuthService;

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
    @GetMapping("/accesstoken")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAccessToken(@RequestParam("code") String code) {
        String token = oAuthService.getKakaoAccessToken(code);
        return ApiResponse.success(SuccessStatus.SEND_KAKAO_ACCESS_TOKEN_SUCCESS, Map.of("accessToken", token));
    }

    // 카카오 액세스토큰으로 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String token = body.get("accessToken");
        if (token == null || token.isBlank()) {
            throw new BadRequestException(ErrorStatus.KAKAO_LOGIN_FAILED.getMessage());
        }
        memberService.kakaoLogin(token);
        return ApiResponse.success_only(SuccessStatus.SEND_KAKAO_LOGIN_SUCCESS);
    }
}

