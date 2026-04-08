package org.example.videocall.service;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class TokenService {

    private final SecretKey key ;

    public TokenService(@Value("${supabase.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String extractUserIdFromToken(String authHeader) {
        return extractClaim(authHeader, "sub");
    }

    public String extractEmailFromToken(String authHeader) {
        return extractClaim(authHeader, "email");
    }

    /** Generic claim extractor — reused by both methods above. */
    private String extractClaim(String authHeader, String claim) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String[] chunks = token.split("\\.");
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payload).get(claim);
            return (node != null && !node.isNull()) ? node.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}