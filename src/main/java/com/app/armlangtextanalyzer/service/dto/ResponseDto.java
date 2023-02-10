package com.app.armlangtextanalyzer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {

    private String originalText;
    private String title;
    private int originalTextCount;
    private long uniqueWordCount;
    private int analysedWordCount;
    private int notAnalysedWordCount;
    private JSONArray wordAndColors;
    private JSONArray explanationAndColors;
    private JSONObject chart;
}
