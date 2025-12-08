package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    private Integer score;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "last_edit_date")
    private LocalDateTime lastEditDate;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();
}