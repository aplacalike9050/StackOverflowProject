package com.example.stackoverflowproject.service;

import com.example.stackoverflowproject.dto.PitfallDTO;
import com.example.stackoverflowproject.dto.SolvabilityDTO;
import com.example.stackoverflowproject.dto.TopicTrendDTO;
import com.example.stackoverflowproject.repository.AnswerRepository;
import com.example.stackoverflowproject.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisServiceImpl implements AnalysisService{
    @Autowired
    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;
    //任务1实现
    @Override
    public List<TopicTrendDTO> analyzeTopicTrends(){
        return new ArrayList<>();
    };
    //任务2实现
    @Override
    public Map<String, Integer> analyzeCoOccurrence(int topN){
        return new HashMap<>();
    }
    //任务3实现
    @Override
    public List<PitfallDTO> analyzeMultithreadingPitfalls(){
        return new ArrayList<>();
    }
    //任务4实现
    @Override
    public List<SolvabilityDTO> analyzeSolvabilityFactors(){
        return new ArrayList<>();
    }
}
