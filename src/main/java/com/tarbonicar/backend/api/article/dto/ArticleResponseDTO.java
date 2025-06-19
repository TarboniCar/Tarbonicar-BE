package com.tarbonicar.backend.api.article.dto;

import com.tarbonicar.backend.api.article.entity.Article;
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

    public static ArticleResponseDTO fromEntity(Article article) {
        return ArticleResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .likeCount(article.getLikeCount())
                .viewCount(article.getViewCount())
                .createAt(article.getCreatedAt())
                .myLike(false) // 로그인 여부에 따라 처리 필요
                .contentImage(null) // 이미지 있으면 추가
                .carName(article.getCarAge().getCarName().getCarName())
                .carAge(article.getCarAge().getCarAge())
                .build();
    }
}
