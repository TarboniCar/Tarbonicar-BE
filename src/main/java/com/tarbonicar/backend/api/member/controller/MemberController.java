package com.tarbonicar.backend.api.member.controller;

import com.tarbonicar.backend.api.member.dto.MemberSignupRequestDto;
import com.tarbonicar.backend.api.member.service.MemberService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto);
        return ApiResponse.success_only(SuccessStatus.SEND_REGISTER_SUCCESS);
    }
}

