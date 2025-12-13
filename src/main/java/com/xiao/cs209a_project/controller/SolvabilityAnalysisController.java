package com.xiao.cs209a_project.controller;

import com.xiao.cs209a_project.service.QuestionSolvabilityAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class SolvabilityAnalysisController {

    private final QuestionSolvabilityAnalysisService solvabilityAnalysisService;

    /**
     * 分析可解问题与难解问题的五个核心因素差异
     * GET /api/analysis/solvability-factors
     */
    @GetMapping("/solvability-factors")
    public ResponseEntity<Map<String, Object>> analyzeSolvabilityFactors() {
        log.info("开始分析问题可解性因素...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.analyzeSolvabilityFactors();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("可解性分析成功完成，分析问题总数: {}", result.get("totalQuestions"));
                return ResponseEntity.ok(result);
            } else {
                log.warn("可解性分析失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("可解性分析控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析服务内部错误: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分析统计信息
     * GET /api/analysis/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAnalysisStatistics() {
        log.info("获取分析统计信息...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.getAnalysisStatistics();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("统计信息查询成功，问题总数: {}", result.get("totalQuestions"));
                return ResponseEntity.ok(result);
            } else {
                log.warn("统计信息查询失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("统计信息查询控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "统计信息查询失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取特定因素的分析结果
     * GET /api/analysis/factor/{factorName}
     */
    @GetMapping("/factor/{factorName}")
    public ResponseEntity<Map<String, Object>> getFactorAnalysis(@PathVariable String factorName) {
        log.info("获取特定因素分析结果: {}", factorName);

        try {
            Map<String, Object> result = solvabilityAnalysisService.getFactorAnalysis(factorName);

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("因素分析查询成功: {}", factorName);
                return ResponseEntity.ok(result);
            } else {
                log.warn("因素分析查询失败: {} - {}", factorName, result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("因素分析查询控制器异常: {}", factorName, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "因素分析查询失败: " + e.getMessage(),
                    "factor", factorName
            ));
        }
    }
}