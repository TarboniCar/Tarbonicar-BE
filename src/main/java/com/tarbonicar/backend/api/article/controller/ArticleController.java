package com.tarbonicar.backend.api.article.controller;

import com.tarbonicar.backend.api.article.dto.ArticleCreateDTO;
import com.tarbonicar.backend.api.article.dto.ArticleDetailResponseDTO;
import com.tarbonicar.backend.api.article.dto.ArticleUpdateDTO;
import com.tarbonicar.backend.api.article.service.ArticleService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<Void>> createArticle(@RequestBody ArticleCreateDTO articleCreateDTO){

        articleService.createArticle(articleCreateDTO);
        return ApiResponse.success_only(SuccessStatus.CREATE_ARTICLE_SUCCESS);
    }

    @Operation(
            summary = "게시글 상세 조회 API", description = "게시글을 상세 조회 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ArticleDetailResponseDTO>> getArticleDetail(@RequestParam Long articleId, Long memberId /* memberId는 JWT 완료 후 변경 할 예정 */) {

        ArticleDetailResponseDTO articleDetailResponseDTO = articleService.getArticleDetail(articleId, memberId);
        return ApiResponse.success(SuccessStatus.SEND_ARTICLE_DETAIL_SUCCESS, articleDetailResponseDTO);
    }

    @Operation(
            summary = "게시글 수정 API", description = "게시글을 수정 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> modifyArticle(@RequestBody ArticleUpdateDTO articleUpdateDTO) {

        articleService.modifyArticle(articleUpdateDTO);
        return ApiResponse.success_only(SuccessStatus.MODIFY_ARTICLE_SUCCESS);
    }

    @Operation(
            summary = "게시글 삭제 API", description = "게시글을 삭제 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공")
    })
    @DeleteMapping("/{articleId}/{memberId}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long articleId, @PathVariable Long memberId) {

        articleService.deleteArticle(articleId, memberId);
        return ApiResponse.success_only(SuccessStatus.DELETE_ARTICLE_SUCCESS);
    }
}
