package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleFilterRepository {

    // 조회수 1 증가
    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // 좋아요 증가
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // 좋아요 감소
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = CASE WHEN a.likeCount > 0 THEN a.likeCount -1 ELSE 0 END WHERE a.id = :id")
    void decreasementLikeCount(@Param("id") Long id);
}
