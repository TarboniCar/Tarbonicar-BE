package com.tarbonicar.backend.api.article.controller;

import com.tarbonicar.backend.api.article.dto.*;
import com.tarbonicar.backend.api.article.entity.ArticleType;
import com.tarbonicar.backend.api.article.entity.SortType;
import com.tarbonicar.backend.api.article.service.ArticleService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name="Article", description = "Article 관련 API 입니다.")
@RequestMapping("/api/v1/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(
            summary = "게시글 등록 API", description = "새로운 게시글을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 등록 성공")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createArticle(@RequestBody ArticleCreateDTO articleCreateDTO, @AuthenticationPrincipal UserDetails userDetails){

        Long id = articleService.createArticle(articleCreateDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_ARTICLE_SUCCESS, id);
    }

    @Operation(
            summary = "게시글 목록 조회 API", description = "등록된 게시글 목록을 조회 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<ArticleResponseDTO>>> getArticle(
            @RequestParam(required = false) String carType,
            @RequestParam(required = false) List<String> carName,
            @RequestParam(required = false) List<Integer> carAge,
            @RequestParam(required = false) List<ArticleType> articleType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "RECENT") SortType sortType,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ArticleResponseDTO> articleResponseDTO = articleService.getArticle(carType, carName, carAge, articleType, sortType, pageRequest, userEmail);
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_SUCCESS, articleResponseDTO);
    }

    @Operation(
            summary = "내가 작성한 게시글 목록 조회 API", description = "내가 작성한 게시글 목록을 조회 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내가 작성한 게시글 목록 조회 성공")
    })
    @GetMapping("/my-list")
    public ResponseEntity<ApiResponse<Page<ArticleResponseDTO>>> getMyArticle(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "RECENT") SortType sortType,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ArticleResponseDTO> articleResponseDTO = articleService.getMyArticle(sortType, pageRequest, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_SUCCESS, articleResponseDTO);
    }


    @Operation(
            summary = "게시글 상세 조회 API", description = "게시글을 상세 조회 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ArticleDetailResponseDTO>> getArticleDetail(@RequestParam Long articleId, @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;

        ArticleDetailResponseDTO articleDetailResponseDTO = articleService.getArticleDetail(articleId, userEmail);
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_DETAIL_SUCCESS, articleDetailResponseDTO);
    }

    @Operation(
            summary = "게시글 수정 API", description = "게시글을 수정 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> modifyArticle(@RequestBody ArticleUpdateDTO articleUpdateDTO, @AuthenticationPrincipal UserDetails userDetails) {

        articleService.modifyArticle(articleUpdateDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.MODIFY_ARTICLE_SUCCESS);
    }

    @Operation(
            summary = "게시글 삭제 API", description = "게시글을 삭제 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공")
    })
    @DeleteMapping("/{articleId}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails userDetails) {

        articleService.deleteArticle(articleId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_ARTICLE_SUCCESS);
    }

    @Operation(summary = "게시글 좋아요 토글 API", description = "게시글 좋아요 등록/해제를 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 토글 성공")
    })
    @PostMapping("/like/{articleId}")
    public ResponseEntity<ApiResponse<Void>> likeArticle(@PathVariable Long articleId, @AuthenticationPrincipal UserDetails userDetails){

        articleService.likeArticle(articleId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.SEND_ARTICLE_LIKE_SUCCESS);
    }
}
