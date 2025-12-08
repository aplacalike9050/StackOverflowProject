package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    private Integer score;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "answer_count")
    private Integer answerCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "favorite_count")
    private Integer favoriteCount;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Column(name = "last_edit_date")
    private LocalDateTime lastEditDate;

    @Column(name = "is_answered")
    private Boolean isAnswered;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(name = "tags_cache", columnDefinition = "TEXT")
    private String tagsCache;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();
}