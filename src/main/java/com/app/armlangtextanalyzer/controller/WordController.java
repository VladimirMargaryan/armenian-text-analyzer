package com.app.armlangtextanalyzer.controller;

import com.app.armlangtextanalyzer.service.WordService;
import com.app.armlangtextanalyzer.service.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.LINE_SEPARATOR;


@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping
    public String parsePage() {
        return "upload-pdf";
    }

    @PostMapping("/parse")
    public String uploadPdf(@RequestParam("file") MultipartFile file, Model model) throws IOException, ParseException, InterruptedException {
        if (!file.isEmpty() && Objects.equals(file.getContentType(), "application/pdf")) {

            final ResponseDto responseDto = wordService.readPdfAndAnalyse(file);

            final String statistic = "Բառաքանակը: " + responseDto.getOriginalTextCount() + LINE_SEPARATOR +
                    "Բառաքանակը առանց կրկնվող բառերի: " + responseDto.getUniqueWordCount() + LINE_SEPARATOR +
                    "Մշակված բառերի քանակը: " + responseDto.getAnalysedWordCount() + LINE_SEPARATOR +
                    "Չմշակված բառերի քանակը: " + responseDto.getNotAnalysedWordCount();

            model.addAttribute("statistic", statistic);
            model.addAttribute("wordColorJson", responseDto.getWordAndColors().toJSONString());
            model.addAttribute("explanationAndColors", responseDto.getExplanationAndColors().toJSONString());
            model.addAttribute("chart", responseDto.getChart().toJSONString());
            model.addAttribute("text", responseDto.getOriginalText());
            model.addAttribute("title", responseDto.getTitle());

            return "analyze";
        }

        return null;
    }
}