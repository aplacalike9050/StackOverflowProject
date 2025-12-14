package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByAnswerId(Long answerId);

    boolean existsByAnswerId(Long answerId);

    List<Answer> findByQuestionId(Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.questionId = :questionId ORDER BY a.score DESC")
    List<Answer> findByQuestionIdOrderByScoreDesc(@Param("questionId") Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.isAccepted = true AND a.questionId = :questionId")
    Optional<Answer> findAcceptedAnswerByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT COUNT(a) FROM Answer a WHERE a.questionId = :questionId")
    long countByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT a FROM Answer a WHERE a.ownerUserId = :userId")
    List<Answer> findByOwnerUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Answer a WHERE a.score > :minScore ORDER BY a.score DESC")
    List<Answer> findHighScoreAnswers(@Param("minScore") int minScore);


}