package org.example.videocall.model;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;


@Entity
@Table(name = "teacher")
@Data
public class Teacher {

    @Id
    @Column(name="id",updatable = false,nullable = false)
    private UUID id;

    @Column(name = "full_name")
    private String fullName;
    private String specialization;
    @Column(columnDefinition = "Text")
    private String description;

    private String employeeId;
    @Column(columnDefinition = "Text")
    private String email;

}
