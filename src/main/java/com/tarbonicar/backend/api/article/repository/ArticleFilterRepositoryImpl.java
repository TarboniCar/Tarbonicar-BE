package com.tarbonicar.backend.api.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tarbonicar.backend.api.article.dto.ArticleResponseDTO;
import com.tarbonicar.backend.api.article.entity.*;
import com.tarbonicar.backend.api.category.entity.QCarAge;
import com.tarbonicar.backend.api.category.entity.QCarName;
import com.tarbonicar.backend.api.category.entity.QCarType;
import com.tarbonicar.backend.api.comment.entity.QComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleFilterRepositoryImpl implements ArticleFilterRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ArticleResponseDTO> findByFilters(
            String carType,
            List<String> carNames,
            List<Integer> carAges,
            List<ArticleType> articleTypes,
            SortType sortType,
            Pageable pageable,
            Long userId
    ) {
        QArticle article = QArticle.article;
        QCarAge carAge = QCarAge.carAge1;
        QCarName carName = QCarName.carName1;
        QCarType carTypeEntity = QCarType.carType1;
        QArticleLike articleLike = QArticleLike.articleLike;
        QComment comment = QComment.comment;

        BooleanBuilder builder = new BooleanBuilder();

        // carType
        if (carType != null && !carType.isEmpty()) {
            builder.and(carTypeEntity.carType.eq(carType));
        }

        // carName
        if (carNames != null && !carNames.isEmpty()) {
            builder.and(carName.carName.in(carNames));
        }

        // carAge
        if (carAges != null && !carAges.isEmpty()) {
            builder.and(carAge.carAge.in(carAges));
        }

        // articleType
        if (articleTypes != null && !articleTypes.isEmpty()) {
            builder.and(article.articleType.in(articleTypes));
        }

        // 정렬 조건 처리
        OrderSpecifier<?> order = getSortSpecifier(sortType, article);

        // 페이징 처라
        List<ArticleResponseDTO> results = queryFactory
                .select(Projections.constructor(ArticleResponseDTO.class,
                        article.id,
                        article.title,
                        article.content,
                        article.likeCount,
                        article.viewCount,
                        // 댓글 개수 서브쿼리
                        JPAExpressions.select(comment.count())
                                .from(comment)
                                .where(comment.article.id.eq(article.id)),
                        article.createdAt,
                        // 좋아요 여부 서브쿼리
                        userId != null ?
                                JPAExpressions.selectOne()
                                        .from(articleLike)
                                        .where(articleLike.article.id.eq(article.id)
                                                .and(articleLike.member.id.eq(userId)))
                                        .exists() : Expressions.asBoolean(false),
                        carName.carName,
                        carAge.carAge
                ))
                .from(article)
                .leftJoin(article.carAge, carAge)
                .leftJoin(carAge.carName, carName)
                .leftJoin(carName.carType, carTypeEntity)
                .leftJoin(article.member)
                .where(builder)
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(article.count())
                .from(article)
                .leftJoin(article.carAge, carAge)
                .leftJoin(carAge.carName, carName)
                .leftJoin(carName.carType, carTypeEntity)
                .where(builder)
                .fetchOne();

        long totalCount = (total == null) ? 0 : total; // NPE 방지용

        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public Page<ArticleResponseDTO> findByMemberId(
            SortType sortType,
            Pageable pageable,
            String memberId,
            Long userId
    ) {
        QArticle article = QArticle.article;
        QCarAge carAge = QCarAge.carAge1;
        QCarName carName = QCarName.carName1;
        QCarType carTypeEntity = QCarType.carType1;
        QArticleLike articleLike = QArticleLike.articleLike;
        QComment comment = QComment.comment;

        // 정렬 기준
        OrderSpecifier<?> order = getSortSpecifier(sortType, article);

        List<ArticleResponseDTO> result = queryFactory
                .select(Projections.constructor(ArticleResponseDTO.class,
                        article.id,
                        article.title,
                        article.content,
                        article.likeCount,
                        article.viewCount,
                        // 댓글 개수 서브쿼리
                        JPAExpressions.select(comment.count())
                                .from(comment)
                                .where(comment.article.id.eq(article.id)),
                        article.createdAt,
                        // 좋아요 여부 서브쿼리
                        userId != null ?
                                JPAExpressions.selectOne()
                                        .from(articleLike)
                                        .where(articleLike.article.id.eq(article.id)
                                                .and(articleLike.member.id.eq(userId)))
                                        .exists() : Expressions.asBoolean(false),
                        carName.carName,
                        carAge.carAge
                        )).from(article)
                .leftJoin(article.carAge, carAge)
                .leftJoin(carAge.carName, carName)
                .leftJoin(carName.carType, carTypeEntity)
                .leftJoin(article.member)
                .where(article.member.email.eq(memberId))
                .orderBy(order)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(article.count())
                .from(article)
                .where(article.member.email.eq(memberId))
                .fetchOne();

        long totalCount = (total == null) ? 0 : total; // NPE 방지용

        return new PageImpl<>(result, pageable, totalCount);
    }

    private OrderSpecifier<?> getSortSpecifier(SortType sortType, QArticle article) {
        if (sortType == null) return article.createdAt.desc(); // 기본값

        return switch (sortType) {
            case RECENT -> article.createdAt.desc();
            case OLDEST -> article.createdAt.asc();
            case MOSTLIKED -> article.likeCount.desc();
            case MOSTVIEW -> article.viewCount.desc();
        };
    }
}
