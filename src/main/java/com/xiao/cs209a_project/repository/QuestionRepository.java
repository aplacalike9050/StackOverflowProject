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

}