package com.tarbonicar.backend.api.comment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentCreateDTO {

    private String content;
    private Long articleId;

    // 추후 JWT 로그인 구현 시 삭제 예정
    private Long memberId;
}
