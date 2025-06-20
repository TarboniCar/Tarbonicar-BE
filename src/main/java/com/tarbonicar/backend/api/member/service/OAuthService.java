package com.tarbonicar.backend.api.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarbonicar.backend.api.member.dto.KakaoUserInfoDto;
import com.tarbonicar.backend.common.exception.BadRequestException;
import com.tarbonicar.backend.common.exception.InternalServerException;
import com.tarbonicar.backend.common.response.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class OAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    // 인가 코드로 액세스 토큰 요청
    public String getKakaoAccessToken(String code) {
        String tokenUri = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<?> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BadRequestException(ErrorStatus.KAKAO_TOKEN_REQUEST_FAILED.getMessage());
        }

        try {
            Map<String, Object> responseBody = new ObjectMapper().readValue(response.getBody(), Map.class);
            return responseBody.get("access_token").toString();
        } catch (Exception e) {
            throw new InternalServerException(ErrorStatus.INTERNAL_TOKEN_PARSING_FAILED.getMessage());
        }
    }

    // 액세스 토큰으로 사용자 정보 요청
    public KakaoUserInfoDto getKakaoUserInfo(String accessToken) {
        String userInfoUri = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new BadRequestException(ErrorStatus.KAKAO_USERINFO_REQUEST_FAILED.getMessage());
        }

        try {
            Map<String, Object> result = new ObjectMapper().readValue(response.getBody(), Map.class);

            // 프로필 정보 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) result.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            return KakaoUserInfoDto.builder()
                    .id(result.get("id").toString())
                    .email("kakao_" + result.get("id").toString() + "@social.com")
                    .nickname((String) profile.get("nickname"))
                    .profileImage((String) profile.get("profile_image_url"))
                    .build();
        } catch (Exception e) {
            throw new InternalServerException(ErrorStatus.INTERNAL_USERINFO_PARSING_FAILED.getMessage());
        }
    }

}
