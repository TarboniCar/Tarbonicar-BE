package com.tarbonicar.backend.api.comment.repository;

import com.tarbonicar.backend.api.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 해당 게시글의 댓글 조회
    List<Comment> findAllByArticle_IdOrderByCreatedAtDesc(Long articleId);

    // 댓글 개수 조회
    long countByArticle_Id(Long articleId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
