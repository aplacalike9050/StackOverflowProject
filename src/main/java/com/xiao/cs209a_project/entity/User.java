package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "display_name")
    private String displayName;

    private Integer reputation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_access_date")
    private LocalDateTime lastAccessDate;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();
}