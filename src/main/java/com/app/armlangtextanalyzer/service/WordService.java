package com.app.armlangtextanalyzer.service;

import com.app.armlangtextanalyzer.model.POS;
import com.app.armlangtextanalyzer.model.Response;
import com.app.armlangtextanalyzer.model.Word;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WordService {

    private final JSONParser jsonParser;
    private final Map<String, String> colors;

    public WordService(JSONParser jsonParser, Map<String, String> colors) {
        this.jsonParser = jsonParser;
        this.colors = colors;
    }


    public Response readPdfAndAnalyse(MultipartFile pdfFile) throws IOException, InterruptedException, ParseException {
        PDDocument document = PDDocument.load(pdfFile.getInputStream());

        if (!document.isEncrypted()) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String originalText = pdfStripper.getText(document);

            String title = originalText.split("\n")[0] + "\n";
            originalText = originalText.replaceAll(title, "");

            title ="\n" + title + "\n";
            originalText = originalText.replaceAll(System.getProperty("line.separator"), "");

            String normalizedText = originalText.replaceAll("[^ա-ֆևԱ-Ֆ\\s]", "");
            normalizedText = normalizedText.replaceAll(" +", " ").toLowerCase().trim();

            log.info("Text stripped");
            document.close();

            List<String> filteredWords = Arrays.stream(normalizedText.split(" ")).collect(Collectors.toList());

            ProcessBuilder pb = new ProcessBuilder("python3",
                    "src/main/java/com/app/armlangtextanalyzer/pyscripts/analyse.py", normalizedText);
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String result;
            String output = "";
            while ((result = in.readLine()) != null){
                output += result;
            }

            p.waitFor();

            log.info("Text analysed");

            Map<String, Word> analysedWords = new HashMap<>();
            JSONArray analysed = new JSONArray();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(output);
            for (Object jsonObject : jsonArray){
                JSONArray jsonArray1 = (JSONArray) jsonObject;
                for (Object o : jsonArray1) {
                    JSONObject object = (JSONObject) o;
                    Word word = new Word();
                    word.setWord(object.get("text").toString());
                    word.setLemma(object.get("lemma").toString());
                    word.setExplanation(POS.valueOf(object.get("upos").toString()).getValue());
                    object.put("color", colors.get(word.getExplanation()));
                    object.put("explanation", word.getExplanation());
                    analysedWords.putIfAbsent(word.getWord(), word);
                    analysed.add(object);
                }
            }

            return buildResponse(originalText, filteredWords.size() - analysed.size(),
                    title, analysedWords, filteredWords, analysed);

        }

        return null;
    }

    private Response buildResponse(String originalText,
                                   int notAnalysedWordsCount,
                                   String title,
                                   Map<String, Word> analysedWords,
                                   List<String> filteredWords,
                                   JSONArray analysed) {

        Response response = new Response();

        response.setOriginalText(originalText);
        response.setNotAnalysedWordCount(notAnalysedWordsCount);

        response.setTitle(title);
        response.setExplanationAndColors(chartSeriesData(analysedWords));
        response.setAnalysedWordCount(analysedWords.size());
        response.setUniqueWordCount(filteredWords.stream().distinct().count());
        response.setOriginalTextCount(filteredWords.size());
        response.setChart(chart(analysedWords, filteredWords));
        response.setWordAndColors(analysed);
        log.info("response build");
        return response;
    }


    private JSONObject chart(Map<String, Word> analysedWords,
                             List<String> withoutStopWords){

        // for adding to json
        JSONObject chart = new JSONObject();
        chart.put("type", "column");

        // for adding to json
        JSONObject title = new JSONObject();
        title.put("align", "center");
        title.put("text", "Խոսքի մասերը տեքստում");

        JSONObject announceNewData = new JSONObject();
        announceNewData.put("enabled", false);

        // for adding to json
        JSONObject accessibility = new JSONObject();
        accessibility.put("announceNewData", announceNewData);

        // for adding to json
        JSONObject xAxis = new JSONObject();
        xAxis.put("type", "category");

        JSONObject yAxisTitle = new JSONObject();
        yAxisTitle.put("text", "Քանակը տեքստում");

        // for adding to json
        JSONObject yAxis = new JSONObject();
        yAxis.put("title", yAxisTitle);

        // for adding to json
        JSONObject legend = new JSONObject();
        legend.put("enabled", false);

        JSONObject dataLabels = new JSONObject();
        dataLabels.put("enabled", true);
        dataLabels.put("format", "{point.y}");

        JSONObject plotOptionsSeries = new JSONObject();
        plotOptionsSeries.put("borderWidth", 0);
        plotOptionsSeries.put("dataLabels", dataLabels);

        // for adding to json
        JSONObject plotOptions = new JSONObject();
        plotOptions.put("series", plotOptionsSeries);

        // for adding to json
        JSONObject tooltip = new JSONObject();
        tooltip.put("headerFormat", "<span style=\"font-size:11px\">{series.name}</span><br>");
        tooltip.put("pointFormat", "<span style=\"color:{point.color}\">{point.name}</span>: Քանակը տեքստում <b>{point.y}</b><br/>");


        JSONObject cartSeriesObject = new JSONObject();
        cartSeriesObject.put("name", "Խոսքի մաս");
        cartSeriesObject.put("colorByPoint", true);
        cartSeriesObject.put("data", chartSeriesData(analysedWords));


        // for adding to json
        JSONArray chartSeriesArray = new JSONArray();
        chartSeriesArray.add(cartSeriesObject);

        JSONObject position = new JSONObject();
        position.put("align", "right");

        JSONObject breadcrumbs = new JSONObject();
        breadcrumbs.put("position", position);

        // for adding to json
        JSONObject drilldown = new JSONObject();
        drilldown.put("breadcrumbs", breadcrumbs);
        drilldown.put("series", drilldownSeriesData(analysedWords, withoutStopWords));


        JSONObject finalChartJson = new JSONObject();
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

    private JSONArray drilldownSeriesData(Map<String, Word> wordsAndExplanations,
                                          List<String> withoutStopWords) {

        JSONArray dataArray = new JSONArray();

        wordsAndExplanations
                .values()
                .stream()
                .map(Word::getExplanation)
                .forEach(s -> {
                    Map<String, Long> result = new LinkedHashMap<>();
                    wordsAndExplanations.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().getExplanation().equals(s))
                            .map(Map.Entry::getKey)
                            .forEach(word -> {
                                long count = withoutStopWords.stream().filter(word::equals).count();
                                result.putIfAbsent(word, count);
                            });

                    JSONObject object = new JSONObject();
                    object.put("name", s);
                    object.put("id", s);
                    JSONArray finalDataArray = new JSONArray();

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

    private JSONArray chartSeriesData (Map<String, Word> wordsAndExplanations) {
        Map<String, Long> result = new LinkedHashMap<>();

        wordsAndExplanations.values()
                .stream()
                .map(Word::getExplanation)
                .forEach(s -> {
            long explanationCount = wordsAndExplanations.values()
                    .stream()
                    .filter(word -> word.getExplanation().equals(s))
                    .count();
            result.putIfAbsent(s, explanationCount);
        });

        JSONArray jsonArray = new JSONArray();

         result.entrySet()
                .stream()
                .sorted((Map.Entry.comparingByValue(Comparator.naturalOrder())))
                .forEach(entry -> {
                    JSONObject object = new JSONObject();
                    object.put("name", entry.getKey());
                    object.put("color", colors.get(entry.getKey()));
                    object.put("y", entry.getValue());
                    object.put("drilldown", entry.getKey());
                    jsonArray.add(object);
                });

        return jsonArray;
    }
}
