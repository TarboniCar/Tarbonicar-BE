package com.tarbonicar.backend.api.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ArticleResponseDTO {

    private Long id;
    private String title;
    private String content;
    private Long likeCount;
    private Long viewCount;
    private Long commentCount;
    private LocalDateTime createAt;

    private Boolean myLike;

    private String carName;
    private Integer carAge;
}
