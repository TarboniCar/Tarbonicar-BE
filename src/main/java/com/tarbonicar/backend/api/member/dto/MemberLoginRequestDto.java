package com.tarbonicar.backend.api.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @NoArgsConstructor
public class MemberLoginRequestDto {
    private String email;
    private String password;
}
