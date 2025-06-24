package com.tarbonicar.backend.api.comment.controller;

import com.tarbonicar.backend.api.comment.dto.CommentCreateDTO;
import com.tarbonicar.backend.api.comment.dto.CommentResponseDTO;
import com.tarbonicar.backend.api.comment.dto.CommentUpdateDTO;
import com.tarbonicar.backend.api.comment.service.CommentService;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Comment", description = "Comment 관련 API 입니다.")
@RequestMapping("api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "댓글 등록 API", description = "새로운 댓글을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "댓글 등록 성공")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createComment(
            @RequestBody CommentCreateDTO commentCreateDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        commentService.createComment(commentCreateDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.CREATE_COMMENT_SUCCESS);
    }

    @Operation(
            summary = "댓글 목록 조회 API", description = "게시글에 등록된 댓글 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentResponseDTO>>> getComment(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<CommentResponseDTO> commentResponseDTO = commentService.getComment(articleId, userEmail, pageRequest);
        return ApiResponse.success(SuccessStatus.SEND_COMMENT_SUCCESS, commentResponseDTO);
    }

    @Operation(
            summary = "댓글 수정 API", description = "댓글을 수정 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 수정 성공")
    })
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> modifyComment(
            @RequestBody CommentUpdateDTO commentUpdateDTO,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        commentService.modifyComment(commentUpdateDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.MODIFY_COMMENT_SUCCESS);
    }

    @Operation(
            summary = "댓글 삭제 API", description = "댓글을 삭제 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "댓글 삭제 성공")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        commentService.deleteComment(commentId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_COMMENT_SUCCESS);
    }
}
