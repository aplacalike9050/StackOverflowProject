package com.example.stackoverflowproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "questions", schema = "stack_schema")
@ToString(exclude = "answers")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", unique = true, nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "last_activity_date")
    private Long lastActivityDate;

    @Column(name = "collected_date", insertable = false, updatable = false)

    private Timestamp collectedDate;

    @Column(name = "is_answered")
    private Boolean isAnswered;

    @Column(name = "accepted_answer_id")
    private Long acceptedAnswerId;

    private Integer score;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "answer_count")
    private Integer answerCount;

    @Column(name = "owner_reputation")
    private Integer ownerReputation;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "owner_display_name")
    private String ownerDisplayName;


    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Answer> answers;
}