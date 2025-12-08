package com.xiao.cs209a_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnswerItem {
    @JsonProperty("answer_id")
    private Long answerId;

    @JsonProperty("question_id")
    private Long questionId;

    @JsonProperty("owner")
    private Object owner;

    private Long ownerUserId;

    private String body;

    private Integer score;

    @JsonProperty("is_accepted")
    private Boolean isAccepted;

    @JsonProperty("creation_date")
    private Long creationDate;

    @JsonProperty("last_activity_date")
    private Long lastActivityDate;

    @JsonProperty("last_edit_date")
    private Long lastEditDate;

    @JsonProperty("comment_count")
    private Integer commentCount;

    public Long getOwnerUserId() {
        if (ownerUserId != null) {
            return ownerUserId;
        }
        if (owner instanceof java.util.Map) {
            java.util.Map<?, ?> ownerMap = (java.util.Map<?, ?>) owner;
            Object userId = ownerMap.get("user_id");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }
}