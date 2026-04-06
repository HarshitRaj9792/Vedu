package org.example.videocall.controller;

import lombok.RequiredArgsConstructor;
import org.example.videocall.DTOs.SignupRequest;
import org.example.videocall.DTOs.loginRequest;
import org.example.videocall.DTOs.SupabaseLoginResponse;
import org.example.videocall.DTOs.SupabaseSignupResponse;
import org.example.videocall.service.SupabaseAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;


@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class Authcontroller {
    private final SupabaseAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            SupabaseSignupResponse signupResponse = authService.signupUser(request.getEmail(), request.getPassword());

        String userId = signupResponse.getId();
       // System.out.println("DEBUG: Sending to DB -> Name: " + request.getFullName() + " Type: " + request.getUserType());


        authService.createProfile(userId, request.getEmail(), request.getFullName(), request.getUserType());
      //  System.out.println(request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Signup Successful", "email", request.getEmail()));
    }
    catch (HttpClientErrorException e){
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", "Signup Failed","details", e.getResponseBodyAsString()));
    } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }}

    @PostMapping("/login")
    public ResponseEntity<?>  login(@RequestBody loginRequest request){
        try {
            SupabaseLoginResponse loginResponse = authService.loginUser(request.getEmail(), request.getPassword());
            String userId = loginResponse.getUser().getId();

            String role = authService.getUserRole(userId);

            System.out.println("login token " + loginResponse.getAccess_token());
            System.out.println(role);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of
                            ("message", "Login Successful",
                                    "access_token", loginResponse.getAccess_token(),
                                    "role",role));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message","login failed","details",e.getResponseBodyAsString()));

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",e.getMessage()));
        }
    }
}
