package com.tarbonicar.backend.api.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponseDTO {

    private Long id;
    private String content;
    private LocalDateTime createAt;

    private boolean myComment; // 내가 작성한 댓글이면 true
    private boolean modify; // 수정한 댓글이면 true

    private Long memberId;
    private String nickname;
    private String profileImage;
}
