package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    List<QuestionTag> findByQuestionId(Long questionId);

    List<QuestionTag> findByTagId(Long tagId);

    boolean existsByQuestionIdAndTagId(Long questionId, Long tagId);

    @Query("SELECT qt.tagId FROM QuestionTag qt WHERE qt.questionId = :questionId")
    List<Long> findTagIdsByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT qt.questionId FROM QuestionTag qt WHERE qt.tagId = :tagId")
    List<Long> findQuestionIdsByTagId(@Param("tagId") Long tagId);

    @Query("SELECT COUNT(DISTINCT qt.questionId) FROM QuestionTag qt WHERE qt.tagId = :tagId")
    long countQuestionsByTagId(@Param("tagId") Long tagId);

    @Query("SELECT COUNT(qt) FROM QuestionTag qt WHERE qt.tagId = :tagId")
    long countTagUsage(@Param("tagId") Long tagId);

    @Query("SELECT qt.tagId, COUNT(qt.questionId) FROM QuestionTag qt GROUP BY qt.tagId ORDER BY COUNT(qt.questionId) DESC")
    List<Object[]> findTagUsageStatistics();

    // 标签共现分析查询
    @Query("SELECT qt2.tagId, COUNT(qt2.questionId) " +
            "FROM QuestionTag qt1 " +
            "JOIN QuestionTag qt2 ON qt1.questionId = qt2.questionId " +
            "WHERE qt1.tagId = :tagId AND qt2.tagId != :tagId " +
            "GROUP BY qt2.tagId " +
            "ORDER BY COUNT(qt2.questionId) DESC")
    List<Object[]> findCoOccurringTags(@Param("tagId") Long tagId);

    @Query("SELECT qt FROM QuestionTag qt WHERE qt.questionId IN :questionIds")
    List<QuestionTag> findByQuestionIds(@Param("questionIds") List<Long> questionIds);

    @Query("SELECT DISTINCT qt.tagId FROM QuestionTag qt")
    List<Long> findAllDistinctTagIds();
}