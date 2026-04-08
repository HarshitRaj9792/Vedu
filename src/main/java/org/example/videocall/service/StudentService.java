package org.example.videocall.service;

import jakarta.transaction.Transactional;
import org.example.videocall.model.Student;
import org.example.videocall.repo.Student_repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private Student_repo student_repo;
    @Transactional
    public Student upsertStudent(Student studentRequest)
    {
        // Normalize course to UPPERCASE before any DB operation (covers both insert and update)
        if (studentRequest.getCourse() != null) {
            studentRequest.setCourse(studentRequest.getCourse().toUpperCase().trim());
        }

        return student_repo.findById(studentRequest.getId()).map(existingStudent ->{
            existingStudent.setFullName(studentRequest.getFullName());
            existingStudent.setStudentClass(studentRequest.getStudentClass());
            existingStudent.setRollNumber(studentRequest.getRollNumber());
            existingStudent.setCourse(studentRequest.getCourse());
            // Only update email if explicitly provided — never null out an existing value
            if (studentRequest.getEmail() != null && !studentRequest.getEmail().isEmpty()) {
                existingStudent.setEmail(studentRequest.getEmail());
            }

            return student_repo.save(existingStudent);
        }).orElseGet(() -> student_repo.save(studentRequest));
    }
}
