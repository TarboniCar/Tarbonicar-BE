package com.tarbonicar.backend.api.aws.s3.controller;

import com.tarbonicar.backend.api.aws.s3.service.S3Service;
import com.tarbonicar.backend.common.response.ApiResponse;
import com.tarbonicar.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/s3")
@Tag(name = "Image - S3", description = "이미지 관련 API 입니다.")
@RequiredArgsConstructor
public class S3Controller {

   private final S3Service s3Service;

    @Operation(summary = "이미지 업로드 API", description = "이미지를 받아서 저장 후 URL로 반환 합니다.")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam(value = "image") MultipartFile file) throws IOException {

        String url = s3Service.uploadImage(file);

        return ApiResponse.success(SuccessStatus.SEND_IMAGE_UPLOAD_SUCCESS, url);
    }

}
