package com.tarbonicar.backend.api.article.service;

import com.tarbonicar.backend.api.article.dto.*;
import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;
import com.tarbonicar.backend.api.article.repository.ArticleLikeRepository;
import com.tarbonicar.backend.api.article.repository.ArticleRepository;
import com.tarbonicar.backend.api.category.entity.CarAge;
import com.tarbonicar.backend.api.category.repository.CarAgeRepository;
import com.tarbonicar.backend.api.member.entity.Member;
import com.tarbonicar.backend.api.member.repository.MemberRepository;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.NotFoundException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleRepository articleRepository;
    private final CarAgeRepository carAgeRepository;
    private final MemberRepository memberRepository;

    // 게시글 작성 메서드
    @Transactional
    public Long createArticle(ArticleCreateDTO articleCreateDTO) {

        // 사용자가 존재하지 않을 때 예외처리
        Member member = memberRepository.findById(articleCreateDTO.getMemberId())
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
            SortType sortType
    ) {
        List<Article> articleList = articleRepository.findByFilters(carType, carName, carAge, articleType, sortType);

        return articleList.stream().map(article -> new ArticleResponseDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getLikeCount(),
                article.getViewCount(),
                article.getCreatedAt(),
                false,
                null,
                article.getCarAge().getCarName().getCarName(),
                article.getCarAge().getCarAge()
        )).collect(Collectors.toList());
    }

    // 게시글 상세 조회 메서드
    @Transactional
    public ArticleDetailResponseDTO getArticleDetail(Long articleId, Long memberId) {

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage()));

        // 조회수 1 증가
        articleRepository.incrementViewCount(articleId);

        // myArticle / myLike 판별
        boolean myArticle = memberId != null && article.getMember().getId().equals(memberId);
        boolean myLike = memberId != null && articleLikeRepository.existsByArticle_IdAndMember_Id(articleId, memberId);

        return ArticleDetailResponseDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .likeCount(article.getLikeCount())
                .viewCount(article.getViewCount() + 1) // 메모리상 viewCount는 오래된 값이라 +1 추가
                .articleType(article.getArticleType())
                .createdAt(article.getCreatedAt())
                .myArticle(myArticle)
                .myLike(myLike)
                .modify(article.isModify())
                .nickname(article.getMember().getNickname())
                .profileImage(article.getMember().getProfileImage())
                .carName(article.getCarAge().getCarName().getCarName())
                .carAge(article.getCarAge().getCarAge())
                .build();
    }

    // 게시글 수정 메서드
    @Transactional
    public void modifyArticle(ArticleUpdateDTO articleUpdateDTO) {

        // 기존 게시글 조회
        Article article = articleRepository.findById(articleUpdateDTO.getArticleId())
                .orElseThrow(() ->
                        new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage())
                );

        // 작성자 확인 (권한 체크)
        if (!article.getMember().getId().equals(articleUpdateDTO.getMemberId())) {
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
    public void deleteArticle(Long articleId, Long memberId) {

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() ->
                        new NotFoundException(ErrorStatus.NOT_FOUND_ARTICLE_EXCEPTION.getMessage())
                );

        // 작성자 확인
        if (!article.getMember().getId().equals(memberId)) {
            throw new BadRequestException(ErrorStatus.THIS_MEMBER_IS_NOT_WRITER_EXCEPTION.getMessage());
        }

        articleRepository.delete(article);
    }
}
