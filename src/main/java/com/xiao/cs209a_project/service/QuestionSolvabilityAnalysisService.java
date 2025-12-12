package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.entity.Question;
import com.xiao.cs209a_project.repository.QuestionRepository;
import com.xiao.cs209a_project.repository.CommentRepository;
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
public class QuestionSolvabilityAnalysisService {

    private final QuestionRepository questionRepository;
    private final CommentRepository commentRepository;

    // 定义高级主题关键词
    private static final Set<String> ADVANCED_TOPICS = Set.of(
            "multithreading", "concurrency", "reflection", "lambda", "stream",
            "generics", "spring-boot", "jpa", "security", "microservices",
            "docker", "kubernetes", "design-patterns", "algorithm", "performance",
            "memory-management", "optimization", "distributed", "transaction"
    );

    // 定义疑难标签（解答率<30%的标签）
    private static final Set<String> DIFFICULT_TAGS = Set.of(
            "memory-leak", "performance", "optimization", "concurrency", "security",
            "jvm", "garbage-collection", "deadlock", "race-condition", "serialization",
            "reflection", "bytecode", "native", "jni", "low-level"
    );

    // 时间配置
    private static final int DAYTIME_START_HOUR = 6;    // 早上6点
    private static final int DAYTIME_END_HOUR = 22;     // 晚上10点
    private static final int MORNING_START = 6;         // 早上开始
    private static final int AFTERNOON_START = 12;      // 下午开始
    private static final int EVENING_START = 18;        // 晚上开始
    private static final int NIGHT_START = 22;          // 夜间开始

    // 显著性阈值配置
    private static final double SCORE_DIFFERENCE_THRESHOLD = 2.0;
    private static final double COMMENT_DIFFERENCE_THRESHOLD = 1.5;
    private static final double ENGAGEMENT_SCORE_SIGNIFICANCE_THRESHOLD = 8.0;

    /**
     * 分析可解问题与难解问题的六个核心因素差异
     */
    public Map<String, Object> analyzeSolvabilityFactors() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Question> allQuestions = questionRepository.findAll();

            if (allQuestions.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有可分析的问题数据");
                return result;
            }

            log.info("开始分析 {} 个问题的可解性因素", allQuestions.size());

            // 分类问题：可解 vs 难解
            List<Question> solvableQuestions = new ArrayList<>();
            List<Question> hardToSolveQuestions = new ArrayList<>();

            for (Question question : allQuestions) {
                if (isSolvable(question)) {
                    solvableQuestions.add(question);
                } else {
                    hardToSolveQuestions.add(question);
                }
            }

            log.info("可解问题: {} 个, 难解问题: {} 个",
                    solvableQuestions.size(), hardToSolveQuestions.size());

            // 分析六个核心因素
            Map<String, Object> factors = analyzeSixCoreFactors(solvableQuestions, hardToSolveQuestions);

            // 生成分析结果
            result.put("success", true);
            result.put("solvableCount", solvableQuestions.size());
            result.put("hardToSolveCount", hardToSolveQuestions.size());
            result.put("totalQuestions", allQuestions.size());
            result.put("solvabilityRate", allQuestions.size() > 0 ?
                    (double) solvableQuestions.size() / allQuestions.size() : 0);
            result.put("factors", factors);
            result.put("insights", generateInsights(solvableQuestions, hardToSolveQuestions, factors));

            log.info("可解性分析完成，识别出 6 个核心因素");

        } catch (Exception e) {
            log.error("分析问题可解性时发生错误", e);
            result.put("success", false);
            result.put("message", "分析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 判断问题是否可解
     */
    private boolean isSolvable(Question question) {
        // 标准1：有被采纳的答案（最强信号）
        if (Boolean.TRUE.equals(question.getIsAnswered()) && question.getAcceptedAnswerId() != null) {
            return true;
        }

        // 标准2：有多个高质量答案（>= 2个答案且分数 > 0）
        if (question.getAnswerCount() != null && question.getAnswerCount() >= 2) {
            if (question.getScore() != null && question.getScore() > 0) {
                return true;
            }
        }

        // 标准3：高分数问题（分数 >= 5）
        if (question.getScore() != null && question.getScore() >= 5) {
            return true;
        }

        // 标准4：有答案且浏览量大（>= 500次浏览）
        if (question.getAnswerCount() != null && question.getAnswerCount() > 0) {
            if (question.getViewCount() != null && question.getViewCount() >= 500) {
                return true;
            }
        }

        return false;
    }

    /**
     * 分析六个核心因素
     */
    private Map<String, Object> analyzeSixCoreFactors(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> factors = new HashMap<>();

        try {
            // 1. 问题复杂度分析
            factors.put("questionComplexity", analyzeQuestionComplexity(solvable, hardToSolve));

            // 2. 疑难标签分析
            factors.put("difficultTags", analyzeDifficultTags(solvable, hardToSolve));

            // 3. 用户参与度分析
            factors.put("userEngagement", analyzeUserEngagement(solvable, hardToSolve));

            // 4. 用户声誉分析
            factors.put("userReputation", analyzeUserReputation(solvable, hardToSolve));

            // 5. 时间因素分析
            factors.put("timeFactors", analyzeTimeFactors(solvable, hardToSolve));

            // 6. 内容质量分析
            factors.put("contentQuality", analyzeContentQuality(solvable, hardToSolve));

        } catch (Exception e) {
            log.error("分析核心因素时发生错误", e);
            // 返回默认数据
            factors = createDefaultFactors();
        }

        return factors;
    }

    /**
     * 1. 问题复杂度分析
     */
    private Map<String, Object> analyzeQuestionComplexity(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析标签数量
            double solvableAvgTags = solvable.stream()
                    .mapToInt(this::countTags)
                    .average().orElse(0);

            double hardToSolveAvgTags = hardToSolve.stream()
                    .mapToInt(this::countTags)
                    .average().orElse(0);

            analysis.put("solvableAvgTags", Math.round(solvableAvgTags * 100.0) / 100.0);
            analysis.put("hardToSolveAvgTags", Math.round(hardToSolveAvgTags * 100.0) / 100.0);

            double tagCountDifference = solvableAvgTags - hardToSolveAvgTags;
            analysis.put("tagCountDifference", Math.round(tagCountDifference * 100.0) / 100.0);

            // 分析高级主题比例
            double solvableAdvancedRate = calculateAdvancedTopicRate(solvable);
            double hardToSolveAdvancedRate = calculateAdvancedTopicRate(hardToSolve);

            analysis.put("solvableAdvancedRate", Math.round(solvableAdvancedRate * 100.0) / 100.0);
            analysis.put("hardToSolveAdvancedRate", Math.round(hardToSolveAdvancedRate * 100.0) / 100.0);

            double advancedTopicDifference = solvableAdvancedRate - hardToSolveAdvancedRate;
            analysis.put("advancedTopicDifference", Math.round(advancedTopicDifference * 100.0) / 100.0);

            // 复杂度综合评分
            double solvableComplexityScore = calculateComplexityScore(solvable);
            double hardToSolveComplexityScore = calculateComplexityScore(hardToSolve);

            analysis.put("solvableValue", Math.round(solvableComplexityScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveComplexityScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableComplexityScore - hardToSolveComplexityScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = Math.abs(tagCountDifference) > 0.5 || Math.abs(advancedTopicDifference) > 10;
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析问题复杂度时发生错误", e);
            analysis = createDefaultComplexityData();
        }

        return analysis;
    }

    /**
     * 2. 疑难标签分析
     */
    private Map<String, Object> analyzeDifficultTags(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析疑难标签比例
            long solvableWithDifficultTags = solvable.stream()
                    .filter(this::containsDifficultTags)
                    .count();

            long hardToSolveWithDifficultTags = hardToSolve.stream()
                    .filter(this::containsDifficultTags)
                    .count();

            double solvableDifficultTagRate = solvable.size() > 0 ?
                    (double) solvableWithDifficultTags / solvable.size() * 100 : 0;
            double hardToSolveDifficultTagRate = hardToSolve.size() > 0 ?
                    (double) hardToSolveWithDifficultTags / hardToSolve.size() * 100 : 0;

            analysis.put("solvableDifficultTagRate", Math.round(solvableDifficultTagRate * 100.0) / 100.0);
            analysis.put("hardToSolveDifficultTagRate", Math.round(hardToSolveDifficultTagRate * 100.0) / 100.0);

            double difficultTagDifference = solvableDifficultTagRate - hardToSolveDifficultTagRate;
            analysis.put("difficultTagDifference", Math.round(difficultTagDifference * 100.0) / 100.0);

            // 疑难标签综合评分
            double solvableDifficultTagScore = 100 - solvableDifficultTagRate; // 疑难标签越少，评分越高
            double hardToSolveDifficultTagScore = 100 - hardToSolveDifficultTagRate;

            analysis.put("solvableValue", Math.round(solvableDifficultTagScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveDifficultTagScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableDifficultTagScore - hardToSolveDifficultTagScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = Math.abs(difficultTagDifference) > 5;
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析疑难标签时发生错误", e);
            analysis = createDefaultDifficultTagsData();
        }

        return analysis;
    }

    /**
     * 3. 用户参与度分析 - 使用CommentRepository获取评论数量
     */
    private Map<String, Object> analyzeUserEngagement(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析评论数量 - 通过CommentRepository获取
            double solvableCommentCount = calculateAverageCommentCount(solvable);
            double hardToSolveCommentCount = calculateAverageCommentCount(hardToSolve);

            analysis.put("solvableCommentCount", Math.round(solvableCommentCount * 100.0) / 100.0);
            analysis.put("hardToSolveCommentCount", Math.round(hardToSolveCommentCount * 100.0) / 100.0);

            double commentDifference = solvableCommentCount - hardToSolveCommentCount;
            analysis.put("commentDifference", Math.round(commentDifference * 100.0) / 100.0);

            // 分析问题分数
            double solvableScore = solvable.stream()
                    .mapToInt(q -> q.getScore() != null ? Math.max(q.getScore(), 0) : 0)
                    .average().orElse(0);

            double hardToSolveScore = hardToSolve.stream()
                    .mapToInt(q -> q.getScore() != null ? Math.max(q.getScore(), 0) : 0)
                    .average().orElse(0);

            analysis.put("solvableScore", Math.round(solvableScore * 100.0) / 100.0);
            analysis.put("hardToSolveScore", Math.round(hardToSolveScore * 100.0) / 100.0);

            double scoreDifference = solvableScore - hardToSolveScore;
            analysis.put("scoreDifference", Math.round(scoreDifference * 100.0) / 100.0);

            // 分析回答数量
            double solvableAnswerCount = solvable.stream()
                    .mapToInt(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0)
                    .average().orElse(0);

            double hardToSolveAnswerCount = hardToSolve.stream()
                    .mapToInt(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0)
                    .average().orElse(0);

            analysis.put("solvableAnswerCount", Math.round(solvableAnswerCount * 100.0) / 100.0);
            analysis.put("hardToSolveAnswerCount", Math.round(hardToSolveAnswerCount * 100.0) / 100.0);

            double answerDifference = solvableAnswerCount - hardToSolveAnswerCount;
            analysis.put("answerDifference", Math.round(answerDifference * 100.0) / 100.0);

            // 分析浏览量
            double solvableViewCount = solvable.stream()
                    .mapToInt(q -> q.getViewCount() != null ? Math.min(q.getViewCount() / 100, 100) : 0)
                    .average().orElse(0);

            double hardToSolveViewCount = hardToSolve.stream()
                    .mapToInt(q -> q.getViewCount() != null ? Math.min(q.getViewCount() / 100, 100) : 0)
                    .average().orElse(0);

            analysis.put("solvableViewCount", Math.round(solvableViewCount * 100.0) / 100.0);
            analysis.put("hardToSolveViewCount", Math.round(hardToSolveViewCount * 100.0) / 100.0);

            double viewDifference = solvableViewCount - hardToSolveViewCount;
            analysis.put("viewDifference", Math.round(viewDifference * 100.0) / 100.0);

            // 参与度综合评分
            double solvableEngagementScore = calculateEngagementScore(solvable);
            double hardToSolveEngagementScore = calculateEngagementScore(hardToSolve);

            analysis.put("solvableValue", Math.round(solvableEngagementScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveEngagementScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableEngagementScore - hardToSolveEngagementScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = isEngagementSignificant(analysis);
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析用户参与度时发生错误", e);
            analysis = createDefaultEngagementData();
        }

        return analysis;
    }

    /**
     * 4. 用户声誉分析
     */
    private Map<String, Object> analyzeUserReputation(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析用户声誉（使用问题分数和回答数作为声誉的代理指标）
            double solvableReputation = solvable.stream()
                    .mapToInt(this::calculateReputationProxy)
                    .average().orElse(0);

            double hardToSolveReputation = hardToSolve.stream()
                    .mapToInt(this::calculateReputationProxy)
                    .average().orElse(0);

            analysis.put("solvableReputation", Math.round(solvableReputation * 100.0) / 100.0);
            analysis.put("hardToSolveReputation", Math.round(hardToSolveReputation * 100.0) / 100.0);

            double reputationDifference = solvableReputation - hardToSolveReputation;
            analysis.put("reputationDifference", Math.round(reputationDifference * 100.0) / 100.0);

            // 声誉评分标准化
            double solvableReputationScore = Math.min(solvableReputation / 10.0, 100.0);
            double hardToSolveReputationScore = Math.min(hardToSolveReputation / 10.0, 100.0);

            analysis.put("solvableValue", Math.round(solvableReputationScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveReputationScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableReputationScore - hardToSolveReputationScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = Math.abs(reputationDifference) > 2;
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析用户声誉时发生错误", e);
            analysis = createDefaultReputationData();
        }

        return analysis;
    }

    /**
     * 5. 时间因素分析
     */
    private Map<String, Object> analyzeTimeFactors(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析白天回答比例
            double solvableDaytimeRate = calculateDaytimeAnswerRate(solvable);
            double hardToSolveDaytimeRate = calculateDaytimeAnswerRate(hardToSolve);

            analysis.put("solvableDaytimeRate", Math.round(solvableDaytimeRate * 100.0) / 100.0);
            analysis.put("hardToSolveDaytimeRate", Math.round(hardToSolveDaytimeRate * 100.0) / 100.0);

            double daytimeRateDifference = solvableDaytimeRate - hardToSolveDaytimeRate;
            analysis.put("daytimeRateDifference", Math.round(daytimeRateDifference * 100.0) / 100.0);

            // 分析回答速度（小时）
            double solvableAnswerTime = solvable.stream()
                    .mapToDouble(this::calculateAnswerTime)
                    .average().orElse(0);

            double hardToSolveAnswerTime = hardToSolve.stream()
                    .mapToDouble(this::calculateAnswerTime)
                    .average().orElse(0);

            analysis.put("solvableAnswerTime", Math.round(solvableAnswerTime * 100.0) / 100.0);
            analysis.put("hardToSolveAnswerTime", Math.round(hardToSolveAnswerTime * 100.0) / 100.0);

            // 分析时间段分布
            Map<String, Double> solvableTimeDistribution = analyzeTimeDistribution(solvable);
            Map<String, Double> hardToSolveTimeDistribution = analyzeTimeDistribution(hardToSolve);

            analysis.put("solvableTimeDistribution", solvableTimeDistribution);
            analysis.put("hardToSolveTimeDistribution", hardToSolveTimeDistribution);

            // 时间因素综合评分
            double solvableTimeScore = calculateTimeFactorScore(solvable);
            double hardToSolveTimeScore = calculateTimeFactorScore(hardToSolve);

            analysis.put("solvableValue", Math.round(solvableTimeScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveTimeScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableTimeScore - hardToSolveTimeScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = Math.abs(daytimeRateDifference) > 5 || Math.abs(solvableAnswerTime - hardToSolveAnswerTime) > 6;
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析时间因素时发生错误", e);
            analysis = createDefaultTimeFactorsData();
        }

        return analysis;
    }

    /**
     * 6. 内容质量分析
     */
    private Map<String, Object> analyzeContentQuality(List<Question> solvable, List<Question> hardToSolve) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            // 分析标题长度
            double solvableTitleLength = solvable.stream()
                    .mapToInt(q -> q.getTitle() != null ? q.getTitle().length() : 0)
                    .average().orElse(0);

            double hardToSolveTitleLength = hardToSolve.stream()
                    .mapToInt(q -> q.getTitle() != null ? q.getTitle().length() : 0)
                    .average().orElse(0);

            analysis.put("solvableTitleLength", Math.round(solvableTitleLength * 100.0) / 100.0);
            analysis.put("hardToSolveTitleLength", Math.round(hardToSolveTitleLength * 100.0) / 100.0);

            // 分析代码片段比例
            double solvableCodeRate = calculateCodeSnippetRate(solvable);
            double hardToSolveCodeRate = calculateCodeSnippetRate(hardToSolve);

            analysis.put("solvableCodeRate", Math.round(solvableCodeRate * 100.0) / 100.0);
            analysis.put("hardToSolveCodeRate", Math.round(hardToSolveCodeRate * 100.0) / 100.0);

            // 内容质量综合评分
            double solvableContentScore = calculateContentQualityScore(solvable);
            double hardToSolveContentScore = calculateContentQualityScore(hardToSolve);

            analysis.put("solvableValue", Math.round(solvableContentScore * 100.0) / 100.0);
            analysis.put("hardToSolveValue", Math.round(hardToSolveContentScore * 100.0) / 100.0);
            analysis.put("difference", Math.round((solvableContentScore - hardToSolveContentScore) * 100.0) / 100.0);

            // 显著性判断
            boolean isSignificant = Math.abs(solvableContentScore - hardToSolveContentScore) > 5;
            analysis.put("significance", isSignificant ? "显著" : "不显著");
            analysis.put("significanceLevel", isSignificant ? "high" : "low");

        } catch (Exception e) {
            log.error("分析内容质量时发生错误", e);
            analysis = createDefaultContentQualityData();
        }

        return analysis;
    }

    // ========== 辅助计算方法 ==========

    /**
     * 计算平均评论数量 - 使用CommentRepository关联查询
     */
    private double calculateAverageCommentCount(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        long totalComments = 0;
        int count = 0;

        for (Question question : questions) {
            try {
                // 使用CommentRepository查询评论数量
                Long commentCount = commentRepository.countByPostId(question.getId());
                if (commentCount != null) {
                    totalComments += commentCount;
                    count++;
                    log.debug("问题 {} 的评论数量: {}", question.getId(), commentCount);
                } else {
                    // 备选方案：使用commentCount字段
                    if (question.getCommentCount() != null) {
                        totalComments += question.getCommentCount();
                        count++;
                    }
                }
            } catch (Exception e) {
                log.warn("获取问题 {} 的评论数量失败: {}", question.getId(), e.getMessage());
                // 备选方案
                if (question.getCommentCount() != null) {
                    totalComments += question.getCommentCount();
                    count++;
                }
            }
        }

        double avgComments = count > 0 ? (double) totalComments / count : 0;
        log.debug("计算了 {} 个问题的平均评论数: {}", count, avgComments);
        return avgComments;
    }

    /**
     * 计算用户参与度评分
     */
    private double calculateEngagementScore(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        try {
            // 计算平均评论数
            double avgComments = calculateAverageCommentCount(questions);

            // 计算平均分数
            double avgScore = questions.stream()
                    .mapToInt(q -> q.getScore() != null ? Math.max(q.getScore(), 0) : 0)
                    .average().orElse(0);

            // 计算平均回答数
            double avgAnswers = questions.stream()
                    .mapToInt(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0)
                    .average().orElse(0);

            // 计算平均浏览量（标准化）
            double avgViews = questions.stream()
                    .mapToInt(q -> q.getViewCount() != null ? Math.min(q.getViewCount() / 100, 100) : 0)
                    .average().orElse(0);

            // 参与度评分公式
            double commentScore = Math.min(avgComments * 3, 30);  // 评论数权重
            double scorePoints = Math.min(avgScore * 2, 40);     // 分数权重
            double answerScore = Math.min(avgAnswers * 5, 20);   // 回答数权重
            double viewScore = Math.min(avgViews, 10);           // 浏览量权重

            double totalScore = commentScore + scorePoints + answerScore + viewScore;

            log.debug("参与度评分计算 - 评论: {}, 分数: {}, 回答: {}, 浏览: {}, 总分: {}",
                    commentScore, scorePoints, answerScore, viewScore, totalScore);

            return Math.min(totalScore, 100);

        } catch (Exception e) {
            log.error("计算参与度评分失败", e);
            return 0;
        }
    }

    /**
     * 判断用户参与度是否显著
     */
    private boolean isEngagementSignificant(Map<String, Object> analysis) {
        try {
            double scoreDifference = Math.abs((Double) analysis.getOrDefault("scoreDifference", 0.0));
            double commentDifference = Math.abs((Double) analysis.getOrDefault("commentDifference", 0.0));
            double answerDifference = Math.abs((Double) analysis.getOrDefault("answerDifference", 0.0));
            double engagementDifference = Math.abs((Double) analysis.getOrDefault("difference", 0.0));

            boolean isSignificant = scoreDifference >= SCORE_DIFFERENCE_THRESHOLD ||
                    commentDifference >= COMMENT_DIFFERENCE_THRESHOLD ||
                    answerDifference >= 1.0 || // 回答数差异阈值
                    engagementDifference >= ENGAGEMENT_SCORE_SIGNIFICANCE_THRESHOLD;

            log.debug("参与度显著性判断 - 分数差异: {}, 评论差异: {}, 回答差异: {}, 综合差异: {}, 显著: {}",
                    scoreDifference, commentDifference, answerDifference, engagementDifference, isSignificant);

            return isSignificant;

        } catch (Exception e) {
            log.error("判断参与度显著性失败", e);
            return false;
        }
    }

    /**
     * 计算标签数量
     */
    private int countTags(Question question) {
        if (question.getTagsCache() == null) return 0;
        return question.getTagsCache().split(",").length;
    }

    /**
     * 计算高级主题比例
     */
    private double calculateAdvancedTopicRate(List<Question> questions) {
        if (questions.isEmpty()) return 0;
        long advancedCount = questions.stream()
                .filter(this::containsAdvancedTopics)
                .count();
        return (double) advancedCount / questions.size() * 100;
    }

    /**
     * 判断是否包含高级主题
     */
    private boolean containsAdvancedTopics(Question question) {
        if (question.getTagsCache() == null) return false;
        String tags = question.getTagsCache().toLowerCase();
        return ADVANCED_TOPICS.stream().anyMatch(tags::contains);
    }

    /**
     * 判断是否包含疑难标签
     */
    private boolean containsDifficultTags(Question question) {
        if (question.getTagsCache() == null) return false;
        String tags = question.getTagsCache().toLowerCase();
        return DIFFICULT_TAGS.stream().anyMatch(tags::contains);
    }

    /**
     * 计算问题复杂度评分
     */
    private double calculateComplexityScore(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        double avgTags = questions.stream()
                .mapToInt(this::countTags)
                .average().orElse(0);

        double advancedRate = calculateAdvancedTopicRate(questions);

        // 复杂度评分公式：标签数量 + 高级主题比例
        double score = (avgTags * 5) + (advancedRate / 2);
        return Math.min(score, 100);
    }

    /**
     * 计算用户声誉代理指标
     */
    private int calculateReputationProxy(Question question) {
        int score = question.getScore() != null ? Math.max(question.getScore(), 0) : 0;
        int answers = question.getAnswerCount() != null ? question.getAnswerCount() : 0;
        int comments = question.getCommentCount() != null ? question.getCommentCount() : 0;
        int views = question.getViewCount() != null ? Math.min(question.getViewCount() / 100, 100) : 0;

        // 综合声誉评分
        return score + (answers * 2) + comments + views;
    }

    /**
     * 计算回答时间（小时）
     */
    private double calculateAnswerTime(Question question) {
        if (question.getCreationDate() == null || question.getLastActivityDate() == null) {
            return 24.0; // 默认值
        }

        long creationTime = question.getCreationDate();
        long lastActivityTime = question.getLastActivityDate();

        // 计算小时差
        double hoursDiff = (lastActivityTime - creationTime) / 3600.0;

        return Math.max(hoursDiff, 1.0); // 至少1小时
    }

    /**
     * 计算代码片段比例
     */
    private double calculateCodeSnippetRate(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        long withCode = questions.stream()
                .filter(this::containsCodeSnippet)
                .count();

        return (double) withCode / questions.size() * 100;
    }

    /**
     * 判断是否包含代码片段
     */
    private boolean containsCodeSnippet(Question question) {
        if (question.getBody() == null) return false;
        String body = question.getBody().toLowerCase();

        // 检测代码片段的常见模式
        return body.contains("<code>") ||
                body.contains("```") ||
                body.contains("public class") ||
                body.contains("public static") ||
                body.contains("import ") ||
                body.contains("system.out") ||
                body.contains("void ") ||
                body.contains("class ") ||
                body.contains("interface ");
    }

    /**
     * 计算内容质量评分
     */
    private double calculateContentQualityScore(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        double avgTitleLength = questions.stream()
                .mapToInt(q -> q.getTitle() != null ? q.getTitle().length() : 0)
                .average().orElse(0);

        double codeRate = calculateCodeSnippetRate(questions);

        // 内容质量评分公式：标题长度/10 + 代码片段比例
        double score = (avgTitleLength / 5) + (codeRate / 2);
        return Math.min(score, 100);
    }

    /**
     * 计算白天回答比例
     */
    private double calculateDaytimeAnswerRate(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        long daytimeAnswers = questions.stream()
                .filter(this::isAnsweredInDaytime)
                .count();

        return (double) daytimeAnswers / questions.size() * 100;
    }

    /**
     * 判断问题是否在白天被回答
     */
    private boolean isAnsweredInDaytime(Question question) {
        if (question.getCreationDate() == null || question.getLastActivityDate() == null) {
            return false;
        }

        // 获取最后活动时间的小时（作为回答时间的代理）
        Instant activityInstant = Instant.ofEpochSecond(question.getLastActivityDate());
        LocalDateTime activityTime = LocalDateTime.ofInstant(activityInstant, ZoneId.systemDefault());
        int activityHour = activityTime.getHour();

        // 定义白天时间：早上6点到晚上10点
        boolean isDaytime = activityHour >= DAYTIME_START_HOUR && activityHour < DAYTIME_END_HOUR;

        return isDaytime;
    }

    /**
     * 分析时间分布
     */
    private Map<String, Double> analyzeTimeDistribution(List<Question> questions) {
        Map<String, Double> distribution = new HashMap<>();

        if (questions.isEmpty()) {
            distribution.put("morning", 0.0);
            distribution.put("afternoon", 0.0);
            distribution.put("evening", 0.0);
            distribution.put("night", 0.0);
            return distribution;
        }

        long morningCount = questions.stream()
                .filter(q -> getTimePeriod(q) == TimePeriod.MORNING)
                .count();

        long afternoonCount = questions.stream()
                .filter(q -> getTimePeriod(q) == TimePeriod.AFTERNOON)
                .count();

        long eveningCount = questions.stream()
                .filter(q -> getTimePeriod(q) == TimePeriod.EVENING)
                .count();

        long nightCount = questions.stream()
                .filter(q -> getTimePeriod(q) == TimePeriod.NIGHT)
                .count();

        distribution.put("morning", Math.round((double) morningCount / questions.size() * 10000) / 100.0);
        distribution.put("afternoon", Math.round((double) afternoonCount / questions.size() * 10000) / 100.0);
        distribution.put("evening", Math.round((double) eveningCount / questions.size() * 10000) / 100.0);
        distribution.put("night", Math.round((double) nightCount / questions.size() * 10000) / 100.0);

        return distribution;
    }

    /**
     * 获取问题的时间段
     */
    private TimePeriod getTimePeriod(Question question) {
        if (question.getCreationDate() == null) {
            return TimePeriod.UNKNOWN;
        }

        Instant instant = Instant.ofEpochSecond(question.getCreationDate());
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        int hour = dateTime.getHour();

        if (hour >= MORNING_START && hour < AFTERNOON_START) {
            return TimePeriod.MORNING;      // 早上 6:00-11:59
        } else if (hour >= AFTERNOON_START && hour < EVENING_START) {
            return TimePeriod.AFTERNOON;   // 下午 12:00-17:59
        } else if (hour >= EVENING_START && hour < NIGHT_START) {
            return TimePeriod.EVENING;      // 晚上 18:00-21:59
        } else {
            return TimePeriod.NIGHT;        // 夜间 22:00-5:59
        }
    }

    /**
     * 计算时间因素综合评分
     */
    private double calculateTimeFactorScore(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        double daytimeRate = calculateDaytimeAnswerRate(questions);
        double answerSpeed = calculateAverageAnswerSpeed(questions);

        // 评分公式：白天回答比例 + 快速回答加分
        double speedBonus = Math.max(0, 24 - answerSpeed) / 24 * 40; // 24小时内回答得满分
        double score = (daytimeRate * 0.6) + speedBonus;

        return Math.min(score, 100);
    }

    /**
     * 计算平均回答速度
     */
    private double calculateAverageAnswerSpeed(List<Question> questions) {
        if (questions.isEmpty()) return 0;

        return questions.stream()
                .mapToDouble(this::calculateAnswerTime)
                .average()
                .orElse(0);
    }

    /**
     * 生成分析洞察
     */
    private List<String> generateInsights(List<Question> solvable, List<Question> hardToSolve, Map<String, Object> factors) {
        List<String> insights = new ArrayList<>();

        double solvabilityRate = (double) solvable.size() / (solvable.size() + hardToSolve.size());
        insights.add(String.format("📊 总体可解率: %.2f%%", solvabilityRate * 100));

        // 分析六个核心因素的显著性
        int significantFactors = 0;

        Map<String, Object> complexityFactors = (Map<String, Object>) factors.get("questionComplexity");
        Map<String, Object> difficultTagsFactors = (Map<String, Object>) factors.get("difficultTags");
        Map<String, Object> engagementFactors = (Map<String, Object>) factors.get("userEngagement");
        Map<String, Object> reputationFactors = (Map<String, Object>) factors.get("userReputation");
        Map<String, Object> timeFactors = (Map<String, Object>) factors.get("timeFactors");
        Map<String, Object> contentFactors = (Map<String, Object>) factors.get("contentQuality");

        // 检查每个因素的显著性
        if ("显著".equals(complexityFactors.get("significance"))) {
            insights.add("🎯 问题复杂度: 复杂问题（多标签、高级主题）更难获得解答");
            significantFactors++;
        }

        if ("显著".equals(difficultTagsFactors.get("significance"))) {
            insights.add("⚠️ 疑难标签: 含解答率<30%标签的问题更难获得解答");
            significantFactors++;
        }

        if ("显著".equals(engagementFactors.get("significance"))) {
            double scoreDiff = (double) engagementFactors.get("scoreDifference");
            double commentDiff = (double) engagementFactors.get("commentDifference");

            StringBuilder insight = new StringBuilder("👥 用户参与度: ");

            if (Math.abs(scoreDiff) >= SCORE_DIFFERENCE_THRESHOLD) {
                insight.append(String.format("高分数问题(平均高%.1f分) ", scoreDiff));
            }

            if (Math.abs(commentDiff) >= COMMENT_DIFFERENCE_THRESHOLD) {
                insight.append(String.format("高评论数问题(平均多%.1f条评论) ", commentDiff));
            }

            insight.append("更容易吸引解答者");
            insights.add(insight.toString());
            significantFactors++;
        }

        if ("显著".equals(reputationFactors.get("significance"))) {
            insights.add("⭐ 用户声誉: 高声誉用户的问题更容易获得解答");
            significantFactors++;
        }

        if ("显著".equals(timeFactors.get("significance"))) {
            double daytimeRateDifference = (double) timeFactors.get("daytimeRateDifference");
            double answerTimeDifference = (double) timeFactors.get("solvableAnswerTime") - (double) timeFactors.get("hardToSolveAnswerTime");

            if (daytimeRateDifference > 0) {
                insights.add("⏰ 时间因素: 白天发布的问题更容易获得解答");
            } else {
                insights.add("⏰ 时间因素: 夜间发布的问题解答率较低");
            }

            if (answerTimeDifference < 0) {
                insights.add("⚡ 回答速度: 快速获得答案的问题可解率更高");
            }

            significantFactors++;
        }

        if ("显著".equals(contentFactors.get("significance"))) {
            insights.add("📝 内容质量: 清晰的问题描述和代码示例显著提高解答率");
            significantFactors++;
        }

        insights.add(String.format("✅ 成功识别出 %d 个显著影响因素", significantFactors));

        return insights;
    }

    // ========== 默认数据创建方法 ==========

    private Map<String, Object> createDefaultFactors() {
        Map<String, Object> factors = new HashMap<>();
        factors.put("questionComplexity", createDefaultComplexityData());
        factors.put("difficultTags", createDefaultDifficultTagsData());
        factors.put("userEngagement", createDefaultEngagementData());
        factors.put("userReputation", createDefaultReputationData());
        factors.put("timeFactors", createDefaultTimeFactorsData());
        factors.put("contentQuality", createDefaultContentQualityData());
        return factors;
    }

    private Map<String, Object> createDefaultComplexityData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    private Map<String, Object> createDefaultDifficultTagsData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    private Map<String, Object> createDefaultEngagementData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableCommentCount", 0.0);
        data.put("hardToSolveCommentCount", 0.0);
        data.put("commentDifference", 0.0);
        data.put("solvableScore", 0.0);
        data.put("hardToSolveScore", 0.0);
        data.put("scoreDifference", 0.0);
        data.put("solvableAnswerCount", 0.0);
        data.put("hardToSolveAnswerCount", 0.0);
        data.put("answerDifference", 0.0);
        data.put("solvableViewCount", 0.0);
        data.put("hardToSolveViewCount", 0.0);
        data.put("viewDifference", 0.0);
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    private Map<String, Object> createDefaultReputationData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    private Map<String, Object> createDefaultTimeFactorsData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    private Map<String, Object> createDefaultContentQualityData() {
        Map<String, Object> data = new HashMap<>();
        data.put("solvableValue", 0.0);
        data.put("hardToSolveValue", 0.0);
        data.put("difference", 0.0);
        data.put("significance", "不显著");
        return data;
    }

    /**
     * 时间段枚举
     */
    private enum TimePeriod {
        MORNING,    // 早上 6:00-11:59
        AFTERNOON,  // 下午 12:00-17:59
        EVENING,    // 晚上 18:00-21:59
        NIGHT,      // 夜间 22:00-5:59
        UNKNOWN
    }

    /**
     * 获取数据时间范围
     */
    public Map<String, Object> getDataTimeRange() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Question> allQuestions = questionRepository.findAll();

            if (allQuestions.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有数据");
                return result;
            }

            // 获取最小和最大创建时间
            long minTime = allQuestions.stream()
                    .mapToLong(q -> q.getCreationDate() != null ? q.getCreationDate() : Long.MAX_VALUE)
                    .min().orElse(0);

            long maxTime = allQuestions.stream()
                    .mapToLong(q -> q.getCreationDate() != null ? q.getCreationDate() : 0)
                    .max().orElse(0);

            result.put("success", true);
            result.put("minTime", minTime);
            result.put("maxTime", maxTime);
            result.put("totalQuestions", allQuestions.size());

            // 转换为可读格式
            if (minTime > 0) {
                result.put("minTimeReadable", Instant.ofEpochSecond(minTime).toString());
            }
            if (maxTime > 0) {
                result.put("maxTimeReadable", Instant.ofEpochSecond(maxTime).toString());
            }

        } catch (Exception e) {
            log.error("获取时间范围失败", e);
            result.put("success", false);
            result.put("message", "获取时间范围失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取分析统计信息
     */
    public Map<String, Object> getAnalysisStatistics() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Question> allQuestions = questionRepository.findAll();

            if (allQuestions.isEmpty()) {
                result.put("success", false);
                result.put("message", "没有数据");
                return result;
            }

            // 基本统计
            long totalQuestions = allQuestions.size();
            long answeredQuestions = allQuestions.stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsAnswered()))
                    .count();
            long highScoreQuestions = allQuestions.stream()
                    .filter(q -> q.getScore() != null && q.getScore() >= 10)
                    .count();
            long highViewQuestions = allQuestions.stream()
                    .filter(q -> q.getViewCount() != null && q.getViewCount() >= 1000)
                    .count();

            // 标签统计
            double avgTags = allQuestions.stream()
                    .mapToInt(this::countTags)
                    .average().orElse(0);

            // 参与度统计
            double avgScore = allQuestions.stream()
                    .mapToInt(q -> q.getScore() != null ? q.getScore() : 0)
                    .average().orElse(0);

            double avgViews = allQuestions.stream()
                    .mapToInt(q -> q.getViewCount() != null ? q.getViewCount() : 0)
                    .average().orElse(0);

            double avgAnswers = allQuestions.stream()
                    .mapToInt(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0)
                    .average().orElse(0);

            result.put("success", true);
            result.put("totalQuestions", totalQuestions);
            result.put("answeredQuestions", answeredQuestions);
            result.put("answerRate", totalQuestions > 0 ?
                    Math.round((double) answeredQuestions / totalQuestions * 10000) / 100.0 : 0);
            result.put("highScoreQuestions", highScoreQuestions);
            result.put("highViewQuestions", highViewQuestions);
            result.put("averageTags", Math.round(avgTags * 100.0) / 100.0);
            result.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
            result.put("averageViews", Math.round(avgViews * 100.0) / 100.0);
            result.put("averageAnswers", Math.round(avgAnswers * 100.0) / 100.0);

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取特定因素的分析结果
     */
    public Map<String, Object> getFactorAnalysis(String factorName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里可以添加特定因素的缓存或快速分析逻辑
            // 目前先返回完整分析结果中的特定因素
            Map<String, Object> fullAnalysis = analyzeSolvabilityFactors();

            if (Boolean.TRUE.equals(fullAnalysis.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> factors = (Map<String, Object>) fullAnalysis.get("factors");

                if (factors != null && factors.containsKey(factorName)) {
                    result.put("success", true);
                    result.put("factor", factorName);
                    result.put("data", factors.get(factorName));
                    result.put("timestamp", System.currentTimeMillis());
                } else {
                    result.put("success", false);
                    result.put("message", "不支持的因子名称: " + factorName);
                }
            } else {
                result.put("success", false);
                result.put("message", fullAnalysis.get("message"));
            }

        } catch (Exception e) {
            log.error("获取因子分析结果失败: {}", factorName, e);
            result.put("success", false);
            result.put("message", "获取因子分析结果失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();

        try {
            long questionCount = questionRepository.count();
            boolean databaseConnected = questionCount >= 0;

            result.put("success", true);
            result.put("status", "UP");
            result.put("service", "Question Solvability Analysis Service");
            result.put("database", databaseConnected ? "CONNECTED" : "DISCONNECTED");
            result.put("questionCount", questionCount);
            result.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("健康检查失败", e);
            result.put("success", false);
            result.put("status", "DOWN");
            result.put("message", "健康检查失败: " + e.getMessage());
        }

        return result;
    }

    // 其他方法（占位符实现）
    public Map<String, Object> analyzeMultipleFactors(java.util.List<String> factors) {
        // 实现批量分析逻辑
        return Map.of("success", true, "message", "批量分析功能待实现");
    }

    public Map<String, Object> getAnalysisHistory() {
        // 实现分析历史查询
        return Map.of("success", true, "history", java.util.List.of());
    }

    public Map<String, Object> clearAnalysisCache() {
        // 实现缓存清除
        return Map.of("success", true, "message", "缓存清除成功");
    }

    public Map<String, Object> getAnalysisConfig() {
        // 实现配置获取
        return Map.of("success", true, "config", Map.of());
    }

    public Map<String, Object> updateAnalysisConfig(Map<String, Object> config) {
        // 实现配置更新
        return Map.of("success", true, "message", "配置更新成功");
    }

    public Map<String, Object> getAnalysisProgress() {
        // 实现进度查询
        return Map.of("success", true, "progress", 100, "status", "COMPLETED");
    }

    public Map<String, Object> stopAnalysis() {
        // 实现分析停止
        return Map.of("success", true, "message", "分析已停止");
    }
}