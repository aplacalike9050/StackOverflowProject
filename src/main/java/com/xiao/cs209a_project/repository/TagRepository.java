package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTagName(String tagName);

    boolean existsByTagName(String tagName);

    List<Tag> findByTagNameIn(List<String> tagNames);

    @Query("SELECT t FROM Tag t WHERE t.tagName LIKE %:keyword%")
    List<Tag> findByTagNameContaining(@Param("keyword") String keyword);

    @Modifying
    // 修复：t.tagId 改为 t.id (因为 Tag 实体的主键是 id)
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :tagId")
    void incrementUsageCount(@Param("tagId") Long tagId);

    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC")
    List<Tag> findTopTagsByUsageCount();

    @Query("SELECT t FROM Tag t WHERE t.usageCount > :minUsage ORDER BY t.usageCount DESC")
    List<Tag> findPopularTags(@Param("minUsage") int minUsage);

    @Query("SELECT COUNT(t) FROM Tag t")
    long countAllTags();
}