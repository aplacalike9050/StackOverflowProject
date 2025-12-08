package com.xiao.cs209a_project.service;

import com.xiao.cs209a_project.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StackExchangeApiService {

    // =========================================================================
    // 修改这里：将 Key 填入下方引号中
    // =========================================================================
    private static final String API_KEY = "rl_EpkMmc1Eyc1PZnZJa4BLa47Fq";

    private final WebClient webClient;

    public StackExchangeApiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.stackexchange.com/2.3")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    /**
     * 获取Java相关问题
     */
    public StackExchangeResponse<QuestionItem> getJavaQuestions(int page, int pageSize) {
        try {
            String url = UriComponentsBuilder.fromPath("/questions")
                    .queryParam("page", page)
                    .queryParam("pagesize", Math.min(pageSize, 100))
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("tagged", "java")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", API_KEY) // 【修改】添加 Key
                    .build().toUriString();

            log.info("请求Stack Exchange API: {}", url);

            Map<String, Object> responseMap = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) {
                log.warn("API返回空响应");
                return null;
            }

            return parseResponse(responseMap, this::convertMapToQuestionItem);

        } catch (Exception e) {
            log.error("调用Stack Exchange API失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定问题的答案
     */
    public StackExchangeResponse<AnswerItem> getAnswersForQuestion(Long questionId, int page, int pageSize) {
        try {
            String url = UriComponentsBuilder.fromPath("/questions/" + questionId + "/answers")
                    .queryParam("page", page)
                    .queryParam("pagesize", Math.min(pageSize, 100))
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", API_KEY) // 【修改】添加 Key
                    .build().toUriString();

            log.info("请求答案API: {}", url);

            Map<String, Object> responseMap = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) {
                return null;
            }

            return parseResponse(responseMap, this::convertMapToAnswerItem);

        } catch (Exception e) {
            log.error("调用答案API失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取用户信息
     */
    public UserItem getUserInfo(Long userId) {
        try {
            String url = UriComponentsBuilder.fromPath("/users/" + userId)
                    .queryParam("site", "stackoverflow")
                    .queryParam("key", API_KEY) // 【修改】添加 Key
                    .build().toUriString();

            log.info("请求用户API: {}", url);

            Map<String, Object> responseMap = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null || responseMap.get("items") == null) {
                return null;
            }

            List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) responseMap.get("items");
            if (itemsMap.isEmpty()) {
                return null;
            }

            return convertMapToUserItem(itemsMap.get(0));

        } catch (Exception e) {
            log.error("调用用户API失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取问题的评论
     */
    public StackExchangeResponse<CommentItem> getCommentsForQuestion(Long questionId, int page, int pageSize) {
        try {
            String url = UriComponentsBuilder.fromPath("/questions/" + questionId + "/comments")
                    .queryParam("page", page)
                    .queryParam("pagesize", Math.min(pageSize, 100))
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", API_KEY) // 【修改】添加 Key
                    .build().toUriString();

            log.info("请求问题评论API: {}", url);

            Map<String, Object> responseMap = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) return null;

            return parseResponse(responseMap, itemMap -> convertMapToCommentItem(itemMap, "question"));

        } catch (Exception e) {
            log.error("调用问题评论API失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取答案的评论
     */
    public StackExchangeResponse<CommentItem> getCommentsForAnswer(Long answerId, int page, int pageSize) {
        try {
            String url = UriComponentsBuilder.fromPath("/answers/" + answerId + "/comments")
                    .queryParam("page", page)
                    .queryParam("pagesize", Math.min(pageSize, 100))
                    .queryParam("order", "desc")
                    .queryParam("sort", "creation")
                    .queryParam("site", "stackoverflow")
                    .queryParam("filter", "withbody")
                    .queryParam("key", API_KEY) // 【修改】添加 Key
                    .build().toUriString();

            log.info("请求答案评论API: {}", url);

            Map<String, Object> responseMap = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (responseMap == null) return null;

            return parseResponse(responseMap, itemMap -> convertMapToCommentItem(itemMap, "answer"));

        } catch (Exception e) {
            log.error("调用答案评论API失败: {}", e.getMessage());
            return null;
        }
    }

    // --- 辅助方法 ---

    // 泛型解析响应，避免重复代码
    private <T> StackExchangeResponse<T> parseResponse(Map<String, Object> responseMap, java.util.function.Function<Map<String, Object>, T> mapper) {
        StackExchangeResponse<T> response = new StackExchangeResponse<>();

        if (responseMap.get("has_more") != null)
            response.setHasMore((Boolean) responseMap.get("has_more"));
        if (responseMap.get("quota_remaining") != null)
            response.setQuotaRemaining((Integer) responseMap.get("quota_remaining"));

        List<Map<String, Object>> itemsMap = (List<Map<String, Object>>) responseMap.get("items");
        if (itemsMap != null) {
            List<T> items = itemsMap.stream().map(mapper).toList();
            response.setItems(items);
        }
        return response;
    }

    private QuestionItem convertMapToQuestionItem(Map<String, Object> itemMap) {
        try {
            QuestionItem item = new QuestionItem();
            if (itemMap.get("question_id") != null) item.setQuestionId(((Number) itemMap.get("question_id")).longValue());
            item.setTitle((String) itemMap.get("title"));
            item.setBody((String) itemMap.get("body"));

            if (itemMap.get("owner") instanceof Map) {
                Map<String, Object> ownerMap = (Map<String, Object>) itemMap.get("owner");
                if (ownerMap.get("user_id") != null) item.setOwnerUserId(((Number) ownerMap.get("user_id")).longValue());
            }

            if (itemMap.get("score") != null) item.setScore(((Number) itemMap.get("score")).intValue());
            if (itemMap.get("view_count") != null) item.setViewCount(((Number) itemMap.get("view_count")).intValue());
            if (itemMap.get("answer_count") != null) item.setAnswerCount(((Number) itemMap.get("answer_count")).intValue());
            if (itemMap.get("comment_count") != null) item.setCommentCount(((Number) itemMap.get("comment_count")).intValue());
            if (itemMap.get("favorite_count") != null) item.setFavoriteCount(((Number) itemMap.get("favorite_count")).intValue());
            if (itemMap.get("is_answered") != null) item.setIsAnswered((Boolean) itemMap.get("is_answered"));
            if (itemMap.get("creation_date") != null) item.setCreationDate(((Number) itemMap.get("creation_date")).longValue());
            if (itemMap.get("last_activity_date") != null) item.setLastActivityDate(((Number) itemMap.get("last_activity_date")).longValue());
            if (itemMap.get("last_edit_date") != null) item.setLastEditDate(((Number) itemMap.get("last_edit_date")).longValue());
            if (itemMap.get("accepted_answer_id") != null) item.setAcceptedAnswerId(((Number) itemMap.get("accepted_answer_id")).longValue());
            if (itemMap.get("tags") instanceof List) item.setTags((List<String>) itemMap.get("tags"));

            return item;
        } catch (Exception e) {
            log.error("转换QuestionItem失败: {}", e.getMessage());
            return null;
        }
    }
    private CommentItem convertMapToCommentItem(Map<String, Object> itemMap, String postType) {
        try {
            CommentItem item = new CommentItem();

            if (itemMap.get("comment_id") != null) {
                item.setCommentId(((Number) itemMap.get("comment_id")).longValue());
            }
            if (itemMap.get("post_id") != null) {
                item.setPostId(((Number) itemMap.get("post_id")).longValue());
            }
            item.setPostType(postType);
            item.setBody((String) itemMap.get("body"));

            // 处理 owner (评论者)
            if (itemMap.get("owner") instanceof Map) {
                Map<String, Object> ownerMap = (Map<String, Object>) itemMap.get("owner");
                if (ownerMap.get("user_id") != null) {
                    item.setOwnerUserId(((Number) ownerMap.get("user_id")).longValue());
                }
            }

            if (itemMap.get("score") != null) {
                item.setScore(((Number) itemMap.get("score")).intValue());
            }
            if (itemMap.get("creation_date") != null) {
                item.setCreationDate(((Number) itemMap.get("creation_date")).longValue());
            }
            if (itemMap.get("edited") != null) {
                item.setEdited((Boolean) itemMap.get("edited"));
            }


            if (itemMap.get("reply_to_user") != null) {

                if (itemMap.get("reply_to_user") instanceof Map) {
                    Map<String, Object> replyUserMap = (Map<String, Object>) itemMap.get("reply_to_user");
                    if (replyUserMap.get("user_id") != null) {
                        item.setReplyToUserId(((Number) replyUserMap.get("user_id")).longValue());
                    }
                }

                else if (itemMap.get("reply_to_user") instanceof Number) {
                    item.setReplyToUserId(((Number) itemMap.get("reply_to_user")).longValue());
                }
            }

            return item;

        } catch (Exception e) {
            log.error("转换CommentItem失败: {}", e.getMessage());

            return null;
        }
    }
    private AnswerItem convertMapToAnswerItem(Map<String, Object> itemMap) {
        try {
            AnswerItem item = new AnswerItem();
            if (itemMap.get("answer_id") != null) item.setAnswerId(((Number) itemMap.get("answer_id")).longValue());
            if (itemMap.get("question_id") != null) item.setQuestionId(((Number) itemMap.get("question_id")).longValue());
            item.setBody((String) itemMap.get("body"));

            if (itemMap.get("owner") instanceof Map) {
                Map<String, Object> ownerMap = (Map<String, Object>) itemMap.get("owner");
                if (ownerMap.get("user_id") != null) item.setOwnerUserId(((Number) ownerMap.get("user_id")).longValue());
            }

            if (itemMap.get("score") != null) item.setScore(((Number) itemMap.get("score")).intValue());
            if (itemMap.get("is_accepted") != null) item.setIsAccepted((Boolean) itemMap.get("is_accepted"));
            if (itemMap.get("comment_count") != null) item.setCommentCount(((Number) itemMap.get("comment_count")).intValue());
            if (itemMap.get("creation_date") != null) item.setCreationDate(((Number) itemMap.get("creation_date")).longValue());
            if (itemMap.get("last_activity_date") != null) item.setLastActivityDate(((Number) itemMap.get("last_activity_date")).longValue());
            if (itemMap.get("last_edit_date") != null) item.setLastEditDate(((Number) itemMap.get("last_edit_date")).longValue());

            return item;
        } catch (Exception e) {
            log.error("转换AnswerItem失败: {}", e.getMessage());
            return null;
        }
    }

    private UserItem convertMapToUserItem(Map<String, Object> itemMap) {
        try {
            UserItem item = new UserItem();
            if (itemMap.get("user_id") != null) item.setUserId(((Number) itemMap.get("user_id")).longValue());
            item.setDisplayName((String) itemMap.get("display_name"));
            if (itemMap.get("reputation") != null) item.setReputation(((Number) itemMap.get("reputation")).intValue());
            if (itemMap.get("creation_date") != null) item.setCreationDate(((Number) itemMap.get("creation_date")).longValue());
            if (itemMap.get("last_access_date") != null) item.setLastAccessDate(((Number) itemMap.get("last_access_date")).longValue());
            return item;
        } catch (Exception e) {
            log.error("转换UserItem失败: {}", e.getMessage());
            return null;
        }
    }


}