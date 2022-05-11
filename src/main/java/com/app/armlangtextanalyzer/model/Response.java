package com.app.armlangtextanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

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
