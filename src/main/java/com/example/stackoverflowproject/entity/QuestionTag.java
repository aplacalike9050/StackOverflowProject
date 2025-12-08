package com.example.stackoverflowproject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "question_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "tag_id"}))
public class QuestionTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "question_id")
    private Long questionId;

    // 如果 Tag 表有 ID，这里存 Tag 的主键 ID。
    @Column(name = "tag_id")
    private Long tagId;

    // 只要保证插入 QuestionTag 之前，Tag 表里已经有这个 Tag 了。

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();

}