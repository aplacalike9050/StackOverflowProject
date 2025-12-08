package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comment_id", unique = true)
    private Long commentId;

    // 核心修改：只存 ID 和类型，不建立物理外键关联
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "post_type")
    private String postType; // "question" or "answer"

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    private Integer score;

    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();

    // 删除了所有的 @ManyToOne 关联，保证数据纯净导入


}