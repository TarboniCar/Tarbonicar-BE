package com.tarbonicar.backend.api.article.repository;

import com.tarbonicar.backend.api.article.dto.ArticleResponseDTO;
import com.tarbonicar.backend.api.article.entity.Article;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleFilterRepository {
    Page<ArticleResponseDTO> findByFilters(
            String carType,
            List<String> carName,
            List<Integer> carAge,
            List<ArticleType> articleType,
            SortType sortType,
            Pageable pageable,
            Long userId
    );

    /*Page<Article> findByFilters(
            String carType,
            List<String> carName,
            List<Integer> carAge,
            List<ArticleType> articleType,
            SortType sortType,
            Pageable pageable
    );*/

    Page<ArticleResponseDTO> findByMemberId(SortType sortType,Pageable pageable, String memberId, Long userId);
}
