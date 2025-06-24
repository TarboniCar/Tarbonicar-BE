package com.tarbonicar.backend.api.passwordReset.controller;

import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetConfirmDTO;
import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetDTO;
import com.tarbonicar.backend.api.passwordReset.dto.PasswordResetRequestDTO;
import com.tarbonicar.backend.api.passwordReset.service.PasswordResetService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PasswordReset", description = "비밀번호 초기화 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "비밀번호 초기화 요청 API",
            description = "이메일로 비밀번호 초기화 코드를 보냅니다"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 초기화 링크 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
    })
    @PostMapping("/email-request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordResetCode(@RequestBody PasswordResetRequestDTO passwordResetRequestDTO) {

        passwordResetService.requestPasswordResetCode(passwordResetRequestDTO);
        return ApiResponse.success_only(SuccessStatus.SEND_PASSWORD_RESET_CODE_SUCCESS);
    }

    @Operation(
            summary = "비밀번호 초기화 코드 검증 API",
            description = "이메일에서 받은 코드를 검증합니다.. <br>"
                    + "<p>"
                    + "호출 필드 정보) <br>"
                    + "code : 비밀번호 초기화 인증코드 <br>"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 초기화 인증코드 검증 성공"),
    })
    @PostMapping("/email-confirm")
    public ResponseEntity<ApiResponse<Void>> resetVerifyPasswordResetCode(@RequestBody PasswordResetConfirmDTO passwordResetConfirmDTO) {

        passwordResetService.resetVerifyPasswordResetCode(passwordResetConfirmDTO);
        return ApiResponse.success_only(SuccessStatus.SEND_PASSWORD_RESET_CODE_VERIFY_SUCCESS);
    }

    @Operation(
            summary = "비밀번호 초기화 API",
            description = "비밀번호를 변경합니다. <br>"
                    + "<p>"
                    + "호출 필드 정보) <br>"
                    + "newPassword : 새로운 비밀번호 <br>"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
    })
    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetDTO passwordResetDTO) {

        passwordResetService.resetPassword(passwordResetDTO);
        return ApiResponse.success_only(SuccessStatus.SEND_PASSWORD_SUCCESS);
    }
}
