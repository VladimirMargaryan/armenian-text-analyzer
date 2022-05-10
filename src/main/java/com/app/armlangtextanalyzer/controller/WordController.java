package com.app.armlangtextanalyzer.controller;

import com.app.armlangtextanalyzer.service.WordService;
import com.app.armlangtextanalyzer.service.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@Slf4j
@RequestMapping("/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }


    @GetMapping("/parse")
    public String parsePage() {
        log.info("main page route");
        return "upload-pdf";
    }


    @PostMapping("/parse/pdf")
    public String uploadPdf(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (!file.isEmpty() && Objects.equals(file.getContentType(), "application/pdf")) {
            Response response = wordService.readPdfAndAnalyse(file);
            String statistic = "Բառաքանակը: " + response.getOriginalTextCount() + "\n" +
                    "Բառաքանակը առանց «կանգ-առ» բառերի: " + response.getTextCountWithoutStopWord() + "\n" +
                    "Մշակված բառերի քանակը: " + response.getAnalysedWordCount() + "\n" +
                    "Չմշակված բառերի քանակը: " + response.getNotAnalysedWordCount();
            log.info("uploaded page route");
            model.addAttribute("statistic", statistic);
            model.addAttribute("wordColorJson", response.getWordAndColors().toJSONString());
            model.addAttribute("explanationAndColors", response.getExplanationAndColors().toJSONString());
            model.addAttribute("chart", response.getChart().toJSONString());
            model.addAttribute("text", response.getOriginalText());
            model.addAttribute("title", response.getTitle());
            return "analyze";

        }
        return null;
    }

}
