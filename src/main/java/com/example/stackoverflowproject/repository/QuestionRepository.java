package com.example.stackoverflowproject.repository;

import com.example.stackoverflowproject.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q.tagsCache FROM Question q WHERE q.tagsCache IS NOT NULL AND q.tagsCache <> ''")
    List<String> findAllTags();
    // 初步筛选：只分析带有多线程标签的问题，缩小分析范围
    @Query("SELECT q FROM Question q WHERE " +
            // 1. 核心大类
            "q.tagsCache LIKE '%multithreading%' OR " +
            "q.tagsCache LIKE '%concurrency%' OR " +
            "q.tagsCache LIKE '%java-threads%' OR " +
            "q.tagsCache LIKE '%asynchronous%' OR " +
            "q.tagsCache LIKE '%parallel-processing%' OR " +

            // 2. 核心API与工具
            "q.tagsCache LIKE '%java.util.concurrent%' OR " +
            "q.tagsCache LIKE '%executorservice%' OR " +
            "q.tagsCache LIKE '%threadpoolexecutor%' OR " +
            "q.tagsCache LIKE '%forkjoinpool%' OR " +
            "q.tagsCache LIKE '%completable-future%' OR " +
            "q.tagsCache LIKE '%concurrenthashmap%' OR " +
            "q.tagsCache LIKE '%virtual-threads%' OR " +

            // 3. 同步与锁机制
            "q.tagsCache LIKE '%synchronization%' OR " +
            "q.tagsCache LIKE '%locking%' OR " +
            "q.tagsCache LIKE '%volatile%' OR " +
            "q.tagsCache LIKE '%atomic%' OR " +

            // 4. 典型问题
            "q.tagsCache LIKE '%deadlock%' OR " +
            "q.tagsCache LIKE '%race-condition%'")
    List<Question> findMultithreadingRelatedQuestions();
}