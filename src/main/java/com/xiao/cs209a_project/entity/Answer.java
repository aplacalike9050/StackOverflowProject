package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", unique = true, nullable = false)
    private Long answerId;

    @Column(name = "question_id") // 逻辑外键，关联 Question 的 questionId
    private Long questionId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    private Integer score;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "creation_date")
    private Long creationDate;
    @Column(name = "last_activity_date")
    private Long lastActivityDate;
    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();


}