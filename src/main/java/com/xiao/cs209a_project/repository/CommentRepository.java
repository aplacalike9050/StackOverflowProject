package com.xiao.cs209a_project.repository;

import com.xiao.cs209a_project.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByCommentId(Long commentId);

    boolean existsByCommentId(Long commentId);

    List<Comment> findByPostId(Long postId);

    List<Comment> findByPostIdAndPostType(Long postId, String postType);

    List<Comment> findByOwnerUserId(Long ownerUserId);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.creationDate ASC")
    List<Comment> findByPostIdOrderByCreationDateAsc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId ORDER BY c.score DESC")
    List<Comment> findByPostIdOrderByScoreDesc(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId")
    long countByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.score > :minScore ORDER BY c.score DESC")
    List<Comment> findHighScoreComments(@Param("minScore") int minScore);

    @Query("SELECT c FROM Comment c WHERE c.creationDate > :sinceDate")
    List<Comment> findCommentsCreatedAfter(@Param("sinceDate") java.time.LocalDateTime sinceDate);
}