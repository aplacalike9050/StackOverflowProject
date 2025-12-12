package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.dto.*;
import com.xiao.cs209a_project.entity.*;
import com.xiao.cs209a_project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionImportService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final QuestionTagRepository questionTagRepository;
    private final StackExchangeApiService apiService;

    @Transactional
    public int importQuestions(int count) {
        log.info("开始从Stack Exchange导入问题数据，目标数量: {}", count);

        if (count <= 0) {
            throw new IllegalArgumentException("导入数量必须大于0");
        }

        int importedCount = 0;
        int page = 1;
        int pageSize = Math.min(count, 50);

        try {
            while (importedCount < count) {
                log.info("正在获取第 {} 页数据，需要 {} 条，已导入 {} 条", page, count, importedCount);

                // API调用延迟
                Thread.sleep(2000);

                StackExchangeResponse<QuestionItem> response = apiService.getJavaQuestions(page, pageSize);

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    log.info("第 {} 页没有获取到数据，停止导入", page);
                    break;
                }

                log.info("获取到 {} 条问题数据，开始处理", response.getItems().size());

                for (QuestionItem questionItem : response.getItems()) {
                    if (importedCount >= count) {
                        break;
                    }

                    if (questionItem.getQuestionId() == null) {
                        log.warn("跳过question_id为null的数据");
                        continue;
                    }

                    // 检查问题是否已存在
                    if (questionRepository.findByQuestionId(questionItem.getQuestionId()).isPresent()) {
                        log.debug("问题 {} 已存在，跳过", questionItem.getQuestionId());
                        continue;
                    }

                    // 1. 导入问题作者的用户数据
                    importUserIfNeeded(questionItem.getOwnerUserId());

                    // 2. 转换并保存问题
                    Question question = convertToQuestion(questionItem);
                    if (question == null) {
                        log.warn("问题 {} 转换失败，跳过", questionItem.getQuestionId());
                        continue;
                    }

                    Question savedQuestion = questionRepository.save(question);
                    importedCount++;

                    log.info("成功导入问题: ID={}, 标题={}",
                            savedQuestion.getQuestionId(),
                            savedQuestion.getTitle() != null ?
                                    (savedQuestion.getTitle().length() > 50 ? savedQuestion.getTitle().substring(0, 50) : savedQuestion.getTitle()) : "无标题");

                    // 3. 导入问题的标签数据
                    if (questionItem.getTags() != null && !questionItem.getTags().isEmpty()) {
                        log.info("问题 {} 有 {} 个标签，开始导入标签: {}",
                                savedQuestion.getQuestionId(),
                                questionItem.getTags().size(),
                                questionItem.getTags());

                        try {
                            // 传入 Stack Overflow 的 questionId (业务ID)
                            processQuestionTags(savedQuestion.getQuestionId(), questionItem.getTags());
                            log.info("问题 {} 的标签导入完成", savedQuestion.getQuestionId());
                        } catch (Exception e) {
                            log.error("导入问题 {} 的标签失败，但问题已保存，继续处理",
                                    savedQuestion.getQuestionId(), e);
                        }
                    }

                    // 4. 导入问题的评论
                    log.info("开始导入问题 {} 的评论数据", savedQuestion.getQuestionId());
                    try {
                        importCommentsForQuestion(savedQuestion.getQuestionId());
                    } catch (Exception e) {
                        log.error("导入问题 {} 的评论失败", savedQuestion.getQuestionId(), e);
                    }

                    // 5. 导入答案
                    if (savedQuestion.getAnswerCount() != null && savedQuestion.getAnswerCount() > 0) {
                        log.info("问题 {} 有 {} 个答案，开始导入答案",
                                savedQuestion.getQuestionId(), savedQuestion.getAnswerCount());
                        importAnswersForQuestion(savedQuestion.getQuestionId());
                    }

                    // 问题间的延迟
                    Thread.sleep(1000);
                }

                if (!response.isHasMore()) {
                    log.info("API返回没有更多数据，停止导入");
                    break;
                }
                page++;
            }

            log.info("数据导入完成，总共导入 {} 个问题", importedCount);
            return importedCount;

        } catch (Exception e) {
            log.error("导入数据时发生错误", e);
            throw new RuntimeException("数据导入失败: " + e.getMessage(), e);
        }
    }

    private void importAnswersForQuestion(Long questionId) {
        int page = 1;
        int pageSize = 100;

        try {
            while (true) {
                Thread.sleep(2000);
                StackExchangeResponse<AnswerItem> response = apiService.getAnswersForQuestion(questionId, page, pageSize);

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    break;
                }

                List<Answer> answersToSave = new ArrayList<>();
                for (AnswerItem answerItem : response.getItems()) {
                    if (answerRepository.findByAnswerId(answerItem.getAnswerId()).isPresent()) {
                        continue;
                    }
                    importUserIfNeeded(answerItem.getOwnerUserId());

                    Answer answer = convertToAnswer(answerItem);
                    if (answer != null) {
                        answersToSave.add(answer);
                    }
                }

                if (!answersToSave.isEmpty()) {
                    List<Answer> savedAnswers = answerRepository.saveAll(answersToSave);

                    // 导入答案评论
                    for (Answer savedAnswer : savedAnswers) {
                        try {
                            importCommentsForAnswer(savedAnswer.getAnswerId());
                        } catch (Exception e) {
                            log.error("导入答案 {} 的评论失败", savedAnswer.getAnswerId(), e);
                        }
                    }
                }

                if (!response.isHasMore()) break;
                page++;
            }
        } catch (Exception e) {
            log.error("导入问题 {} 的答案数据时发生错误", questionId, e);
        }
    }

    private void importCommentsForQuestion(Long questionId) {
        importComments(questionId, "question");
    }

    private void importCommentsForAnswer(Long answerId) {
        importComments(answerId, "answer");
    }

    private void importComments(Long postId, String type) {
        int page = 1;
        int pageSize = 100;

        try {
            while (true) {
                Thread.sleep(1000);
                StackExchangeResponse<CommentItem> response;

                if ("question".equals(type)) {
                    response = apiService.getCommentsForQuestion(postId, page, pageSize);
                } else {
                    response = apiService.getCommentsForAnswer(postId, page, pageSize);
                }

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    break;
                }

                List<Comment> commentsToSave = new ArrayList<>();
                for (CommentItem item : response.getItems()) {
                    if (commentRepository.existsByCommentId(item.getCommentId())) {
                        continue;
                    }
                    importUserIfNeeded(item.getOwnerUserId());

                    Comment comment = convertToComment(item, type);
                    if (comment != null) {
                        commentsToSave.add(comment);
                    }
                }

                if (!commentsToSave.isEmpty()) {
                    commentRepository.saveAll(commentsToSave);
                }

                if (!response.isHasMore()) break;
                page++;
            }
        } catch (Exception e) {
            log.error("导入评论失败: postId={}, type={}", postId, type, e);
        }
    }

    private void importUserIfNeeded(Long userId) {
        if (userId == null) return;

        try {
            if (userRepository.findByUserId(userId).isPresent()) {
                return;
            }

            Thread.sleep(1000);
            UserItem item = apiService.getUserInfo(userId);
            if (item == null) return;

            User user = convertToUser(item);
            if (user != null) {
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("导入用户数据失败: userId={}", userId, e);
        }
    }

    @Transactional
    public void processQuestionTags(Long questionId, List<String> tagNames) {
        if (questionId == null || tagNames == null || tagNames.isEmpty()) {
            return;
        }

        try {
            // 1. 确保 Tag 表中有这些标签
            List<Tag> tags = ensureTagsExist(tagNames);

            // 2. 创建关联
            List<QuestionTag> questionTags = new ArrayList<>();
            for (Tag tag : tags) {
                // 使用 Tag 的 id (主键)
                // 使用 Question 的 questionId (业务ID) - 对应 QuestionTag 实体中的定义
                // 假设 QuestionTag 实体里的 tagId 对应 Tag 表的主键 id
                if (!questionTagRepository.existsByQuestionIdAndTagId(questionId, tag.getId())) {
                    QuestionTag qt = new QuestionTag();
                    qt.setQuestionId(questionId);
                    qt.setTagId(tag.getId()); // 注意：这里用 Tag 的主键 ID
                    questionTags.add(qt);
                }
            }
            if (!questionTags.isEmpty()) {
                questionTagRepository.saveAll(questionTags);
            }

            // 3. 更新引用计数
            for (Tag tag : tags) {
                tagRepository.incrementUsageCount(tag.getId());
            }

        } catch (Exception e) {
            log.error("处理标签失败: questionId={}", questionId, e);
        }
    }

    private List<Tag> ensureTagsExist(List<String> tagNames) {
        List<Tag> existingTags = tagRepository.findByTagNameIn(tagNames);
        Set<String> existingNames = existingTags.stream().map(Tag::getTagName).collect(Collectors.toSet());

        List<Tag> newTags = new ArrayList<>();
        for (String name : tagNames) {
            if (!existingNames.contains(name)) {
                Tag t = new Tag();
                t.setTagName(name);
                t.setUsageCount(0);
                newTags.add(t);
            }
        }

        if (!newTags.isEmpty()) {
            existingTags.addAll(tagRepository.saveAll(newTags));
        }
        return existingTags;
    }

    // --- 转换方法 (核心修改：时间类型直接赋值 Long) ---

    private Question convertToQuestion(QuestionItem item) {
        Question q = new Question();
        q.setQuestionId(item.getQuestionId());
        q.setTitle(item.getTitle());
        q.setBody(item.getBody());
        q.setOwnerUserId(item.getOwnerUserId());
        q.setScore(item.getScore() != null ? item.getScore() : 0);
        q.setViewCount(item.getViewCount() != null ? item.getViewCount() : 0);
        q.setAnswerCount(item.getAnswerCount() != null ? item.getAnswerCount() : 0);
        q.setCommentCount(item.getCommentCount() != null ? item.getCommentCount() : 0);
        q.setIsAnswered(item.getIsAnswered() != null ? item.getIsAnswered() : false);
        q.setAcceptedAnswerId(item.getAcceptedAnswerId());

        // 直接存 Long
        q.setCreationDate(item.getCreationDate());
        q.setLastActivityDate(item.getLastActivityDate());

        // 标签缓存字符串
        if (item.getTags() != null) {
            q.setTagsCache(String.join(",", item.getTags()));
        }

        return q;
    }

    private Answer convertToAnswer(AnswerItem item) {
        Answer a = new Answer();
        a.setAnswerId(item.getAnswerId());
        a.setQuestionId(item.getQuestionId());
        a.setOwnerUserId(item.getOwnerUserId());
        a.setBody(item.getBody());
        a.setScore(item.getScore() != null ? item.getScore() : 0);
        a.setIsAccepted(item.getIsAccepted() != null ? item.getIsAccepted() : false);

        // 直接存 Long
        a.setCreationDate(item.getCreationDate());
        a.setLastActivityDate(item.getLastActivityDate());

        return a;
    }

    private Comment convertToComment(CommentItem item, String postType) {
        Comment c = new Comment();
        c.setCommentId(item.getCommentId());
        c.setPostId(item.getPostId());
        c.setPostType(postType);
        c.setOwnerUserId(item.getOwnerUserId());
        c.setBody(item.getBody());
        c.setScore(item.getScore() != null ? item.getScore() : 0);

        // 直接存 Long
        c.setCreationDate(item.getCreationDate());

        return c;
    }

    private User convertToUser(UserItem item) {
        User u = new User();
        u.setUserId(item.getUserId());
        u.setDisplayName(item.getDisplayName());
        u.setReputation(item.getReputation() != null ? item.getReputation() : 0);

        // 直接存 Long
        u.setCreationDate(item.getCreationDate());

        return u;
    }

    public long getImportedCount() {
        return questionRepository.count();
    }

    @Transactional
    public void processAllQuestionsTags() {
        log.info("开始批量处理所有问题的标签...");
        List<Question> questions = questionRepository.findAll();
        int processedCount = 0;

        for (Question question : questions) {
            String tagsCache = question.getTagsCache();
            if (tagsCache != null && !tagsCache.isEmpty()) {


                List<String> tagNames;
                if (tagsCache.startsWith("<")) {
                    // 解析 <java><spring> 格式
                    tagNames = new ArrayList<>();
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("<([^>]+)>").matcher(tagsCache);
                    while (matcher.find()) {
                        tagNames.add(matcher.group(1));
                    }
                } else {
                    // 解析 java,spring 格式
                    tagNames = Arrays.asList(tagsCache.split(","));
                }

                // 调用现有的处理逻辑
                processQuestionTags(question.getQuestionId(), tagNames);
                processedCount++;
            }
        }
        log.info("批量处理完成，共处理 {} 个问题的标签数据", processedCount);
    }

    /**
     * 获取最常用的标签
     */
    public List<Tag> getTopTags(int limit) {
        // 调用 Repository 中已有的方法
        return tagRepository.findTopTagsByUsageCount().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
}