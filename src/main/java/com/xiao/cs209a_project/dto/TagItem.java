package com.xiao.cs209a_project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TagItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("count")
    private Integer count;
}