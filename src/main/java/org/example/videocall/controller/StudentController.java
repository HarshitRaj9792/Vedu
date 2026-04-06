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
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,@RequestBody Student studentData){

    String UserId = tokenService.extractUserIdFromToken(authHeader);
    if(UserId.isEmpty()){return ResponseEntity.status(401).body("Unauthorized : Invalid Token");}
    studentData.setId(java.util.UUID.fromString(UserId));

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

        if(UserId.isEmpty()){return ResponseEntity.status(401).body("Unauthorized : Invalid Token");}
        java.util.UUID userId = java.util.UUID.fromString(UserId);

        return student_repo.findById(userId).map(student -> {

          /*  System.out.println("DEBUG - Student ID: " + userId);
            System.out.println("DEBUG - Fetching for Course: [" + student.getCourse() + "]");
            System.out.println("DEBUG - Fetching for Class: [" + student.getStudentClass() + "]");*/
            // --- FOR DEBUGGING --//

            List<Schedules> sorted_Schedules = schedules_repo
                    .findByTopicCourseAndTopicClassOrderByTopicTimeAscAllIgnoreCase(student.getCourse(), student.getStudentClass());
            return  ResponseEntity.ok(sorted_Schedules);
        }).orElse(ResponseEntity.status(404).build());
    }
}
