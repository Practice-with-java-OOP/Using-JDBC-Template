package com.jidian.cosalon.migration.pos365.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse<T> {
    @JsonProperty("__count")
    private Integer count;
    private List<T> results;
    @JsonProperty("Timestamp")
    private String timestamp;
}
