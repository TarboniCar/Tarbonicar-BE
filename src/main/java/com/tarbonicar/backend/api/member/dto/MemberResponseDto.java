package com.tarbonicar.backend.api.member.dto;

import com.tarbonicar.backend.api.member.entity.Member;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .build();
    }
}
