package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByQuestionId(Long questionId);

    boolean existsByQuestionId(Long questionId);

    @Query("SELECT q FROM Question q WHERE q.title LIKE %:keyword% OR q.body LIKE %:keyword%")
    List<Question> findByTitleOrBodyContaining(@Param("keyword") String keyword);

    @Query("SELECT q FROM Question q WHERE q.answerCount > :minAnswers ORDER BY q.answerCount DESC")
    List<Question> findQuestionsWithManyAnswers(@Param("minAnswers") int minAnswers);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.isAnswered = true")
    long countAnsweredQuestions();

    @Query("SELECT q FROM Question q ORDER BY q.creationDate DESC")
    List<Question> findRecentQuestions();

    @Query("SELECT q FROM Question q WHERE q.tagsCache LIKE %:tagName%")
    List<Question> findByTagName(@Param("tagName") String tagName);

    /**
     * 查找包含疑难标签的问题
     */
    @Query("SELECT q FROM Question q WHERE " +
            "LOWER(q.tagsCache) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Question> findByDifficultTag(@Param("tag") String tag);

    /**
     * 查找包含高级主题的问题
     */
    @Query("SELECT q FROM Question q WHERE " +
            "LOWER(q.tagsCache) LIKE LOWER(CONCAT('%', :topic, '%'))")
    List<Question> findByAdvancedTopic(@Param("topic") String topic);

    /**
     * 获取有答案的问题
     */
    List<Question> findByAnswerCountGreaterThan(Integer minAnswers);

    /**
     * 获取高分数问题
     */
    List<Question> findByScoreGreaterThanEqual(Integer minScore);

    /**
     * 获取特定时间范围内的问题
     */
    List<Question> findByCreationDateBetween(Long startTime, Long endTime);

    /**
     * 统计各标签的使用频率
     */
    @Query(value = "SELECT tag, COUNT(*) as usage_count FROM (" +
            "SELECT DISTINCT q.id, TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(q.tags_cache, ',', n.n), ',', -1)) as tag " +
            "FROM question q " +
            "CROSS JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) n " +
            "WHERE CHAR_LENGTH(q.tags_cache) - CHAR_LENGTH(REPLACE(q.tags_cache, ',', '')) >= n.n - 1" +
            ") tags GROUP BY tag ORDER BY usage_count DESC",
            nativeQuery = true)
    List<Object[]> getTagUsageStatistics();

    /**
     * 获取解答率低的标签（解答率 < 10%）
     */
    @Query(value = "SELECT tag, " +
            "COUNT(*) as total_questions, " +
            "SUM(CASE WHEN is_answered = true THEN 1 ELSE 0 END) as answered_questions, " +
            "SUM(CASE WHEN is_answered = true THEN 1 ELSE 0 END) * 100.0 / COUNT(*) as answer_rate " +
            "FROM (" +
            "SELECT q.id, q.is_answered, TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(q.tags_cache, ',', n.n), ',', -1)) as tag " +
            "FROM question q " +
            "CROSS JOIN (SELECT 1 n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) n " +
            "WHERE CHAR_LENGTH(q.tags_cache) - CHAR_LENGTH(REPLACE(q.tags_cache, ',', '')) >= n.n - 1" +
            ") tags GROUP BY tag HAVING answer_rate < 10 OR answer_rate IS NULL",
            nativeQuery = true)
    List<Object[]> getLowAnswerRateTags();

    /**
     * 根据问题ID列表批量查询
     */
    List<Question> findByIdIn(List<Long> ids);

    /**
     * 获取特定标签的问题
     */
    @Query("SELECT q FROM Question q WHERE q.tagsCache LIKE %:tag%")
    List<Question> findByTag(@Param("tag") String tag);
}