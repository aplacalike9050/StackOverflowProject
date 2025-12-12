package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.entity.Question;
import com.xiao.cs209a_project.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicAnalysisService {

    private final QuestionRepository questionRepository;

    // 定义我们要分析的Java主题关键词
    private static final Map<String, List<String>> TOPIC_KEYWORDS = Map.of(
            "generics", Arrays.asList("generics", "<T>", "wildcard", "extends", "super"),
            "collections", Arrays.asList("collection", "list", "map", "set", "arraylist", "hashmap"),
            "io", Arrays.asList("file", "inputstream", "outputstream", "reader", "writer", "nio"),
            "lambda", Arrays.asList("lambda", "->", "stream", "filter", "map", "collect"),
            "multithreading", Arrays.asList("thread", "synchronized", "lock", "executor", "concurrent", "volatile"),
            "socket", Arrays.asList("socket", "serversocket", "tcp", "udp", "network"),
            "reflection", Arrays.asList("reflection", "class.forname", "getdeclared", "invoke"),
            "spring-boot", Arrays.asList("spring boot", "@springbootapplication", "autoconfigure"),
            "jpa", Arrays.asList("jpa", "entity", "repository", "@entity", "@repository"),
            "exception", Arrays.asList("exception", "try-catch", "throw", "throws")
    );

    /**
     * 分析指定时间范围内的主题趋势
     */
    public Map<String, Object> analyzeTopicTrends(Long startTime, Long endTime, String activityType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取指定时间范围内的问题
            List<Question> questions = getQuestionsInTimeRange(startTime, endTime);

            // 根据活动类型进行分析
            Map<String, Long> topicActivity = analyzeTopicActivity(questions, activityType);

            // 按时间分组分析趋势
            Map<String, Map<Long, Long>> monthlyTrends = analyzeMonthlyTrends(questions, activityType);

            result.put("success", true);
            result.put("totalQuestions", questions.size());
            result.put("topicActivity", topicActivity);
            result.put("monthlyTrends", monthlyTrends);
            result.put("timeRange", Map.of(
                    "start", startTime,
                    "end", endTime
            ));

        } catch (Exception e) {
            log.error("分析主题趋势时发生错误", e);
            result.put("success", false);
            result.put("message", "分析失败: " + e.getMessage());
        }

        return result;
    }

    private List<Question> getQuestionsInTimeRange(Long startTime, Long endTime) {
        List<Question> allQuestions = questionRepository.findAll();

        return allQuestions.stream()
                .filter(q -> q.getCreationDate() != null)
                .filter(q -> q.getCreationDate() >= startTime && q.getCreationDate() <= endTime)
                .collect(Collectors.toList());
    }

    private Map<String, Long> analyzeTopicActivity(List<Question> questions, String activityType) {
        Map<String, Long> topicCounts = new HashMap<>();

        for (Question question : questions) {
            // 检查问题是否包含各个主题的关键词
            for (Map.Entry<String, List<String>> topicEntry : TOPIC_KEYWORDS.entrySet()) {
                String topic = topicEntry.getKey();
                List<String> keywords = topicEntry.getValue();

                if (containsTopicKeywords(question, keywords)) {
                    long activityValue = calculateActivityValue(question, activityType);
                    topicCounts.put(topic, topicCounts.getOrDefault(topic, 0L) + activityValue);
                }
            }
        }

        return topicCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private boolean containsTopicKeywords(Question question, List<String> keywords) {
        String searchText = (question.getTitle() + " " + question.getBody()).toLowerCase();

        return keywords.stream()
                .anyMatch(keyword -> searchText.contains(keyword.toLowerCase()));
    }

    private long calculateActivityValue(Question question, String activityType) {
        return switch (activityType) {
            case "questions" -> 1; // 单纯计数问题数量
            case "answers" -> question.getAnswerCount() != null ? question.getAnswerCount() : 0;
            case "comments" -> question.getCommentCount() != null ? question.getCommentCount() : 0;
            case "score" -> question.getScore() != null ? Math.max(question.getScore(), 0) : 0;
            case "views" -> question.getViewCount() != null ? question.getViewCount() : 0;
            default -> 1;
        };
    }

    private Map<String, Map<Long, Long>> analyzeMonthlyTrends(List<Question> questions, String activityType) {
        Map<String, Map<Long, Long>> monthlyTrends = new HashMap<>();

        // 按月份分组问题
        Map<Long, List<Question>> questionsByMonth = questions.stream()
                .collect(Collectors.groupingBy(q -> {
                    Instant instant = Instant.ofEpochSecond(q.getCreationDate());
                    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                    // 转换为月份的时间戳（每月第一天）
                    return LocalDateTime.of(dateTime.getYear(), dateTime.getMonth(), 1, 0, 0)
                            .atZone(ZoneId.systemDefault()).toEpochSecond();
                }));

        // 为每个主题分析月度趋势
        for (String topic : TOPIC_KEYWORDS.keySet()) {
            Map<Long, Long> monthlyData = new TreeMap<>();

            for (Map.Entry<Long, List<Question>> monthEntry : questionsByMonth.entrySet()) {
                Long monthTimestamp = monthEntry.getKey();
                List<Question> monthlyQuestions = monthEntry.getValue();

                long monthlyActivity = monthlyQuestions.stream()
                        .filter(q -> containsTopicKeywords(q, TOPIC_KEYWORDS.get(topic)))
                        .mapToLong(q -> calculateActivityValue(q, activityType))
                        .sum();

                monthlyData.put(monthTimestamp, monthlyActivity);
            }

            monthlyTrends.put(topic, monthlyData);
        }

        return monthlyTrends;
    }

    /**
     * 获取可用的时间范围
     */
    public Map<String, Object> getAvailableTimeRange() {
        List<Question> allQuestions = questionRepository.findAll();

        OptionalLong minTime = allQuestions.stream()
                .filter(q -> q.getCreationDate() != null)
                .mapToLong(Question::getCreationDate)
                .min();

        OptionalLong maxTime = allQuestions.stream()
                .filter(q -> q.getCreationDate() != null)
                .mapToLong(Question::getCreationDate)
                .max();

        Map<String, Object> result = new HashMap<>();
        if (minTime.isPresent() && maxTime.isPresent()) {
            result.put("success", true);
            result.put("minTime", minTime.getAsLong());
            result.put("maxTime", maxTime.getAsLong());
            result.put("totalQuestions", allQuestions.size());
        } else {
            result.put("success", false);
            result.put("message", "没有可用的数据");
        }

        return result;
    }
}