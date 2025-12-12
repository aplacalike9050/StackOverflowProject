package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "display_name")
    private String displayName;

    private Integer reputation;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();

    @Column(name = "creation_date")
    private Long creationDate; // 存 Unix 时间戳

}