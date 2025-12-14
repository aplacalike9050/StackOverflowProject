package com.xiao.cs209a_project.service;


import com.xiao.cs209a_project.dto.CoOccurrenceDTO;
import com.xiao.cs209a_project.dto.PitfallDTO;
import com.xiao.cs209a_project.dto.SolvabilityDTO;
import com.xiao.cs209a_project.dto.TopicTrendDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {

    // 任务1: 话题趋势
    List<TopicTrendDTO> analyzeTopicTrends();

    // 任务2: 话题共现 (Top N)
    List<CoOccurrenceDTO> analyzeCoOccurrence(int topN);

    // 任务3: 多线程常见陷阱
    List<PitfallDTO> analyzeMultithreadingPitfalls();

    // 任务4: 易解决 vs 难解决问题对比
    List<SolvabilityDTO> analyzeSolvabilityFactors();

    String exportUniqueTags();
}
