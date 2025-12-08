package com.example.stackoverflowproject.repository;

import com.example.stackoverflowproject.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;



@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

}