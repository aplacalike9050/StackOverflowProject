package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "question_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "tag_id"}))
public class QuestionTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "collected_at")
    private java.time.LocalDateTime collectedAt = java.time.LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;
}