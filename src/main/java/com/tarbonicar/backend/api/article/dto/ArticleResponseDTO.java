package com.tarbonicar.backend.api.article.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleResponseDTO {

    private Long id;
    private String title;
    private String content;
    private long likeCount;
    private long viewCount;
    private LocalDateTime createAt;

    private boolean myLike;
    private String contentImage; // 게시글 대표 사진

    private String carName;
    private int carAge;
}
