package com.example.stackoverflowproject.repository;

import com.example.stackoverflowproject.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 查问题下的评论
    List<Comment> findByPostIdAndPostType(Long postId, String postType);

    // 查一组回答下的所有评论
    List<Comment> findByPostIdInAndPostType(List<Long> postIds, String postType);

}