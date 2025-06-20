package com.tarbonicar.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /**
     * 200
     */
    SEND_REGISTER_SUCCESS(HttpStatus.OK,"회원가입 성공"),
    SEND_HEALTH_SUCCESS(HttpStatus.OK,"서버 응답 성공"),
    SEND_CARTYPE_CATEGORY_SUCCESS(HttpStatus.OK,"차량 종류 카테고리 조회 성공"),
    SEND_CARNAME_CATEGORY_SUCCESS(HttpStatus.OK,"차량 카테고리 조회 성공"),
    SEND_CARAGE_CATEGORY_SUCCESS(HttpStatus.OK,"차량 연식 카테고리 조회 성공"),
    DELETE_CARAGE_SUCCESS(HttpStatus.OK,"차량 연식 카테고리 삭제 성공"),
    DELETE_CARNAME_SUCCESS(HttpStatus.OK,"차량 이름 카테고리 삭제 성공"),
    DELETE_CARTYPE_SUCCESS(HttpStatus.OK,"차량 타입 카테고리 삭제 성공"),
    SEND_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "이미지 업로드 성공"),
    SEND_ARTICLE_SUCCESS(HttpStatus.OK,"게시글 목록 조회 성공"),
    SEND_ARTICLE_DETAIL_SUCCESS(HttpStatus.OK,"게시글 상세 조회 성공"),
    MODIFY_ARTICLE_SUCCESS(HttpStatus.OK,"게시글 수정 성공"),
    DELETE_ARTICLE_SUCCESS(HttpStatus.OK,"게시글 삭제 성공"),
    SEND_KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "카카오 로그인 성공"),
    SEND_KAKAO_ACCESS_TOKEN_SUCCESS(HttpStatus.OK, "카카오 액세스 토큰 발급 성공"),

    /**
     * 201
     */
    CREATE_ARTICLE_SUCCESS(HttpStatus.CREATED, "게시글 등록 성공"),
    CREATE_CATEGORY_SUCCESS(HttpStatus.CREATED, "카테고리 등록 성공"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
