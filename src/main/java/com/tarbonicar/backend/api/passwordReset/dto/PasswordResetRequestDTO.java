package com.tarbonicar.backend.api.passwordReset.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PasswordResetRequestDTO {

    private String email;
}