package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.entity.ArticleLike;
import com.tarbonicar.backend.api.member.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Long> {

    // 사용자가 게시글에 좋아요를 눌렀는지 체크
    boolean existsByArticle_IdAndMember_Id(Long articleId, Long memberId);

    // 좋아요 상태 체크
    Optional<ArticleLike> findByArticle_IdAndMember_Id(Long articleId, Long memberId);

    @Modifying
    @Query("DELETE FROM ArticleLike al WHERE al.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);

    // 좋아요 한 게시글 찾기
    @Query("SELECT al.article.id FROM ArticleLike al WHERE al.member.id = :memberId")
    List<Long> findLikedArticleIdsByMemberId(@Param("memberId") Long memberId);

    // 좋아요 삭제
    @Modifying
    @Query("DELETE FROM ArticleLike al WHERE al.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
