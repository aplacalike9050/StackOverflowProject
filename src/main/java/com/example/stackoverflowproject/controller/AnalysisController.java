package com.example.stackoverflowproject.controller;

import com.example.stackoverflowproject.dto.CoOccurrenceDTO;
import com.example.stackoverflowproject.dto.PitfallDTO;
import com.example.stackoverflowproject.dto.SolvabilityDTO;
import com.example.stackoverflowproject.dto.TopicTrendDTO;
import com.example.stackoverflowproject.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    //任务1接口
    @GetMapping("/trends")
    public List<TopicTrendDTO> getTopicTrend(){
        return analysisService.analyzeTopicTrends();
    }
    //任务2接口
    //http://localhost:8080/api/analysis/coOccurrence?topN=10
    @GetMapping("/coOccurrence")
    public ResponseEntity<List<CoOccurrenceDTO>> getCoOccurrence(@RequestParam(defaultValue = "10") int topN){
        return ResponseEntity.ok(analysisService.analyzeCoOccurrence(topN));
    }
    //任务3接口
    @GetMapping("/pitfalls")
    public ResponseEntity<List<PitfallDTO>> getMultithreadingPitfalls(){
        return ResponseEntity.ok(analysisService.analyzeMultithreadingPitfalls());
    }
//额外的统计不重复标签接口
    @GetMapping("/export-tags")
    public ResponseEntity<String> exportTags() {
        return ResponseEntity.ok(analysisService.exportUniqueTags());
    }
    //任务4接口
    @GetMapping("/solvability")
    public List<SolvabilityDTO> getSolvabilityFactors(){
        return analysisService.analyzeSolvabilityFactors();
    }
}
