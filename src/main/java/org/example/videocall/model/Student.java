package org.example.videocall.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "student")
@Data
public class Student {
    @Id
    @Column(name="id",updatable = false,nullable = false)
    private UUID id;

    @Column(name="full_name")
    private String fullName;
    private String course;
    private String rollNumber;
    private String studentClass;
    private String email;
}
