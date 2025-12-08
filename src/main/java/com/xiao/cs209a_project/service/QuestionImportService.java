package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.dto.QuestionItem;
import com.xiao.cs209a_project.dto.AnswerItem;
import com.xiao.cs209a_project.dto.CommentItem;
import com.xiao.cs209a_project.dto.StackExchangeResponse;
import com.xiao.cs209a_project.dto.UserItem;
import com.xiao.cs209a_project.entity.Question;
import com.xiao.cs209a_project.entity.Answer;
import com.xiao.cs209a_project.entity.Comment;
import com.xiao.cs209a_project.entity.User;
import com.xiao.cs209a_project.entity.Tag;
import com.xiao.cs209a_project.entity.QuestionTag;
import com.xiao.cs209a_project.repository.QuestionRepository;
import com.xiao.cs209a_project.repository.AnswerRepository;
import com.xiao.cs209a_project.repository.CommentRepository;
import com.xiao.cs209a_project.repository.UserRepository;
import com.xiao.cs209a_project.repository.TagRepository;
import com.xiao.cs209a_project.repository.QuestionTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
                    if (questionRepository.existsByQuestionId(questionItem.getQuestionId())) {
                        log.debug("问题 {} 已存在，跳过", questionItem.getQuestionId());
                        continue;
                    }

                    // 1. 导入问题作者的用户数据
                    User questionOwner = importUserIfNeeded(questionItem.getOwnerUserId());

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
                                    savedQuestion.getTitle().substring(0, Math.min(50, savedQuestion.getTitle().length())) : "无标题");

                    // 3. 导入问题的标签数据
                    if (questionItem.getTags() != null && !questionItem.getTags().isEmpty()) {
                        log.info("问题 {} 有 {} 个标签，开始导入标签: {}",
                                savedQuestion.getQuestionId(),
                                questionItem.getTags().size(),
                                questionItem.getTags());

                        try {
                            processQuestionTags(savedQuestion.getQuestionId(), questionItem.getTags());
                            log.info("问题 {} 的标签导入完成", savedQuestion.getQuestionId());
                        } catch (Exception e) {
                            log.error("导入问题 {} 的标签失败，但问题已保存，继续处理",
                                    savedQuestion.getQuestionId(), e);
                        }
                    } else {
                        log.info("问题 {} 没有标签，跳过标签导入", savedQuestion.getQuestionId());
                    }

                    // 4. 导入问题的评论
                    log.info("开始导入问题 {} 的评论数据", savedQuestion.getQuestionId());
                    try {
                        importCommentsForQuestion(savedQuestion.getQuestionId());
                        log.info("问题 {} 的评论导入完成", savedQuestion.getQuestionId());
                    } catch (Exception e) {
                        log.error("导入问题 {} 的评论失败，但问题已保存，继续处理",
                                savedQuestion.getQuestionId(), e);
                    }

                    // 5. 如果问题有答案，导入所有答案和答案评论
                    if (savedQuestion.getAnswerCount() != null && savedQuestion.getAnswerCount() > 0) {
                        log.info("问题 {} 有 {} 个答案，开始导入答案",
                                savedQuestion.getQuestionId(), savedQuestion.getAnswerCount());

                        importAnswersForQuestion(savedQuestion.getQuestionId());
                    } else {
                        log.info("问题 {} 没有答案，跳过答案导入", savedQuestion.getQuestionId());
                    }

                    // 问题间的延迟
                    Thread.sleep(1000);
                }

                // 检查是否还有更多数据
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

    /**
     * 导入指定问题的所有答案和答案评论
     */
    private void importAnswersForQuestion(Long questionId) {
        log.info("开始导入问题 {} 的所有答案数据", questionId);

        int page = 1;
        int pageSize = 100;
        int importedAnswerCount = 0;

        try {
            while (true) {
                // API调用延迟
                Thread.sleep(2000);

                StackExchangeResponse<AnswerItem> response = apiService.getAnswersForQuestion(questionId, page, pageSize);

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    log.info("问题 {} 没有更多答案数据", questionId);
                    break;
                }

                List<Answer> answersToSave = new ArrayList<>();
                for (AnswerItem answerItem : response.getItems()) {
                    // 检查答案是否已存在
                    if (answerRepository.existsByAnswerId(answerItem.getAnswerId())) {
                        log.debug("答案 {} 已存在，跳过", answerItem.getAnswerId());
                        continue;
                    }

                    // 导入答案作者的用户数据
                    User answerOwner = importUserIfNeeded(answerItem.getOwnerUserId());

                    // 转换并保存答案
                    Answer answer = convertToAnswer(answerItem);
                    if (answer != null) {
                        answersToSave.add(answer);
                        importedAnswerCount++;
                    }
                }

                // 批量保存答案
                if (!answersToSave.isEmpty()) {
                    List<Answer> savedAnswers = answerRepository.saveAll(answersToSave);
                    log.info("成功保存 {} 个答案到数据库，问题ID: {}", savedAnswers.size(), questionId);

                    // 为每个答案导入评论
                    for (Answer savedAnswer : savedAnswers) {
                        log.info("开始导入答案 {} 的评论数据", savedAnswer.getAnswerId());
                        try {
                            importCommentsForAnswer(savedAnswer.getAnswerId());
                            log.info("答案 {} 的评论导入完成", savedAnswer.getAnswerId());
                        } catch (Exception e) {
                            log.error("导入答案 {} 的评论失败，但答案已保存，继续处理",
                                    savedAnswer.getAnswerId(), e);
                        }
                    }
                }

                // 检查是否还有更多页数据
                if (!response.isHasMore()) {
                    break;
                }

                page++;
            }

            log.info("问题 {} 的答案导入完成，总共导入 {} 个答案", questionId, importedAnswerCount);

        } catch (Exception e) {
            log.error("导入问题 {} 的答案数据时发生错误，但问题已保存", questionId, e);
        }
    }

    /**
     * 导入问题的所有评论
     */
    private void importCommentsForQuestion(Long questionId) {
        log.info("开始导入问题 {} 的所有评论数据", questionId);

        int page = 1;
        int pageSize = 100;
        int importedCommentCount = 0;

        try {
            while (true) {
                // API调用延迟
                Thread.sleep(1000);

                StackExchangeResponse<CommentItem> response = apiService.getCommentsForQuestion(questionId, page, pageSize);

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    log.info("问题 {} 没有更多评论数据", questionId);
                    break;
                }

                List<Comment> commentsToSave = new ArrayList<>();
                for (CommentItem commentItem : response.getItems()) {
                    // 检查评论是否已存在
                    if (commentRepository.existsByCommentId(commentItem.getCommentId())) {
                        log.debug("评论 {} 已存在，跳过", commentItem.getCommentId());
                        continue;
                    }

                    // 导入评论作者的用户数据
                    User commentOwner = importUserIfNeeded(commentItem.getOwnerUserId());

                    // 转换并保存评论
                    Comment comment = convertToComment(commentItem, "question");
                    if (comment != null) {
                        commentsToSave.add(comment);
                        importedCommentCount++;
                    }
                }

                // 批量保存评论
                if (!commentsToSave.isEmpty()) {
                    List<Comment> savedComments = commentRepository.saveAll(commentsToSave);
                    log.info("成功保存 {} 个评论到数据库，问题ID: {}", savedComments.size(), questionId);
                }

                // 检查是否还有更多页数据
                if (!response.isHasMore()) {
                    break;
                }

                page++;
            }

            log.info("问题 {} 的评论导入完成，总共导入 {} 个评论", questionId, importedCommentCount);

        } catch (Exception e) {
            log.error("导入问题 {} 的评论数据时发生错误，但问题已保存", questionId, e);
        }
    }

    /**
     * 导入答案的所有评论
     */
    private void importCommentsForAnswer(Long answerId) {
        log.info("开始导入答案 {} 的所有评论数据", answerId);

        int page = 1;
        int pageSize = 100;
        int importedCommentCount = 0;

        try {
            while (true) {
                // API调用延迟
                Thread.sleep(1000);

                StackExchangeResponse<CommentItem> response = apiService.getCommentsForAnswer(answerId, page, pageSize);

                if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                    log.info("答案 {} 没有更多评论数据", answerId);
                    break;
                }

                List<Comment> commentsToSave = new ArrayList<>();
                for (CommentItem commentItem : response.getItems()) {
                    // 检查评论是否已存在
                    if (commentRepository.existsByCommentId(commentItem.getCommentId())) {
                        log.debug("评论 {} 已存在，跳过", commentItem.getCommentId());
                        continue;
                    }

                    // 导入评论作者的用户数据
                    User commentOwner = importUserIfNeeded(commentItem.getOwnerUserId());

                    // 转换并保存评论
                    Comment comment = convertToComment(commentItem, "answer");
                    if (comment != null) {
                        commentsToSave.add(comment);
                        importedCommentCount++;
                    }
                }

                // 批量保存评论
                if (!commentsToSave.isEmpty()) {
                    List<Comment> savedComments = commentRepository.saveAll(commentsToSave);
                    log.info("成功保存 {} 个评论到数据库，答案ID: {}", savedComments.size(), answerId);
                }

                // 检查是否还有更多页数据
                if (!response.isHasMore()) {
                    break;
                }

                page++;
            }

            log.info("答案 {} 的评论导入完成，总共导入 {} 个评论", answerId, importedCommentCount);

        } catch (Exception e) {
            log.error("导入答案 {} 的评论数据时发生错误", answerId, e);
        }
    }

    /**
     * 导入用户数据（如果不存在）
     */
    private User importUserIfNeeded(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 检查用户是否已存在
            if (userRepository.existsByUserId(userId)) {
                return userRepository.findById(userId).orElse(null);
            }

            // API调用延迟
            Thread.sleep(1000);

            // 调用API获取用户数据
            UserItem userItem = apiService.getUserInfo(userId);
            if (userItem == null) {
                log.warn("无法获取用户 {} 的数据", userId);
                return null;
            }

            User user = convertToUser(userItem);
            if (user != null) {
                User savedUser = userRepository.save(user);
                log.debug("成功导入用户: ID={}, 名称={}", savedUser.getUserId(), savedUser.getDisplayName());
                return savedUser;
            }

            return null;

        } catch (Exception e) {
            log.error("导入用户数据失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 处理问题的标签数据
     */
    @Transactional
    public void processQuestionTags(Long questionId, List<String> tagNames) {
        if (questionId == null || tagNames == null || tagNames.isEmpty()) {
            log.warn("问题ID或标签列表为空，跳过标签处理");
            return;
        }

        try {
            // 1. 确保所有标签都存在
            List<Tag> tags = ensureTagsExist(tagNames);

            // 2. 创建问题与标签的关联
            createQuestionTagAssociations(questionId, tags);

            // 3. 更新标签使用计数
            updateTagUsageCounts(tags);

            log.debug("成功处理问题 {} 的 {} 个标签: {}", questionId, tags.size(), tagNames);

        } catch (Exception e) {
            log.error("处理问题 {} 的标签数据失败", questionId, e);
        }
    }

    /**
     * 确保标签存在，不存在则创建
     */
    private List<Tag> ensureTagsExist(List<String> tagNames) {
        List<Tag> existingTags = tagRepository.findByTagNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getTagName)
                .collect(Collectors.toSet());

        List<Tag> newTags = new ArrayList<>();
        for (String tagName : tagNames) {
            if (!existingTagNames.contains(tagName)) {
                Tag newTag = new Tag();
                newTag.setTagName(tagName);
                newTag.setUsageCount(0);
                newTags.add(newTag);
            }
        }

        if (!newTags.isEmpty()) {
            List<Tag> savedTags = tagRepository.saveAll(newTags);
            existingTags.addAll(savedTags);
            log.info("创建了 {} 个新标签: {}", savedTags.size(),
                    savedTags.stream().map(Tag::getTagName).collect(Collectors.toList()));
        }

        return existingTags;
    }

    /**
     * 创建问题与标签的关联
     */
    private void createQuestionTagAssociations(Long questionId, List<Tag> tags) {
        List<QuestionTag> questionTags = new ArrayList<>();

        for (Tag tag : tags) {
            // 检查关联是否已存在
            if (questionTagRepository.existsByQuestionIdAndTagId(questionId, tag.getTagId())) {
                continue;
            }

            QuestionTag questionTag = new QuestionTag();
            questionTag.setQuestionId(questionId);
            questionTag.setTagId(tag.getTagId());
            questionTags.add(questionTag);
        }

        if (!questionTags.isEmpty()) {
            questionTagRepository.saveAll(questionTags);
            log.debug("为问题 {} 创建了 {} 个标签关联", questionId, questionTags.size());
        }
    }

    /**
     * 更新标签使用计数
     */
    private void updateTagUsageCounts(List<Tag> tags) {
        for (Tag tag : tags) {
            tagRepository.incrementUsageCount(tag.getTagId());
        }
    }

    /**
     * 批量处理所有问题的标签
     */
    @Transactional
    public void processAllQuestionsTags() {
        List<Question> questions = questionRepository.findAll();
        int processedCount = 0;

        for (Question question : questions) {
            if (question.getTagsCache() != null && !question.getTagsCache().isEmpty()) {
                List<String> tagNames = Arrays.asList(question.getTagsCache().split(","));
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
        return tagRepository.findTopTagsByUsageCount().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Question convertToQuestion(QuestionItem item) {
        try {
            Question question = new Question();
            question.setQuestionId(item.getQuestionId());
            question.setTitle(item.getTitle());
            question.setBody(item.getBody());
            question.setOwnerUserId(item.getOwnerUserId());
            question.setScore(item.getScore() != null ? item.getScore() : 0);
            question.setViewCount(item.getViewCount() != null ? item.getViewCount() : 0);
            question.setAnswerCount(item.getAnswerCount() != null ? item.getAnswerCount() : 0);
            question.setCommentCount(item.getCommentCount() != null ? item.getCommentCount() : 0);
            question.setFavoriteCount(item.getFavoriteCount() != null ? item.getFavoriteCount() : 0);
            question.setIsAnswered(item.getIsAnswered() != null ? item.getIsAnswered() : false);
            question.setAcceptedAnswerId(item.getAcceptedAnswerId());

            // 日期转换
            if (item.getCreationDate() != null) {
                question.setCreationDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getCreationDate()), ZoneOffset.UTC));
            } else {
                question.setCreationDate(LocalDateTime.now());
            }

            if (item.getLastActivityDate() != null) {
                question.setLastActivityDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getLastActivityDate()), ZoneOffset.UTC));
            } else {
                question.setLastActivityDate(question.getCreationDate());
            }

            if (item.getLastEditDate() != null) {
                question.setLastEditDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getLastEditDate()), ZoneOffset.UTC));
            }

            // 设置标签缓存
            if (item.getTags() != null && !item.getTags().isEmpty()) {
                question.setTagsCache(String.join(",", item.getTags()));
            } else {
                question.setTagsCache("");
            }

            return question;

        } catch (Exception e) {
            log.error("转换问题数据失败: questionId={}", item.getQuestionId(), e);
            return null;
        }
    }

    private Answer convertToAnswer(AnswerItem item) {
        try {
            Answer answer = new Answer();
            answer.setAnswerId(item.getAnswerId());
            answer.setQuestionId(item.getQuestionId());
            answer.setOwnerUserId(item.getOwnerUserId());
            answer.setBody(item.getBody());
            answer.setScore(item.getScore() != null ? item.getScore() : 0);
            answer.setIsAccepted(item.getIsAccepted() != null ? item.getIsAccepted() : false);
            answer.setCommentCount(item.getCommentCount() != null ? item.getCommentCount() : 0);

            // 日期转换
            if (item.getCreationDate() != null) {
                answer.setCreationDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getCreationDate()), ZoneOffset.UTC));
            } else {
                answer.setCreationDate(LocalDateTime.now());
            }

            if (item.getLastActivityDate() != null) {
                answer.setLastActivityDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getLastActivityDate()), ZoneOffset.UTC));
            } else {
                answer.setLastActivityDate(answer.getCreationDate());
            }

            if (item.getLastEditDate() != null) {
                answer.setLastEditDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getLastEditDate()), ZoneOffset.UTC));
            }

            return answer;

        } catch (Exception e) {
            log.error("转换答案数据失败: answerId={}", item.getAnswerId(), e);
            return null;
        }
    }

    private Comment convertToComment(CommentItem item, String postType) {
        try {
            Comment comment = new Comment();
            comment.setCommentId(item.getCommentId());
            comment.setPostId(item.getPostId());
            comment.setPostType(postType);
            comment.setOwnerUserId(item.getOwnerUserId());
            comment.setBody(item.getBody());
            comment.setScore(item.getScore() != null ? item.getScore() : 0);

            // 日期转换
            if (item.getCreationDate() != null) {
                comment.setCreationDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getCreationDate()), ZoneOffset.UTC));
            } else {
                comment.setCreationDate(LocalDateTime.now());
            }

            return comment;

        } catch (Exception e) {
            log.error("转换评论数据失败: commentId={}", item.getCommentId(), e);
            return null;
        }
    }

    private User convertToUser(UserItem item) {
        try {
            User user = new User();
            user.setUserId(item.getUserId());
            user.setDisplayName(item.getDisplayName());
            user.setReputation(item.getReputation() != null ? item.getReputation() : 0);

            // 日期转换
            if (item.getCreationDate() != null) {
                user.setCreatedAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getCreationDate()), ZoneOffset.UTC));
            } else {
                user.setCreatedAt(LocalDateTime.now());
            }

            if (item.getLastAccessDate() != null) {
                user.setLastAccessDate(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.getLastAccessDate()), ZoneOffset.UTC));
            }

            return user;

        } catch (Exception e) {
            log.error("转换用户数据失败: userId={}", item.getUserId(), e);
            return null;
        }
    }

    public long getImportedCount() {
        return questionRepository.count();
    }
}