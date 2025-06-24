package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleFilterRepository {
    Page<Article> findByFilters(
            String carType,
            List<String> carName,
            List<Integer> carAge,
            List<ArticleType> articleType,
            SortType sortType,
            Pageable pageable
    );

    Page<Article> findByMemberId(SortType sortType,Pageable pageable, String memberId);
}
