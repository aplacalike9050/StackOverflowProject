package com.xiao.cs209a_project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", unique = true, length = 60)
    private String tagName;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "collected_at")
    private java.time.LocalDateTime collectedAt = java.time.LocalDateTime.now();
}