package com.example.stackoverflowproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questions")
public class Question {

    // 1. 增加独立的数据库自增主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 2. Stack Overflow 的原始 ID，作为业务唯一键
    @Column(name = "question_id", unique = true, nullable = false)
    private Long questionId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    // 只需要存ID，不需要强关联 User 对象，防止 User 没爬取导致报错
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    private Integer score;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "answer_count")
    private Integer answerCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    // 3. 时间改为 Long，直接存 API 返回的时间戳，无需转换
    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "last_activity_date")
    private Long lastActivityDate;
    @Column(name = "is_answered")
    private Boolean isAnswered;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    @Column(name = "tags_cache", columnDefinition = "TEXT")
    private String tagsCache;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();


}