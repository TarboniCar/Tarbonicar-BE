package com.tarbonicar.backend.api.article.service;

import com.tarbonicar.backend.api.article.dto.*;
import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleLike;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;
import com.tarbonicar.backend.api.article.repository.ArticleLikeRepository;
import com.tarbonicar.backend.api.article.repository.ArticleRepository;
import com.tarbonicar.backend.api.category.entity.CarAge;
import com.tarbonicar.backend.api.category.repository.CarAgeRepository;
import com.tarbonicar.backend.api.comment.repository.CommentRepository;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final CarAgeRepository carAgeRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    // 게시글 작성 메서드
    @Transactional
    public Long createArticle(ArticleCreateDTO articleCreateDTO, String memberEmail) {

        // 사용자가 존재하지 않을 때 예외처리
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 카테고리(차량 연식)이 존재하지 않을 때 예외처리
        CarAge carAge = carAgeRepository.findById(articleCreateDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException(ErrorStatus.NOT_FOUND_CARAGE_EXCEPTION.getMessage()));

        Article article = Article.builder()
                .title(articleCreateDTO.getTitle())
                .content(articleCreateDTO.getContent())
                .articleType(articleCreateDTO.getArticleType())
                .likeCount(0)
                .viewCount(0)
                .modify(false)
                .member(member)
                .carAge(carAge)
                .build();

        Article articleSave = articleRepository.save(article);
        return articleSave.getId();
    }

    // 게시글 목록 조회 메서드
    @Transactional
    public List<ArticleResponseDTO> getArticle(
            String carType,
            List<String> carName,
            List<Integer> carAge,
            List<ArticleType> articleType,
            SortType sortType,
            String memberEmail
    ) {

        List<Article> articleList = articleRepository.findByFilters(carType, carName, carAge, articleType, sortType);

        // 이메일이 null이 아니면 회원 조회
        Long userId;
        if (memberEmail != null && !memberEmail.isBlank()) {
            Optional<Member> opt = memberRepository.findByEmail(memberEmail.trim());
            userId = opt.map(Member::getId).orElse(null);
        } else userId = null;

        return articleList.stream().map(article -> new ArticleResponseDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getLikeCount(),
                article.getViewCount(),
                commentRepository.countByArticle_Id(article.getId()),
                article.getCreatedAt(),
                (userId != null) && articleLikeRepository.existsByArticle_IdAndMember_Id(article.getId(), userId),
                null,
                article.getCarAge().getCarName().getCarName(),
                article.getCarAge().getCarAge()
        )).collect(Collectors.toList());
    }

    // 내가 작성한 게시글 목록 조회 메서드
    @Transactional
    public List<ArticleResponseDTO> getMyArticle(SortType sortType, String memberEmail) {

        List<Article> articleList = articleRepository.findByMemberId(sortType, memberEmail);

        // 이메일이 null이 아니면 회원 조회
        Long userId;
        if (memberEmail != null && !memberEmail.isBlank()) {
            Optional<Member> opt = memberRepository.findByEmail(memberEmail.trim());
            userId = opt.map(Member::getId).orElse(null);
        } else userId = null;

        return articleList.stream().map(article -> new ArticleResponseDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getLikeCount(),
                article.getViewCount(),
                commentRepository.countByArticle_Id(article.getId()),
                article.getCreatedAt(),
                (userId != null) && articleLikeRepository.existsByArticle_IdAndMember_Id(article.getId(), userId),
                null,
                article.getCarAge().getCarName().getCarName(),
                article.getCarAge().getCarAge()
        )).collect(Collectors.toList());
    }

    // 게시글 상세 조회 메서드
    @Transactional
    public ArticleDetailResponseDTO getArticleDetail(Long articleId, String memberEmail) {

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        // 조회수 1 증가
        articleRepository.incrementViewCount(articleId);

        // 이메일이 null이 아니면 회원 조회
        Long userId = null;
        if (memberEmail != null && !memberEmail.isBlank()) {
            Optional<Member> opt = memberRepository.findByEmail(memberEmail.trim());
            if (opt.isPresent()) {
                userId = opt.get().getId();
            }
        }

        // userId가 null이면 myArticle/myLike는 모두 false
        boolean myArticle = (userId != null)
                && article.getMember().getId().equals(userId);
        boolean myLike = (userId != null)
                && articleLikeRepository
                .existsByArticle_IdAndMember_Id(articleId, userId);

        long commentCount = commentRepository.countByArticle_Id(articleId);

        return ArticleDetailResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .likeCount(article.getLikeCount())
                .viewCount(article.getViewCount() + 1) // 메모리상 viewCount는 오래된 값이라 +1 추가
                .commentCount(commentCount)
                .articleType(article.getArticleType())
                .createdAt(article.getCreatedAt())
                .myArticle(myArticle)
                .myLike(myLike)
                .modify(article.isModify())
                .nickname(article.getMember().getNickname())
                .profileImage(article.getMember().getProfileImage())
                .carType(article.getCarAge().getCarName().getCarType().getCarType())
                .carName(article.getCarAge().getCarName().getCarName())
                .carAge(article.getCarAge().getCarAge())
                .build();
    }

    // 게시글 수정 메서드
    @Transactional
    public void modifyArticle(ArticleUpdateDTO articleUpdateDTO, String memberEmail) {

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 기존 게시글 조회
        Article article = articleRepository.findById(articleUpdateDTO.getArticleId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        // 작성자 확인 (권한 체크)
        if (!article.getMember().getId().equals(member.getId())) {
            throw new BadRequestException(ErrorStatus.THIS_MEMBER_IS_NOT_WRITER_EXCEPTION.getMessage());
        }

        // 새로운 연식(CarAge) 조회
        CarAge carAge = carAgeRepository.findById(articleUpdateDTO.getCategoryId())
                .orElseThrow(() ->
                        new NotFoundException(ErrorStatus.NOT_FOUND_CARAGE_EXCEPTION.getMessage())
                );

        article.modify(
                articleUpdateDTO.getTitle(),
                articleUpdateDTO.getContent(),
                articleUpdateDTO.getArticleType(),
                carAge
        );
    }

    // 게시글 삭제 메서드
    @Transactional
    public void deleteArticle(Long articleId, String memberEmail) {

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        // 작성자 확인
        if (!article.getMember().getId().equals(member.getId())) {
            throw new BadRequestException(ErrorStatus.THIS_MEMBER_IS_NOT_WRITER_EXCEPTION.getMessage());
        }

        // 좋아요 삭제
        articleLikeRepository.deleteByArticleId(articleId);

        // 댓글 삭제
        commentRepository.deleteByArticleId(articleId);

        // 게시글 삭제
        articleRepository.delete(article);
    }

    // 게시글 좋아요 토글 메서드
    @Transactional
    public void likeArticle(Long articleId, String memberEmail){

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        // 좋아요 상태 체크
        Optional<ArticleLike> existingLike = articleLikeRepository.findByArticle_IdAndMember_Id(articleId, member.getId());

        // 만약 좋아요를 누른상태면 좋아요 해제
        if(existingLike.isPresent()) {
            articleLikeRepository.delete(existingLike.get());
            articleRepository.decreasementLikeCount(articleId);

        //만약 좋아요를 누르지 않았으면 좋아요 추가
        }else{
            ArticleLike articleLike = ArticleLike.builder()
                    .article(article)
                    .member(member)
                    .build();

            articleLikeRepository.save(articleLike);
            articleRepository.incrementLikeCount(articleId);
        }

    }
}
