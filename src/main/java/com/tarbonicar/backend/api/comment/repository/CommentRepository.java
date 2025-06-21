package com.tarbonicar.backend.api.comment.repository;

import com.tarbonicar.backend.api.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 해당 게시글의 댓글 조회
    List<Comment> findAllByArticle_Id(Long articleId);
}
