package com.xiao.cs209a_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserItem {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("display_name")
    private String displayName;

    private Integer reputation;

    @JsonProperty("creation_date")
    private Long creationDate;

    @JsonProperty("last_access_date")
    private Long lastAccessDate;
}