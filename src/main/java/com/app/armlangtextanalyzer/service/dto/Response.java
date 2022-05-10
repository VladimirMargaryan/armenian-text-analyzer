package com.app.armlangtextanalyzer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    private String originalText;
    private String title;
    private int originalTextCount;
    private int textCountWithoutStopWord;
    private int analysedWordCount;
    private int notAnalysedWordCount;
    private Map<String, List<String>> wordExplanations;
    private Map<String, Integer> wordCountByExplanation;
    private JSONArray wordAndColors;
    private JSONArray explanationAndColors;
    private JSONObject chart;
}
