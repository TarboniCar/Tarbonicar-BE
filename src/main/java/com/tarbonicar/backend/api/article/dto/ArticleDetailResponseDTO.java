package com.tarbonicar.backend.api.article.dto;

import com.tarbonicar.backend.api.article.entity.ArticleType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArticleDetailResponseDTO {

    private Long id;
    private String title;
    private String content;
    private long likeCount;
    private long viewCount;
    private ArticleType articleType;
    private LocalDateTime createdAt;

    private boolean myArticle; // 내가 작성한 게시글이면 true
    private boolean myLike; // 내가 좋아요 누른 게시글이면 true
    private boolean modify; // 수정한 게시글이면 true

    private String nickname;
    private String profileImage;

    private String carName;
    private int carAge;

}
