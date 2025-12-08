package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "post_id")
    private Long postId; // 可以是问题ID或答案ID

    @Column(name = "post_type")
    private String postType; // 'question' 或 'answer'

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    private Integer score;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();

    // 可选：添加关联关系
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", insertable = false, updatable = false)
    private User user;
}