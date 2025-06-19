package com.tarbonicar.backend.api.aws.s3.service;

import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    @Value("${cloud.aws.s3.domain}")
    private String domain;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(ErrorStatus.MISSING_UPLOAD_IMAGE_EXCEPTION.getMessage());
        }

        String dir = "images";
        // 한 번만 생성되는 랜덤 문자열
        String randomString = RandomStringUtils.randomAlphanumeric(16);
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());

        // 원본 파일명 분리
        String originalFilename = file.getOriginalFilename();
        String baseName = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
                : (originalFilename == null ? "file" : originalFilename);
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // 저장할 파일명 구성
        String fileName = baseName + "_" + currentDateTime + extension;
        // S3 의 키 (경로) 구성
        String fileKey = String.format("%s/%s/%s",
                dir, randomString, fileName);

        // S3에 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileKey)
                .acl("public-read")
                .build();
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 완전한 URL 반환
        return domain + "/" + bucketName + "/" + fileKey;
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith(domain)) {
            // AWS S3
            //String fileKey = imageUrl.replace(domain + "/", "");
            // rhkr8521-Bucket
            String fileKey = imageUrl.replace(domain + "/" + bucketName + "/", "");
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        }
    }
}