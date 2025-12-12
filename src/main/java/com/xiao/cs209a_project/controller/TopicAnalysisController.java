package com.xiao.cs209a_project.controller;

import com.xiao.cs209a_project.service.TopicAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class TopicAnalysisController {

    private final TopicAnalysisService topicAnalysisService;

    /**
     * 分析主题趋势的REST端点
     */
    @GetMapping("/topic-trends")
    public ResponseEntity<Map<String, Object>> analyzeTopicTrends(
            @RequestParam(defaultValue = "0") Long startTime,
            @RequestParam(defaultValue = "0") Long endTime,
            @RequestParam(defaultValue = "questions") String activityType) {

        // 如果没有提供时间范围，使用默认的过去3年
        long defaultEndTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
        long defaultStartTime = defaultEndTime - (3L * 365 * 24 * 60 * 60); // 3年前

        long actualStartTime = startTime > 0 ? startTime : defaultStartTime;
        long actualEndTime = endTime > 0 ? endTime : defaultEndTime;

        Map<String, Object> result = topicAnalysisService.analyzeTopicTrends(
                actualStartTime, actualEndTime, activityType);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取支持的活动类型
     */
    @GetMapping("/activity-types")
    public ResponseEntity<Map<String, Object>> getActivityTypes() {
        Map<String, Object> result = Map.of(
                "success", true,
                "activityTypes", Map.of(
                        "questions", "问题数量",
                        "answers", "答案数量",
                        "comments", "评论数量",
                        "score", "问题分数",
                        "views", "浏览数量"
                )
        );
        return ResponseEntity.ok(result);
    }
}