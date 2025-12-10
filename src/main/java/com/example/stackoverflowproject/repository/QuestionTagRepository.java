package com.example.stackoverflowproject.repository;

import com.example.stackoverflowproject.entity.Question;
import com.example.stackoverflowproject.entity.QuestionTag;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

}