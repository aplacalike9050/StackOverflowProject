package com.example.stackoverflowproject.service;

import com.example.stackoverflowproject.dto.PitfallDTO;
import com.example.stackoverflowproject.dto.SolvabilityDTO;
import com.example.stackoverflowproject.dto.TopicTrendDTO;

import java.util.List;
import java.util.Map;

public interface AnalysisService {

    // 任务1: 话题趋势
    List<TopicTrendDTO> analyzeTopicTrends();

    // 任务2: 话题共现 (Top N)
    Map<String, Integer> analyzeCoOccurrence(int topN);

    // 任务3: 多线程常见陷阱
    List<PitfallDTO> analyzeMultithreadingPitfalls();

    // 任务4: 易解决 vs 难解决问题对比
    List<SolvabilityDTO> analyzeSolvabilityFactors();
}
