package com.tarbonicar.backend.api.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.QArticle;
import com.tarbonicar.backend.api.article.entity.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArticleFilterRepositoryImpl implements ArticleFilterRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Article> findByFilters(String carType, List<String> carNames, List<Integer> carAges, List<ArticleType> articleTypes, SortType sortType) {
        QArticle article = QArticle.article;

        BooleanBuilder builder = new BooleanBuilder();
        //builder.and(article.isDeleted.eq(false)); // 삭제 안 된 게시글만

        // carType
        if (carType != null && !carType.isEmpty()) {
            builder.and(article.carAge.isNotNull());
            builder.and(article.carAge.carName.isNotNull());
            builder.and(article.carAge.carName.carType.isNotNull());
            builder.and(article.carAge.carName.carType.carType.eq(carType));
        }

        // carName
        if (carNames != null && !carNames.isEmpty()) {
            builder.and(article.carAge.carName.carName.in(carNames));
        }

        // carAge
        if (carAges != null && !carAges.isEmpty()) {
            builder.and(article.carAge.carAge.in(carAges));
        }

        // articleType
        if (articleTypes != null && !articleTypes.isEmpty()) {
            builder.and(article.articleType.in(articleTypes));
        }

        // 정렬 조건 처리
        OrderSpecifier<?> order = getSortSpecifier(sortType, article);

        return queryFactory
                .selectFrom(article)
                .leftJoin(article.carAge).fetchJoin()
                .leftJoin(article.carAge.carName).fetchJoin()
                //.leftJoin(article.carAge.carName.carType).fetchJoin()
                .leftJoin(article.member).fetchJoin()
                .where(builder)
                .orderBy(order)
                .fetch();
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
