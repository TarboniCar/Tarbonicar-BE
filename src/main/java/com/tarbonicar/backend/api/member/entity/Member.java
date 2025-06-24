package com.tarbonicar.backend.api.member.entity;

import com.tarbonicar.backend.api.constant.Authority;
import com.tarbonicar.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "member")
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;
    private String password;
    private String nickname;
    private String profileImage;
    private String socialType;
    private String socialId;
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Builder
    public Member(String nickname, String password, String email, String profileImage, Authority authority) {
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.profileImage = profileImage;
        this.authority = authority;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 변경
    public void setPassword(String password) {
        this.password = password;
    }

    // 프로필 이미지 변경
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

}
