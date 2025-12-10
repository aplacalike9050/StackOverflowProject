package com.example.stackoverflowproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//任务3：常见陷阱

public class PitfallDTO {

    private String pitfallName;
    private Integer frequency;
    private Double percentage;

    public PitfallDTO(String pitfallName, Integer frequency, Double percentage) {
        this.pitfallName = pitfallName;
        this.frequency = frequency;
        this.percentage = percentage;
    }

    public PitfallDTO() {
    }

    public String getPitfallName() {
        return pitfallName;
    }

    public void setPitfallName(String pitfallName) {
        this.pitfallName = pitfallName;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
