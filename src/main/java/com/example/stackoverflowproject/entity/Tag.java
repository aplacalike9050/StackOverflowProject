package com.example.stackoverflowproject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "tag_name", unique = true, nullable = false)
    private String tagName;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt = LocalDateTime.now();

}