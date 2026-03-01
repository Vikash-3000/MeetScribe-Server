package com.meetscribe.gateway.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtValidator {

    private final PublicKey publicKey;

    public JwtValidator() {
        try {
            this.publicKey = loadPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        InputStream is =
                new ClassPathResource("public.pem").getInputStream();

        String key = new String(is.readAllBytes())
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    public Claims validate(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}