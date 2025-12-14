package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.dto.CoOccurrenceDTO;
import com.xiao.cs209a_project.dto.PitfallDTO;
import com.xiao.cs209a_project.dto.SolvabilityDTO;
import com.xiao.cs209a_project.dto.TopicTrendDTO;
import com.xiao.cs209a_project.entity.Answer;
import com.xiao.cs209a_project.entity.Comment;
import com.xiao.cs209a_project.entity.Question;
import com.xiao.cs209a_project.repository.AnswerRepository;
import com.xiao.cs209a_project.repository.CommentRepository;
import com.xiao.cs209a_project.repository.QuestionRepository;
import com.xiao.cs209a_project.repository.QuestionTagRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service

public class AnalysisServiceImpl implements AnalysisService{

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;

    public AnalysisServiceImpl(QuestionRepository questionRepository, AnswerRepository answerRepository, CommentRepository commentRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.commentRepository = commentRepository;

    }

    //任务1实现
    @Override
    public List<TopicTrendDTO> analyzeTopicTrends(){
        return new ArrayList<>();
    };
    //任务2实现
    @Override
    public List<CoOccurrenceDTO> analyzeCoOccurrence(int topN){

        List<String> allTagsRaw = questionRepository.findAllTags();

        Map<String, Integer> pairCounts = new HashMap<>();


        for (String rawTags : allTagsRaw) {

            if (rawTags == null || rawTags.trim().isEmpty()) {
                continue;
            }

            String[] splitArray = rawTags.split(",");

            List<String> tags = new ArrayList<>();
            for (String tagStr : splitArray) {
                String cleanTag = tagStr.trim();

                // 过滤条件：1. 非空 2. 不是 "java"
                if (!cleanTag.isEmpty() && !cleanTag.equalsIgnoreCase("java")) {
                    tags.add(cleanTag);
                }
            }
            // 边界检查：如果只有一个标签或没有标签，无法构成"对"，直接跳过
            if (tags.size() < 2) {
                continue;
            }

            Collections.sort(tags);

            // 双重循环生成组合
            // 所有不重复的组合
            for (int i = 0; i < tags.size(); i++) {
                for (int j = i + 1; j < tags.size(); j++) {
                    String key = tags.get(i) + "::" + tags.get(j);
                    //将组合出的值作为key存入map
                    pairCounts.merge(key, 1, Integer::sum);
                }
            }
        }

        return pairCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> {
                    // 解析 Key: "java::spring" -> ["java", "spring"]
                    String[] parts = entry.getKey().split("::");
                    return new CoOccurrenceDTO(parts[0], parts[1], entry.getValue());
                })
                .collect(Collectors.toList());
    }
    //任务3实现
    /*
     * exportUniqueTags() 方法使用AI生成
     * 模型：gemini3
     * */
    @Override
    public String exportUniqueTags() {
        // 1. 从数据库获取所有原始标签字符串
        // (利用之前写的优化查询，只查 tags_cache 字段)
        List<String> allRawTags = questionRepository.findAllTags();

        // 2. 使用 TreeSet 自动去重并排序
        Set<String> uniqueTags = new TreeSet<>();

        for (String raw : allRawTags) {
            if (raw == null || raw.trim().isEmpty()) continue;

            // 按逗号分割
            String[] tags = raw.split(",");
            for (String t : tags) {
                if (!t.trim().isEmpty()) {
                    uniqueTags.add(t.trim().toLowerCase());
                }
            }
        }

        // 3. 将结果写入文件
        String fileName = "unique_tags.txt";
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(fileName))) {
            writer.write("Total Unique Tags: " + uniqueTags.size());
            writer.newLine();
            writer.write("============================");
            writer.newLine();

            for (String tag : uniqueTags) {
                writer.write(tag);
                writer.newLine();
            }

            return "成功导出 " + uniqueTags.size() + " 个不重复标签到文件: " + fileName;
        } catch (java.io.IOException e) {

            return "导出失败: " + e.getMessage();
        }
    }

/*
* Deadlock/Hanging(死锁):deadlock|dead-lock|hold and wait|stuck|hangs|freezes|blocking indefinit
* Race Condition/Inconsistency(竞态条件):race condition|check-then-act|inconsistent|unpredictable|wrong value sometimes
* ConcurrentModification(并发修改异常):ConcurrentModificationException|fail-fast|concurrent modification
* Synchronization/Thread Safety(线程安全/同步问题):thread safe|thread-safe|not synchronized|IllegalMonitorStateException|monitor
* Visibility/Atomicity(可见性与原子性):volatile|visibility|atomic|memory barrier|instruction reordering
* Thread Lifecycle/Interrupt(线程生命周期管理):InterruptedException|interrupt|thread leak|daemon
* NullPointerException (Concurrency)(空指针异常):NullPointerException|NPE
* */
    private static final Map<String, Pattern> PITFALL_PATTERNS = new HashMap<>();

    static {
        // 1. 死锁 (扩充：卡死、冻结、挂起)
        PITFALL_PATTERNS.put("Deadlock/Hanging", Pattern.compile("(?i)(deadlock|dead-lock|hold and wait|stuck|hangs|freezes|blocking indefinit)"));

        // 2. 竞态条件 (扩充：不一致、有时候错)
        PITFALL_PATTERNS.put("Race Condition/Inconsistency", Pattern.compile("(?i)(race condition|check-then-act|inconsistent|unpredictable|wrong value sometimes)"));

        // 3. 并发修改异常 (集合类最常见错误)
        PITFALL_PATTERNS.put("ConcurrentModification", Pattern.compile("(?i)(ConcurrentModificationException|fail-fast|concurrent modification)"));

        // 4. 线程安全/同步问题 (synchronized, lock)
        PITFALL_PATTERNS.put("Synchronization/Thread Safety", Pattern.compile("(?i)(thread safe|thread-safe|not synchronized|IllegalMonitorStateException|monitor)"));

        // 5. 可见性与原子性 (Volatile/Atomic)
        PITFALL_PATTERNS.put("Visibility/Atomicity", Pattern.compile("(?i)(volatile|visibility|atomic|memory barrier|instruction reordering)"));

        // 6. 线程生命周期管理 (启动、停止、中断)
        PITFALL_PATTERNS.put("Thread Lifecycle/Interrupt", Pattern.compile("(?i)(InterruptedException|interrupt|thread leak|daemon)"));

        // 7. 空指针异常 (并发环境下初始化顺序导致的NPE)
        PITFALL_PATTERNS.put("NullPointerException (Concurrency)", Pattern.compile("(?i)(NullPointerException|NPE)"));
    }
    @Override
    public List<PitfallDTO> analyzeMultithreadingPitfalls(){
// 1. 获取所有"多线程相关"的问题 (Question)
        List<Question> questions = questionRepository.findMultithreadingRelatedQuestions();


        Map<String, Integer> pitfallCounts = new HashMap<>();
        // 初始化计数器
        PITFALL_PATTERNS.keySet().forEach(k -> pitfallCounts.put(k, 0));

        // 2. 遍历每个问题，聚合所有相关文本
        for (Question q : questions) {
            StringBuilder fullTextBuilder = new StringBuilder();

            // A. 添加问题标题和正文
            fullTextBuilder.append(q.getTitle()).append(" ").append(q.getBody()).append(" ");

            // B. 获取并添加该问题下的直接评论
            List<Comment> qComments = commentRepository.findByPostIdAndPostType(q.getQuestionId(), "question");
            for (Comment c : qComments) {
                fullTextBuilder.append(c.getBody()).append(" ");
            }

            // C. 获取该问题下的所有回答 (Answers)
            List<Answer> answers = answerRepository.findByQuestionId(q.getQuestionId());
            if (!answers.isEmpty()) {
                List<Long> answerIds = new ArrayList<>();
                for (Answer a : answers) {
                    // 添加回答正文
                    fullTextBuilder.append(a.getBody()).append(" ");
                    answerIds.add(a.getAnswerId());
                }

                // D. 批量获取这些回答下的所有评论

                if (!answerIds.isEmpty()) {
                    List<Comment> aComments = commentRepository.findByPostIdInAndPostType(answerIds, "answer");
                    for (Comment c : aComments) {
                        fullTextBuilder.append(c.getBody()).append(" ");
                    }
                }
            }

            // 3. 对聚合后的全量文本进行正则匹配
            String fullText = fullTextBuilder.toString();

            // 针对当前这个 Thread (帖子)，检查命中了哪些陷阱
            for (Map.Entry<String, Pattern> entry : PITFALL_PATTERNS.entrySet()) {
                String pitfallName = entry.getKey();
                Pattern pattern = entry.getValue();

                // 只要全量文本中出现了一次该关键词，就算该问题涉及这个陷阱
                if (pattern.matcher(fullText).find()) {
                    pitfallCounts.merge(pitfallName, 1, Integer::sum);
                }
            }
        }

        // 4. 转换结果并排序
        int totalAnalyzed = questions.size();
        return pitfallCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(entry -> {
                    // 计算占比
                    double percentage = totalAnalyzed > 0 ?
                            (double) entry.getValue() / totalAnalyzed * 100 : 0.0;
                    return new PitfallDTO(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());
    }
    //任务4实现
    @Override
    public List<SolvabilityDTO> analyzeSolvabilityFactors(){
        return new ArrayList<>();
    }
}
