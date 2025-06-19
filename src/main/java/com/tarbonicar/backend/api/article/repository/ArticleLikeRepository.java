package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    // 사용자가 게시글에 좋아요를 눌렀는지 체크
    boolean existsByArticle_IdAndMember_Id(Long articleId, Long memberId);
}
