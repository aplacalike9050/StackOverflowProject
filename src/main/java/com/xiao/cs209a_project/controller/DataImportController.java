package com.xiao.cs209a_project.controller;

import com.xiao.cs209a_project.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DataImportController {

    private final QuestionImportService questionImportService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 一键导入API：导入问题 + 答案 + 用户 + 标签
     */
    @PostMapping("/api/import/questions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importQuestions(
            @RequestParam(defaultValue = "10") int count) {

        Map<String, Object> result = new HashMap<>();

        try {
            long beforeCount = questionImportService.getImportedCount();
            int imported = questionImportService.importQuestions(count);
            long afterCount = questionImportService.getImportedCount();

            result.put("success", true);
            result.put("importedCount", imported);
            result.put("beforeCount", beforeCount);
            result.put("afterCount", afterCount);
            result.put("message", String.format("成功导入 %d 个问题及其所有答案、用户和标签数据", imported));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 批量处理所有问题的标签
     */
    @PostMapping("/api/process-all-tags")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processAllTags() {
        Map<String, Object> result = new HashMap<>();

        try {
            questionImportService.processAllQuestionsTags();
            result.put("success", true);
            result.put("message", "开始批量处理所有问题的标签数据");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量处理标签失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取最常用标签
     */
    @GetMapping("/api/tags/top")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopTags(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            var topTags = questionImportService.getTopTags(limit);
            result.put("success", true);
            result.put("topTags", topTags);
            result.put("message", String.format("获取前 %d 个最常用标签成功", limit));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取最常用标签失败: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            long questionCount = questionImportService.getImportedCount();
            result.put("questionCount", questionCount);
            result.put("status", "运行正常");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("questionCount", 0);
            result.put("status", "服务异常: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Stack Overflow Data Importer");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
}