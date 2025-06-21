package com.tarbonicar.backend.api.comment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateDTO {

    private Long commentId;
    private String content;
    private Long articleId;
}
