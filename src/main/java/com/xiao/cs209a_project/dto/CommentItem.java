package com.xiao.cs209a_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentItem {
    @JsonProperty("comment_id")
    private Long commentId;

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("post_type")
    private String postType; // 从API响应中推断

    @JsonProperty("owner")
    private Object owner;

    private Long ownerUserId;

    private String body;

    private Integer score;

    @JsonProperty("creation_date")
    private Long creationDate;

    @JsonProperty("edited")
    private Boolean edited;

    @JsonProperty("reply_to_user")
    private Long replyToUserId;

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