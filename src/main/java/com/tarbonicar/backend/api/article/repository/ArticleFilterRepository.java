package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;

import java.util.List;

public interface ArticleFilterRepository {
    List<Article> findByFilters(
            String carType,
            List<String> carName,
            List<Integer> carAge,
            List<ArticleType> articleType,
            SortType sortType
    );

    List<Article> findByMemberId(SortType sortType, String memberId);
}
