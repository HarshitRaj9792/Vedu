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
        return student_repo.findById(studentRequest.getId()).map(existingStudent ->{
            existingStudent.setFullName(studentRequest.getFullName());
            existingStudent.setEmail(studentRequest.getEmail());
            existingStudent.setStudentClass(studentRequest.getStudentClass());
            existingStudent.setRollNumber(studentRequest.getRollNumber());
            existingStudent.setCourse(studentRequest.getCourse());

            return student_repo.save(existingStudent);
        }).orElseGet(() -> student_repo.save(studentRequest));
    }
}
