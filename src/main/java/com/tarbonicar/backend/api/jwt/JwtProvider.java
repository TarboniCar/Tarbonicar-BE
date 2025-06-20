package com.tarbonicar.backend.api.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access")

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60;

    public String generateToken(String nickname) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME);
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(headerJson.getBytes());
        long exp = expiryDate.getTime() / 1000;
        String payloadJson = String.format("{\"sub\":\"%s\",\"exp\":%d}", nickname, exp);
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());

        String message = header + "." + payload;

        String signature = sign(message, SECRET_KEY);
        System.out.println("✅ [generateToken] 발급된 토큰: \" + token");
        return  message + "." + signature;
    }

    private String sign(String data, String secret) {
        try{
            SecretKeySpec hmacKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(hmacKey);

            byte[] sigBytes = mac.doFinal(data.getBytes());

            return Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
        }
        catch (Exception e) {
            throw new RuntimeException("JWT 서명 생성 실패",e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String message = header + "." + payload;
            String expectedSig = sign(message, SECRET_KEY);
            if (!expectedSig.equals(signature)) return false;

            // ✅ 만료 시간 검증 추가
            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));
            long exp = Long.parseLong(decodedPayload.split("\"exp\":")[1].replaceAll("[^0-9]", ""));
            long now = System.currentTimeMillis() / 1000;
            return now < exp;

        } catch (Exception e) {
            return false;
        }
    }


    public String getSubject(String token) {
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        // payload: {"sub":"nickname","exp":1718912234}
        String sub = payload.split("\"sub\":\"")[1].split("\"")[0];
        return sub;
    }



}
