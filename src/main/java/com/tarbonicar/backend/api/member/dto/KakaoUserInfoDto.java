package com.tarbonicar.backend.api.member.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoDto {
    private String id;
    private String email;
    private String nickname;
    private String profileImage;
}
