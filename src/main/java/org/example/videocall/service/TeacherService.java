package org.example.videocall.service;

import jakarta.transaction.Transactional;
import org.example.videocall.model.Teacher;
import org.example.videocall.repo.Teacher_repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    @Autowired
    private Teacher_repo teacher_repo;
    @Transactional
    public Teacher upsertTeacher(Teacher teacherRequest){
        return teacher_repo.findById(teacherRequest.getId()).map(existingTeacher ->{
            existingTeacher.setFullName(teacherRequest.getFullName());
            existingTeacher.setSpecialization(teacherRequest.getSpecialization());
            existingTeacher.setDescription(teacherRequest.getDescription());
            existingTeacher.setEmployeeId(teacherRequest.getEmployeeId());

            return teacher_repo.save(existingTeacher);
        }).orElseGet(() -> teacher_repo.save(teacherRequest));
    }
}
