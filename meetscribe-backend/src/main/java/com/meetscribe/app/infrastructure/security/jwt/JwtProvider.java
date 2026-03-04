package com.meetscribe.app.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long expiryMillis = 3600000;

    public JwtProvider(
            @Value("${JWT_PRIVATE_KEY}") String privateKeyStr,
            @Value("${JWT_PUBLIC_KEY}") String publicKeyStr
    ) throws Exception {

        this.privateKey = loadPrivateKey(privateKeyStr);
        this.publicKey = loadPublicKey(publicKeyStr);
    }

    private PrivateKey loadPrivateKey(String key) throws Exception {

        key = key.replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PublicKey loadPublicKey(String key) throws Exception {

        key = key.replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }

    public String generateToken(Long userId,
                                String email,
                                String deviceId,
                                Integer tokenVersion) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("deviceId", deviceId)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey)
                .compact();
    }

    public Claims validate(String token) {
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token);

        return claims.getPayload();
    }
}