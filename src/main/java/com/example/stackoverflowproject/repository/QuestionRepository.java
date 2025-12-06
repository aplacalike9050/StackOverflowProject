package com.example.stackoverflowproject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.stackoverflowproject.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {


}
