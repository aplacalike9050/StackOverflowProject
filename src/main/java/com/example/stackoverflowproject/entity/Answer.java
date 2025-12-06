package com.example.stackoverflowproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "answers", schema = "stack_schema")
@ToString(exclude = "question")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answer_id", unique = true, nullable = false)
    private Long answerId;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "last_activity_date")
    private Long lastActivityDate;

    @Column(name = "collected_date", insertable = false, updatable = false)
    private Timestamp collectedDate;

    @Column(name = "is_accepted")
    private Boolean isAccepted;

    private Integer score;

    @Column(name = "owner_reputation")
    private Integer ownerReputation;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "owner_display_name")
    private String ownerDisplayName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "question_id", nullable = false)

    private Question question;
}