package org.example.videocall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.videocall.DTOs.SupabaseLoginResponse;
import org.example.videocall.DTOs.SupabaseSignupResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String anonKey;

    @Value("${supabase.jwt.secret}")
    private String supaKey;

    private final RestTemplate restTemplate = new RestTemplate();


    public SupabaseSignupResponse signupUser(String email, String password) {
        String url = supabaseUrl + "/auth/v1/signup";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", anonKey);

        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), SupabaseSignupResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Supabase signup response", e);
        }
    }

    public void createProfile(String userId, String email, String fullName, String userType) {
        String url = supabaseUrl + "/rest/v1/profiles";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supaKey);
        headers.set("Authorization", "Bearer " + supaKey);

        Map<String, Object> body = Map.of(
                "id",userId,
                "email", email,
                "full_name", fullName,
                "user_type", userType

        );
       // System.out.println(body);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public SupabaseLoginResponse loginUser(String email, String password) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supaKey);

        String body = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        ResponseEntity<SupabaseLoginResponse> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), SupabaseLoginResponse.class);

        return response.getBody();
    }
    public String getUserRole(String userId) {
        // PostgREST URL to select only the user_type column for a specific ID
        String url = supabaseUrl + "/rest/v1/profiles?id=eq." + userId + "&select=user_type";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supaKey);
        headers.set("Authorization", "Bearer " + supaKey);
        headers.set("Accept", "application/json");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            // Supabase returns an array: [{"user_role": "teacher"}]
            ObjectMapper mapper = new ObjectMapper();
            java.util.List<java.util.Map<String, Object>> list = mapper.readValue(
                    response.getBody(), new com.fasterxml.jackson.core.type.TypeReference<>() {});

            if (list != null && !list.isEmpty()) {
                return (String) list.get(0).get("user_type");
            }
        } catch (Exception e) {
            System.err.println("Error fetching user role: " + e.getMessage());
        }
        return "student"; // Default fallback
    }
}