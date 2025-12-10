package com.example.stackoverflowproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//任务2：话题共同出现

public class CoOccurrenceDTO {

    private String tag1;
    private String tag2;
    private Integer occurrenceCount;
    public CoOccurrenceDTO(String tag1, String tag2, Integer occurrenceCount) {
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.occurrenceCount = occurrenceCount;
    }
    public CoOccurrenceDTO() {
    }

    public String getTag1() {
        return tag1;
    }

    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }

    public String getTag2() {
        return tag2;
    }

    public void setTag2(String tag2) {
        this.tag2 = tag2;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }
}
