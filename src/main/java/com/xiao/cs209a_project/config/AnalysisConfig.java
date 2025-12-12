package com.xiao.cs209a_project.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisConfig {

    // 可解性判断阈值配置
    public static final int MIN_ANSWERS_FOR_SOLVABLE = 1;
    public static final int MIN_SCORE_FOR_SOLVABLE = 3;
    public static final int MIN_VIEWS_FOR_SOLVABLE = 500;

    // 疑难标签阈值（解答率<30%）
    public static final double DIFFICULT_TAG_THRESHOLD = 10;

    // 用户参与度显著性阈值
    public static final double ENGAGEMENT_SCORE_SIGNIFICANCE_THRESHOLD = 8.0;
    public static final double SCORE_DIFFERENCE_THRESHOLD = 2.0;
    public static final double COMMENT_DIFFERENCE_THRESHOLD = 1.5;

    // 时间因素配置
    public static final int DAYTIME_START_HOUR = 6;
    public static final int DAYTIME_END_HOUR = 22;
    public static final int FAST_ANSWER_THRESHOLD_HOURS = 24;

    // 时间段定义
    public static final int MORNING_START = 6;
    public static final int AFTERNOON_START = 12;
    public static final int EVENING_START = 18;
    public static final int NIGHT_START = 22;

    // 显著性判断阈值
    public static final double SIGNIFICANCE_THRESHOLD = 5.0;
}