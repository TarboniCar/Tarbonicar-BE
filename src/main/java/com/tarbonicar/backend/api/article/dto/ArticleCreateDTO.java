package com.tarbonicar.backend.api.article.dto;

import com.tarbonicar.backend.api.article.entity.ArticleType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleCreateDTO {

    private String title;
    private String content;
    private ArticleType articleType;
    private Long categoryId;
}
