package com.tarbonicar.backend.api.passwordReset.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PasswordResetConfirmDTO {

    private String code;
}