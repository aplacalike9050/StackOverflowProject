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
     * 分析可解问题与难解问题的六个核心因素差异
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

    /**
     * 批量分析多个因素
     * POST /api/analysis/factors/batch
     */
    @PostMapping("/factors/batch")
    public ResponseEntity<Map<String, Object>> analyzeMultipleFactors(@RequestBody Map<String, Object> request) {
        log.info("批量分析多个因素: {}", request);

        try {
            @SuppressWarnings("unchecked")
            java.util.List<String> factors = (java.util.List<String>) request.get("factors");
            Map<String, Object> result = solvabilityAnalysisService.analyzeMultipleFactors(factors);

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("批量因素分析成功，分析因素数量: {}", factors != null ? factors.size() : 0);
                return ResponseEntity.ok(result);
            } else {
                log.warn("批量因素分析失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("批量因素分析控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "批量因素分析失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取可解性分析的历史记录
     * GET /api/analysis/history
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAnalysisHistory() {
        log.info("获取可解性分析历史记录...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.getAnalysisHistory();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("分析历史查询成功");
                return ResponseEntity.ok(result);
            } else {
                log.warn("分析历史查询失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("分析历史查询控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析历史查询失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 清除分析缓存
     * DELETE /api/analysis/cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearAnalysisCache() {
        log.info("清除分析缓存...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.clearAnalysisCache();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("分析缓存清除成功");
                return ResponseEntity.ok(result);
            } else {
                log.warn("分析缓存清除失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("分析缓存清除控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析缓存清除失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分析配置
     * GET /api/analysis/config
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAnalysisConfig() {
        log.info("获取分析配置...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.getAnalysisConfig();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("分析配置查询成功");
                return ResponseEntity.ok(result);
            } else {
                log.warn("分析配置查询失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("分析配置查询控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析配置查询失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新分析配置
     * PUT /api/analysis/config
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateAnalysisConfig(@RequestBody Map<String, Object> config) {
        log.info("更新分析配置: {}", config);

        try {
            Map<String, Object> result = solvabilityAnalysisService.updateAnalysisConfig(config);

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("分析配置更新成功");
                return ResponseEntity.ok(result);
            } else {
                log.warn("分析配置更新失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("分析配置更新控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析配置更新失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 健康检查端点
     * GET /api/analysis/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("可解性分析服务健康检查...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.healthCheck();

            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(503).body(result);
            }

        } catch (Exception e) {
            log.error("健康检查控制器异常", e);
            return ResponseEntity.status(503).body(Map.of(
                    "success", false,
                    "status", "DOWN",
                    "message", "健康检查失败: " + e.getMessage(),
                    "service", "Question Solvability Analysis Service"
            ));
        }
    }

    /**
     * 获取支持的六个核心因素列表
     * GET /api/analysis/factors
     */
    @GetMapping("/factors")
    public ResponseEntity<Map<String, Object>> getSupportedFactors() {
        log.info("获取支持的六个核心因素列表...");

        try {
            Map<String, Object> result = Map.of(
                    "success", true,
                    "factors", java.util.List.of(
                            Map.of(
                                    "name", "questionComplexity",
                                    "displayName", "问题复杂度",
                                    "description", "分析标签数量、高级主题比例等指标，评估问题的技术复杂度",
                                    "icon", "🎯"
                            ),
                            Map.of(
                                    "name", "difficultTags",
                                    "displayName", "疑难标签",
                                    "description", "分析解答率低于10%的标签，识别难解问题的特征标签",
                                    "icon", "⚠️"
                            ),
                            Map.of(
                                    "name", "userEngagement",
                                    "displayName", "用户参与度",
                                    "description", "分析评论数量、问题分数、回答数、浏览量等指标，评估问题的社区参与程度",
                                    "icon", "👥"
                            ),
                            Map.of(
                                    "name", "userReputation",
                                    "displayName", "用户声誉",
                                    "description", "分析提问者声誉值，评估用户经验对解答率的影响",
                                    "icon", "⭐"
                            ),
                            Map.of(
                                    "name", "timeFactors",
                                    "displayName", "时间因素",
                                    "description", "分析白天/夜间回答比例、回答速度，识别最佳提问时机",
                                    "icon", "⏰"
                            ),
                            Map.of(
                                    "name", "contentQuality",
                                    "displayName", "内容质量",
                                    "description", "分析标题长度、代码片段，评估问题的描述质量",
                                    "icon", "📝"
                            )
                    ),
                    "totalFactors", 6
            );

            log.info("返回支持的六个核心因素列表");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("获取支持因素列表控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取支持因素列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取分析进度状态
     * GET /api/analysis/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getAnalysisProgress() {
        log.debug("获取分析进度状态...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.getAnalysisProgress();

            if (Boolean.TRUE.equals(result.get("success"))) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("分析进度查询控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "分析进度查询失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 停止正在进行的分析
     * POST /api/analysis/stop
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopAnalysis() {
        log.info("停止正在进行的分析...");

        try {
            Map<String, Object> result = solvabilityAnalysisService.stopAnalysis();

            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("分析已成功停止");
                return ResponseEntity.ok(result);
            } else {
                log.warn("停止分析失败: {}", result.get("message"));
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            log.error("停止分析控制器异常", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "停止分析失败: " + e.getMessage()
            ));
        }
    }
}