package org.example.videocall.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseSignupResponse {
    // Supabase returns the data inside a "user" field
    private User user;


    public String getId() {
        return (user != null) ? user.getId() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String id;
        private String email;
    }
}