package com.tarbonicar.backend.api.comment.repository;

import com.tarbonicar.backend.api.comment.entity.Comment;
import com.tarbonicar.backend.api.member.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 해당 게시글의 댓글 조회
    Page<Comment> findAllByArticle_IdOrderByCreatedAtDesc(Long articleId, Pageable pageable);

    // 댓글 개수 조회
    long countByArticle_Id(Long articleId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);

    // 회원이 작성한 모든 댓글 삭제
//    @Modifying
//    @Transactional
//    @Query("DELETE FROM Comment c WHERE c.member.email = :email")
//    void deleteAllByMemberEmail(@Param("email") String email);

    // 회원탈퇴 댓글 삭제
    int deleteByMember(Member member);

}
