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

    // 내가 작성한 게시글 수 조회
    @Query("SELECT COUNT(a) FROM Article a WHERE a.member.email = :email")
    int countByMemberEmail(@Param("email") String email);

    // 내가 받은 좋아요 수 조회
    @Query("SELECT COALESCE(SUM(a.likeCount), 0) FROM Article a WHERE a.member.email = :email")
    int countTotalLikesByMemberEmail(@Param("email") String email);
}
