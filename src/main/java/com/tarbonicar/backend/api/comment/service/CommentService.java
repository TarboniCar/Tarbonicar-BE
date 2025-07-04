package com.tarbonicar.backend.api.comment.service;

import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.repository.ArticleRepository;
import com.tarbonicar.backend.api.comment.dto.CommentCreateDTO;
import com.tarbonicar.backend.api.comment.dto.CommentResponseDTO;
import com.tarbonicar.backend.api.comment.dto.CommentUpdateDTO;
import com.tarbonicar.backend.api.comment.entity.Comment;
import com.tarbonicar.backend.api.comment.repository.CommentRepository;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    // 댓글 작성 메서드
    @Transactional
    public void createComment(CommentCreateDTO commentCreateDTO, String memberId) {

        // 사용자가 존재하지 않을 때 예외처리
        Member member = memberRepository.findByEmail(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 게시글이 존재하지 않을 때 예외처라
        Article article = articleRepository.findById(commentCreateDTO.getArticleId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        Comment comment = Comment.builder()
                .content(commentCreateDTO.getContent())
                .modify(false)
                .member(member)
                .article(article)
                .build();

        commentRepository.save(comment);
    }

    // 댓글 목록 조회 메서드
    @Transactional
    public Page<CommentResponseDTO> getComment(Long articleId, String memberId, Pageable pageable) {

        // 댓글 리스트 조회
        Page<Comment> commentList = commentRepository.findAllByArticle_IdOrderByCreatedAtDesc(articleId, pageable);

        // 게시글 확인
        if (!articleRepository.existsById(articleId)) {
            throw new BadRequestException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage());
        }

        return commentList.map(comment -> new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getMember().getEmail().equals(memberId), // myComment 판별
                comment.isModify(),
                comment.getMember().getId(),
                comment.getMember().getNickname(),
                comment.getMember().getProfileImage()
        ));
    }

    // 댓글 수정 메서드
    @Transactional
    public void modifyComment(CommentUpdateDTO commentUpdateDTO, String memberId) {

        // 기존 댓글 조회
        Comment comment = commentRepository.findById(commentUpdateDTO.getCommentId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COMMENT_EXCEPTION.getMessage()));

        // 작성자 확인 (권한 체크)
        if (!comment.getMember().getEmail().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.THIS_MEMBER_IS_NOT_COMMENT_WRITER_EXCEPTION.getMessage());
        }

        // 게시글 확인
        if (!comment.getArticle().getId().equals(commentUpdateDTO.getArticleId())) {
            throw new BadRequestException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage());
        }

        comment.modify(commentUpdateDTO.getContent());
    }

    // 댓글 삭제 메서드
    @Transactional
    public void deleteComment(Long commentId, String memberId) {

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_COMMENT_EXCEPTION.getMessage()));

        // 작성자 확인
        if (!comment.getMember().getEmail().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.THIS_MEMBER_IS_NOT_COMMENT_WRITER_EXCEPTION.getMessage());
        }

        commentRepository.delete(comment);
    }
}
