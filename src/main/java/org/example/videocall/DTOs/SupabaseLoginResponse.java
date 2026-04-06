package org.example.videocall.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseLoginResponse {
    private String access_token;
    private String refresh_token;
    private user user;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class user{
        private String id;
        private String email;}
}
