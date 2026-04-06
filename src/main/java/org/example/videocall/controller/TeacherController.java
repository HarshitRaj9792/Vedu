package org.example.videocall.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.videocall.model.Schedules;
import org.example.videocall.model.Teacher;

import org.example.videocall.repo.Schedules_repo;
import org.example.videocall.repo.Teacher_repo;
import org.example.videocall.service.TeacherService;
import org.example.videocall.service.TokenService;
import org.example.videocall.service.EmailService;
import org.example.videocall.repo.Student_repo;
import org.example.videocall.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TokenService tokenService;

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private Teacher_repo teacherRepo;
    @Autowired
    private Schedules_repo schedulesRepo;
    @Autowired
    private Student_repo studentRepo;
    @Autowired
    private EmailService emailService;

    @PostMapping("/profile")
    public ResponseEntity<?> updateprofile(@RequestHeader("Authorization") String authHeader, @RequestBody Teacher teacherData){

        String UserId = tokenService.extractUserIdFromToken(authHeader);

        if(UserId == null ){return ResponseEntity.status(401).body("Invalid Token");}
        teacherData.setId(java.util.UUID.fromString(UserId));

        Teacher saved = teacherService.upsertTeacher(teacherData);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getme(@RequestHeader("Authorization") String authHeader){
        String UserId = tokenService.extractUserIdFromToken(authHeader);
        if(UserId == null){return ResponseEntity.status(401).body("Unauthorized: Invalid Token");}
        java.util.UUID id = java.util.UUID.fromString(UserId);
        return teacherRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/saveSchedule")
    public ResponseEntity<?> saveSchedule(@RequestHeader("authorization") String authHeader,@RequestBody Schedules schedulesData){
        String UserId = tokenService.extractUserIdFromToken(authHeader);

        if(UserId == null){return ResponseEntity.status(401).body("Unauthorized: Invalid Token");}
        UUID id = java.util.UUID.fromString(UserId);

        schedulesData.setTeacherid(java.util.UUID.fromString(UserId));
        teacherRepo.findById(id).ifPresent(teacher -> {
            schedulesData.setTeacherName(teacher.getFullName());

        });

        try {
            Schedules saved = schedulesRepo.save(schedulesData);

            // Fetch students linked to this course and class
            List<Student> targetStudents = studentRepo.findByCourseIgnoreCaseAndStudentClassIgnoreCase(
                    saved.getTopicCourse(), saved.getTopicClass());

            // Fire and forget asynchronous email dispatch
            emailService.sendClassNotification(saved, targetStudents);

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Database Error: " + e.getMessage());
        }
    }

    @GetMapping("/my-Schedule")
    public ResponseEntity<?> getMySchedule(@RequestHeader("Authorization") String authHeader){
        String UserId = tokenService.extractUserIdFromToken(authHeader);
        if(UserId == null){return ResponseEntity.status(401).body("Unauthorized: Invalid Token");}
        try {
            java.util.UUID id = java.util.UUID.fromString(UserId);
            List<Schedules> teacherSchecdule= schedulesRepo.findByTeacheridOrderByTopicTimeAscAllIgnoreCase(id);

            if (teacherSchecdule.isEmpty()){
                return ResponseEntity.status(201).body("No schedules found");
            }
            return ResponseEntity.ok(teacherSchecdule);
        }catch (Exception e){return ResponseEntity.status(500).body("Error: "+e.getMessage());}



    }
}
