package com.tarbonicar.backend.api.article.dto;

import com.tarbonicar.backend.api.article.entity.ArticleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleUpdateDTO {

    private Long articleId;
    private String title;
    private String content;
    private ArticleType articleType;
    private Long categoryId;

    // 추후 JWT 로그인 구현 시 삭제 예정
    private Long memberId;
}
