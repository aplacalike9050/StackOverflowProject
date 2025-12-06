package com.example.stackoverflowproject.controller;

import com.example.stackoverflowproject.dto.PitfallDTO;
import com.example.stackoverflowproject.dto.SolvabilityDTO;
import com.example.stackoverflowproject.dto.TopicTrendDTO;
import com.example.stackoverflowproject.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")

public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    //任务1接口
    @GetMapping("/trends")
    public List<TopicTrendDTO> getTopicTrend(){
        return analysisService.analyzeTopicTrends();
    }
    //任务2接口
    @GetMapping("/coOccurrence")
    public Map<String, Integer> getCoOccurrence(@RequestParam int topN){
        return analysisService.analyzeCoOccurrence(topN);
    }
    //任务3接口
    @GetMapping("/pitfalls")
    public List<PitfallDTO> getMultithreadingPitfalls(){
        return analysisService.analyzeMultithreadingPitfalls();
    }

    //任务4接口
    @GetMapping("/solvability")
    public List<SolvabilityDTO> getSolvabilityFactors(){
        return analysisService.analyzeSolvabilityFactors();
    }
}
