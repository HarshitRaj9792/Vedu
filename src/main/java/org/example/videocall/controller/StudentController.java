package org.example.videocall.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.model.Schedules;
import org.example.videocall.model.Student;

import org.example.videocall.repo.Schedules_repo;
import org.example.videocall.repo.Student_repo;
import org.example.videocall.service.StudentService;
import org.example.videocall.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/student")
public class StudentController {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private Student_repo student_repo;
    @Autowired
    private Schedules_repo schedules_repo;

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader, @RequestBody Student studentData){

        String UserId = tokenService.extractUserIdFromToken(authHeader);
        if (UserId == null || UserId.isEmpty()) { return ResponseEntity.status(401).body("Unauthorized : Invalid Token"); }
        studentData.setId(java.util.UUID.fromString(UserId));

        // Extract email directly from the verified JWT — no need for the client to send it
        String email = tokenService.extractEmailFromToken(authHeader);
        if (email != null && !email.isEmpty()) studentData.setEmail(email);

        Student saved = studentService.upsertStudent(studentData);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader){
        String UserId = tokenService.extractUserIdFromToken(authHeader);
        if(UserId.isEmpty()){return ResponseEntity.status(401).body("Unauthorized : Invalid Token");}
        java.util.UUID userId = java.util.UUID.fromString(UserId);
        return  student_repo.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());

    }

    @GetMapping("/my-schedules")
    public ResponseEntity<?> getMySchedules(@RequestHeader("Authorization") String authHeader){
        String UserId = tokenService.extractUserIdFromToken(authHeader);
        if (UserId == null || UserId.isEmpty()) {
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }

        java.util.UUID userId = java.util.UUID.fromString(UserId);

        // If profile not yet saved, return empty list with a hint
        java.util.Optional<Student> studentOpt = student_repo.findById(userId);
        if (studentOpt.isEmpty()) {
            // Student hasn't set up their profile yet — return empty with header hint
            return ResponseEntity.ok()
                    .header("X-Vedu-Hint", "profile-incomplete")
                    .body(java.util.List.of());
        }

        Student student = studentOpt.get();

        // Guard: if course or class not filled, can't match schedules
        if (student.getCourse() == null || student.getCourse().isBlank()
                || student.getStudentClass() == null || student.getStudentClass().isBlank()) {
            return ResponseEntity.ok()
                    .header("X-Vedu-Hint", "profile-incomplete")
                    .body(java.util.List.of());
        }

        List<Schedules> schedules = schedules_repo
                .findByTopicCourseAndTopicClassOrderByTopicTimeAscAllIgnoreCase(
                        student.getCourse(), student.getStudentClass());

        return ResponseEntity.ok(schedules);
    }
}
