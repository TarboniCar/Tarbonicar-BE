package com.tarbonicar.backend.api.comment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentCreateDTO {

    private String content;
    private Long articleId;
}
