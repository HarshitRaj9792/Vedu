package org.example.videocall.DTOs;

import lombok.Data;

@Data
public class SignupRequest {
   private String email;
   private String password;
   private String fullName;
   private String userType;
}

