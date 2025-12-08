package com.xiao.cs209a_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class QuestionItem {
    @JsonProperty("question_id")
    private Long questionId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("owner")
    private Object owner;

    @JsonProperty("owner_user_id")
    private Long ownerUserId;

    @JsonProperty("score")
    private Integer score;

    @JsonProperty("view_count")
    private Integer viewCount;

    @JsonProperty("answer_count")
    private Integer answerCount;

    @JsonProperty("comment_count")
    private Integer commentCount;

    @JsonProperty("favorite_count")
    private Integer favoriteCount;

    @JsonProperty("is_answered")
    private Boolean isAnswered;

    @JsonProperty("accepted_answer_id")
    private Long acceptedAnswerId;

    @JsonProperty("creation_date")
    private Long creationDate;

    @JsonProperty("last_activity_date")
    private Long lastActivityDate;

    @JsonProperty("last_edit_date")
    private Long lastEditDate;

    @JsonProperty("tags")
    private List<String> tags;
}