package com.app.armlangtextanalyzer.service;

import com.app.armlangtextanalyzer.service.dto.ResponseDto;
import com.app.armlangtextanalyzer.service.dto.WordDto;
import com.app.armlangtextanalyzer.service.enums.PosType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.apache.logging.log4j.util.Strings.LINE_SEPARATOR;

@Service
@Slf4j
@RequiredArgsConstructor
public class WordService {

    @Value("${py.script.file-path}")
    private String pyScriptPath;

    private final JSONParser jsonParser;
    private final Map<String, String> colors;

    public ResponseDto readPdfAndAnalyse(MultipartFile pdfFile) throws IOException, InterruptedException, ParseException {
        final PDDocument document = PDDocument.load(pdfFile.getInputStream());

        if (!document.isEncrypted()) {
            final PDFTextStripper pdfStripper = new PDFTextStripper();
            final String originalText = pdfStripper.getText(document);
            final String title = originalText.split(LINE_SEPARATOR)[0] + LINE_SEPARATOR;
            final String normalizedText = originalText
                    .replaceAll(title, EMPTY)
                    .replaceAll(System.getProperty("line.separator"), EMPTY)
                    .replaceAll("[^ա-ֆևԱ-Ֆ\\s]", EMPTY)
                    .replaceAll(" +", " ")
                    .toLowerCase()
                    .trim();

            log.info("Text stripped");
            document.close();

            final List<String> filteredWords = Arrays.stream(normalizedText.split(" ")).collect(Collectors.toList());

            final ProcessBuilder pb = new ProcessBuilder("python3", String.join("/", pyScriptPath, "analyse.py"), normalizedText);
            final Process process = pb.start();
            final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result;
            final StringBuilder output = new StringBuilder();
            while ((result = in.readLine()) != null) {
                output.append(result);
            }

            System.out.println(output);
            process.waitFor();

            log.info("Text analysed");

            final Map<String, WordDto> analysedWords = new HashMap<>();
            final JSONArray analysed = new JSONArray();
            final JSONArray jsonArray = (JSONArray) jsonParser.parse(output.toString());
            for (Object jsonObject : jsonArray) {
                final JSONArray jsonArray1 = (JSONArray) jsonObject;
                for (Object o : jsonArray1) {
                    JSONObject object = (JSONObject) o;

                    final WordDto wordDto = new WordDto();
                    wordDto.setWord(object.get("text").toString());
                    wordDto.setLemma(object.get("lemma").toString());
                    wordDto.setExplanation(PosType.valueOf(object.get("upos").toString()).getValue());
                    object.put("color", colors.get(wordDto.getExplanation()));
                    object.put("explanation", wordDto.getExplanation());
                    analysedWords.putIfAbsent(wordDto.getWord(), wordDto);
                    analysed.add(object);
                }
            }

            return buildResponse(
                    originalText,
                    filteredWords.size() - analysed.size(),
                    LINE_SEPARATOR + title + LINE_SEPARATOR,
                    analysedWords,
                    filteredWords,
                    analysed
            );
        }

        return null;
    }

    private ResponseDto buildResponse(String originalText,
                                      int notAnalysedWordsCount,
                                      String title,
                                      Map<String, WordDto> analysedWords,
                                      List<String> filteredWords,
                                      JSONArray analysed) {

        final ResponseDto responseDto = new ResponseDto();

        responseDto.setOriginalText(originalText);
        responseDto.setNotAnalysedWordCount(notAnalysedWordsCount);
        responseDto.setTitle(title);
        responseDto.setExplanationAndColors(chartSeriesData(analysedWords));
        responseDto.setAnalysedWordCount(analysedWords.size());
        responseDto.setUniqueWordCount(filteredWords.stream().distinct().count());
        responseDto.setOriginalTextCount(filteredWords.size());
        responseDto.setChart(chart(analysedWords, filteredWords));
        responseDto.setWordAndColors(analysed);
        log.info("response built");

        return responseDto;
    }


    private JSONObject chart(Map<String, WordDto> analysedWords,
                             List<String> withoutStopWords) {

        // for adding to json
        final JSONObject chart = new JSONObject();
        chart.put("type", "column");

        // for adding to json
        final JSONObject title = new JSONObject();
        title.put("align", "center");
        title.put("text", "Խոսքի մասերը տեքստում");

        final JSONObject announceNewData = new JSONObject();
        announceNewData.put("enabled", false);

        // for adding to json
        final JSONObject accessibility = new JSONObject();
        accessibility.put("announceNewData", announceNewData);

        // for adding to json
        final JSONObject xAxis = new JSONObject();
        xAxis.put("type", "category");

        final JSONObject yAxisTitle = new JSONObject();
        yAxisTitle.put("text", "Քանակը տեքստում");

        // for adding to json
        final JSONObject yAxis = new JSONObject();
        yAxis.put("title", yAxisTitle);

        // for adding to json
        final JSONObject legend = new JSONObject();
        legend.put("enabled", false);

        final JSONObject dataLabels = new JSONObject();
        dataLabels.put("enabled", true);
        dataLabels.put("format", "{point.y}");

        final JSONObject plotOptionsSeries = new JSONObject();
        plotOptionsSeries.put("borderWidth", 0);
        plotOptionsSeries.put("dataLabels", dataLabels);

        // for adding to json
        final JSONObject plotOptions = new JSONObject();
        plotOptions.put("series", plotOptionsSeries);

        // for adding to json
        final JSONObject tooltip = new JSONObject();
        tooltip.put("headerFormat", "<span style=\"font-size:11px\">{series.name}</span><br>");
        tooltip.put("pointFormat", "<span style=\"color:{point.color}\">{point.name}</span>: Քանակը տեքստում <b>{point.y}</b><br/>");

        final JSONObject cartSeriesObject = new JSONObject();
        cartSeriesObject.put("name", "Խոսքի մաս");
        cartSeriesObject.put("colorByPoint", true);
        cartSeriesObject.put("data", chartSeriesData(analysedWords));

        // for adding to json
        final JSONArray chartSeriesArray = new JSONArray();
        chartSeriesArray.add(cartSeriesObject);

        final JSONObject position = new JSONObject();
        position.put("align", "right");

        final JSONObject breadcrumbs = new JSONObject();
        breadcrumbs.put("position", position);

        // for adding to json
        final JSONObject drilldown = new JSONObject();
        drilldown.put("breadcrumbs", breadcrumbs);
        drilldown.put("series", drilldownSeriesData(analysedWords, withoutStopWords));

        final JSONObject finalChartJson = new JSONObject();
        finalChartJson.put("chart", chart);
        finalChartJson.put("title", title);
        finalChartJson.put("accessibility", accessibility);
        finalChartJson.put("xAxis", xAxis);
        finalChartJson.put("yAxis", yAxis);
        finalChartJson.put("legend", legend);
        finalChartJson.put("plotOptions", plotOptions);
        finalChartJson.put("tooltip", tooltip);
        finalChartJson.put("series", chartSeriesArray);
        finalChartJson.put("drilldown", drilldown);

        return finalChartJson;
    }

    private JSONArray drilldownSeriesData(Map<String, WordDto> wordsAndExplanations,
                                          List<String> withoutStopWords) {

        final JSONArray dataArray = new JSONArray();

        wordsAndExplanations
                .values()
                .stream()
                .map(WordDto::getExplanation)
                .forEach(s -> {
                    final Map<String, Long> result = new LinkedHashMap<>();
                    wordsAndExplanations.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().getExplanation().equals(s))
                            .map(Map.Entry::getKey)
                            .forEach(word -> {
                                long count = withoutStopWords.stream().filter(word::equals).count();
                                result.putIfAbsent(word, count);
                            });

                    final JSONObject object = new JSONObject();
                    object.put("name", s);
                    object.put("id", s);
                    final JSONArray finalDataArray = new JSONArray();

                    result.entrySet()
                            .stream()
                            .sorted((Map.Entry.comparingByValue(Comparator.naturalOrder())))
                            .skip(Math.max(0, result.size() - 10))
                            .forEach(entry -> {
                                JSONArray jsonArray = new JSONArray();
                                jsonArray.add(entry.getKey());
                                jsonArray.add(entry.getValue());
                                finalDataArray.add(jsonArray);
                            });

                    object.put("data", finalDataArray);
                    dataArray.add(object);
                });

        return dataArray;
    }

    private JSONArray chartSeriesData(Map<String, WordDto> wordsAndExplanations) {
        final Map<String, Long> result = new LinkedHashMap<>();

        wordsAndExplanations.values()
                .stream()
                .map(WordDto::getExplanation)
                .forEach(s -> {
                    long explanationCount = wordsAndExplanations.values()
                            .stream()
                            .filter(wordDto -> wordDto.getExplanation().equals(s))
                            .count();
                    result.putIfAbsent(s, explanationCount);
                });

        final JSONArray jsonArray = new JSONArray();

        result.entrySet()
                .stream()
                .sorted((Map.Entry.comparingByValue(Comparator.naturalOrder())))
                .skip(Math.max(0, result.size() - 14))
                .forEach(entry -> {
                    final JSONObject object = new JSONObject();
                    object.put("name", entry.getKey());
                    object.put("color", colors.get(entry.getKey()));
                    object.put("y", entry.getValue());
                    object.put("drilldown", entry.getKey());
                    jsonArray.add(object);
                });

        return jsonArray;
    }
}
