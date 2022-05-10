package com.app.armlangtextanalyzer.service;

import com.app.armlangtextanalyzer.service.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WordService {

    private final FileService fileService;

    public WordService(FileService fileService) {

        this.fileService = fileService;
    }


    public Response readPdfAndAnalyse(MultipartFile pdfFile) throws IOException {
        PDDocument document = PDDocument.load(pdfFile.getInputStream());
        Map<String, String> resultMap = new HashMap<>();

        if (!document.isEncrypted()) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String originalText = pdfStripper.getText(document);
            String title = originalText.split("\n")[0] + "\n";
            originalText = originalText.replaceAll(title, "");
            title ="\n" + title + "\n";
            originalText = originalText.replaceAll(System.getProperty("line.separator"), "");
            String normalizedText = originalText;
            normalizedText = normalizedText.replaceAll(System.getProperty("line.separator"), "");
            normalizedText = normalizedText.replaceAll("[^ա-ֆևԱ-Ֆ\\s]", "");
            normalizedText = normalizedText.replaceAll(" +", " ").toLowerCase().trim();

            resultMap.put("originalText", originalText);
            resultMap.put("normalizedText", normalizedText);
            resultMap.put("title", title);
            log.info("pdf read");
            document.close();
        }

        return filter(resultMap);
    }

    private Response filter(Map<String, String> map) throws IOException {

        String normalizedText = map.get("normalizedText");

        List<String> pdfText = Arrays.stream(normalizedText.split(" "))
                .collect(Collectors.toList());

        List<String> allStopWords = fileService.getStopWords().stream()
                .map(StopWord::getStopWordName)
                .collect(Collectors.toList());

        List<String> withoutStopWords = pdfText.stream().filter(s -> !allStopWords.contains(s))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toList());

        Set<String> filteredWords = pdfText.stream().filter(s -> !allStopWords.contains(s))
                .filter(s -> s.length() > 2)
                .collect(Collectors.toSet());

        Map<String, Word> words = fileService.getWords().stream()
                .collect(Collectors.toMap(Word::getWord, Function.identity()));

        log.info("filtered");

        return roughAnalyse(map.get("originalText"), map.get("title"),  filteredWords, words, withoutStopWords);
    }

    private Response roughAnalyse(String originalText,
                                  String title,
                                  Set<String> filteredWords,
                                  Map<String, Word> words,
                                  List<String> withoutStopWords) throws IOException {

        Map<String, Word> analysedWords = new HashMap<>();

        Set<String> notAnalysedWords = new HashSet<>();
        final List<Lemma>[] verbs = new List[]{fileService.getVerbs()};
        List<Lemma> nouns = fileService.getNouns();
        verbs[0].addAll(nouns);

        filteredWords.forEach(s -> {
            if (words.get(s) != null) {
                Word word = new Word();
                word.setWord(s);
                word.setLemma(s);
                word.setExplanations(words.get(s).getExplanations());
                analysedWords.putIfAbsent(s, word);
            } else {
                notAnalysedWords.add(s);
            }
        });

        Set<String> strings = new HashSet<>();
        notAnalysedWords.forEach(s -> {
            verbs[0] = verbs[0].stream()
                    .filter(lemma -> s.charAt(0) == lemma.getLemma().charAt(0) || s.charAt(1) == lemma.getLemma().charAt(1))
                    .collect(Collectors.toList());
            boolean flag = true;
            for (Lemma lemma : verbs[0]) {
                if (lemma.getForms().get(s) != null || lemma.getLemma().equals(s)) {
                    List<Explanation> explanations = Collections.emptyList();
                    if (words.get(lemma.getLemma()) != null)
                        explanations = words.get(lemma.getLemma()).getExplanations();
                    Word word = new Word();
                    word.setWord(s);
                    word.setLemma(lemma.getLemma());
                    word.setExplanations(explanations);
                    analysedWords.putIfAbsent(s, word);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                strings.add(s);
            }

        });



        log.info("rough analyse");
        return analyseByHolov(originalText, strings, title,  analysedWords, withoutStopWords, words, filteredWords.size());
    }

    private Response analyseByHolov(String originalText,
                                    Set<String> notAnalysedWords,
                                    String title,
                                    Map<String, Word> analysedWords,
                                    List<String> withoutStopWords,
                                    Map<String, Word> words, int filteredWordsSize) {

        List<Holov> holovs = fileService.getHolovs();
        Set<String> notAnalysedAfterHolov = new HashSet<>();
        notAnalysedWords.forEach(s -> {
            boolean flag = true;
            for (Holov holov : holovs) {
                String plural = "";

                if (s.endsWith(holov.getPlural())) {
                    plural = s.substring(0, s.length() - holov.getPlural().length());
                } else if (s.endsWith(holov.getSingular())) {
                    plural = s.substring(0, s.length() - holov.getSingular().length());
                }

                if (!plural.isEmpty() && words.get(plural) != null) {
                    Word word = new Word();
                    word.setWord(s);
                    word.setLemma(plural);
                    word.setExplanations(words.get(plural).getExplanations());
                    analysedWords.putIfAbsent(s, word);
                    flag = false;
                    break;
                }
            }
            if (flag)
                notAnalysedAfterHolov.add(s);
        });

        log.info("analyse holov");

        return analyseBySuffix(originalText, notAnalysedAfterHolov, title,  analysedWords, withoutStopWords, words, filteredWordsSize);
    }

    private Response analyseBySuffix(String originalText,
                                     Set<String> notAnalysedWords,
                                     String title,
                                     Map<String, Word> analysedWords,
                                     List<String> withoutStopWords,
                                     Map<String, Word> words,
                                     int filteredWordsSize) {

        List<Suffix> suffixes = fileService.getSuffixes();
        Set<String> notAnalysedAfterSuffix = new HashSet<>();

        notAnalysedWords.forEach(s -> {
            boolean flag = true;
            for (Suffix suffix : suffixes) {
                String w = "";
                if (s.endsWith(suffix.getSuffix())) {
                    w = s.substring(0, s.length() - suffix.getSuffix().length());
                }
                if (!w.isEmpty() && words.get(w) != null) {
                    Word word = new Word();
                    word.setWord(s);
                    word.setLemma(w);
                    word.setExplanations(words.get(w).getExplanations());
                    analysedWords.putIfAbsent(s, word);
                    flag = false;
                    break;
                }
            }
            if (flag)
                notAnalysedAfterSuffix.add(s);
        });

        log.info("suffix analyse");

        return analyseByPrefix(originalText, notAnalysedAfterSuffix, title,  analysedWords, withoutStopWords, words, filteredWordsSize);
    }

    private Response analyseByPrefix(String originalText,
                                     Set<String> notAnalysedWords,
                                     String title,
                                     Map<String, Word> analysedWords,
                                     List<String> withoutStopWords,
                                     Map<String, Word> words,
                                     int filteredWordsSize) {

        List<Prefix> prefixes = fileService.getPrefixes();
        Set<String> notAnalysedAfterPrefix = new HashSet<>();

        notAnalysedWords.forEach(s -> {
            boolean flag = true;
            for (Prefix prefix : prefixes) {
                String w = "";
                if (s.startsWith(prefix.getPrefix())) {
                    w = s.substring(s.indexOf(prefix.getPrefix()));
                }

                if (!w.isEmpty() && words.containsKey(w)) {
                    Word word = new Word();
                    word.setWord(s);
                    word.setLemma(w);
                    word.setExplanations(words.get(w).getExplanations());
                    analysedWords.putIfAbsent(s, word);
                    flag = false;
                    break;
                }
            }

            if (flag)
                notAnalysedAfterPrefix.add(s);
        });

        log.info("by suffix");
        return getBySuggesting(originalText, notAnalysedAfterPrefix, title, analysedWords, withoutStopWords , words, filteredWordsSize);
    }


    private Response getBySuggesting(String originalText,
                                     Set<String> notAnalysedAfterPrefix,
                                     String title,
                                     Map<String, Word> analysedWords,
                                     List<String> withoutStopWords,
                                     Map<String, Word> words,
                                     int filteredWordsSize) {

        Set<String> notAnalysedWordsAfterSuggestion = new HashSet<>();
        notAnalysedAfterPrefix.forEach(s -> {
            String suggestion = getSuggestion(s, words);

            if (suggestion != null) {
                Word word = new Word();
                word.setWord(s);
                word.setLemma(suggestion);
                word.setExplanations(words.get(suggestion).getExplanations());
                analysedWords.putIfAbsent(s, word);
            } else
                notAnalysedWordsAfterSuggestion.add(s);
        });

        log.info("suggestion");
        return buildResponse(originalText, notAnalysedWordsAfterSuggestion.size(), title, analysedWords, withoutStopWords, filteredWordsSize);
    }


    private Response buildResponse(String originalText,
                                   int notAnalysedWordsCount,
                                   String title,
                                   Map<String, Word> analysedWords,
                                   List<String> withoutStopWords,
                                   int filteredWordsSize) {

        Response response = new Response();
        response.setOriginalText(originalText);
        response.setNotAnalysedWordCount(Math.max(notAnalysedWordsCount, 0));
        Map<String, List<String>> analysedWordsAndExplanations = new HashMap<>();
        analysedWords.forEach((s, word) -> {
            List<String> explanations =
                    word.getExplanations()
                            .stream()
                            .map(Explanation::getExplanation)
                            .collect(Collectors.toList());

            analysedWordsAndExplanations.put(s,explanations);
        });

        response.setTitle(title);
        response.setExplanationAndColors(chartSeriesData(analysedWordsAndExplanations));
        response.setWordExplanations(analysedWordsAndExplanations);
        response.setAnalysedWordCount(analysedWordsAndExplanations.size());
        response.setOriginalTextCount(originalText.split(" ").length);
        response.setTextCountWithoutStopWord(filteredWordsSize);
        response.setChart(chart(analysedWordsAndExplanations, withoutStopWords));
        response.setWordAndColors(wordsAndColors(analysedWords));
        analysedWords.clear();
        withoutStopWords.clear();
        analysedWordsAndExplanations.clear();
        System.gc();
        log.info("response build");
        return response;
    }


    private String getSuggestion(String s, Map<String, Word> words) {

        String result = suggestionHelper(s, words);

        if (result == null) {
            if (s.startsWith("կ") || s.startsWith("չ"))
                s = s.substring(1);
        }

        return suggestionHelper(s, words);
    }

    public String suggestionHelper(String word, Map<String, Word> words) {

        if (word.endsWith("ս")) {
            String result = word.substring(0, word.lastIndexOf("ս"));
            if (words.get(result) != null)
                return result;
        }

        if (word.startsWith("ամենա")) {
            String result = word.substring(word.lastIndexOf("ամենա"));
            if (words.get(result) != null)
                return result;
        }

        if (word.startsWith("երը")) {
            String result = word.substring(word.lastIndexOf("երը"));
            if (words.get(result) != null)
                return result;
        }

        if (word.startsWith("երն")) {
            String result = word.substring(word.lastIndexOf("երն"));
            if (words.get(result) != null)
                return result;
        }


        if (word.endsWith("ի")) {
            String result = word.substring(0, word.lastIndexOf("ի")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("աց")) {
            String result = word.substring(0, word.lastIndexOf("աց")) + "ալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ի")) {
            String result = word.substring(0, word.lastIndexOf("ի"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ացման")) {
            String result = word.substring(0, word.lastIndexOf("ացման")) + "անալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("մանը")) {
            String result = word.substring(0, word.lastIndexOf("մանը")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ման")) {
            String result = word.substring(0, word.lastIndexOf("ման")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("այի")) {
            String result = word.substring(0, word.lastIndexOf("այի")) + "ա";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ին")) {
            String result = word.substring(0, word.lastIndexOf("ին")) + "ի";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("վում")) {
            String result = word.substring(0, word.lastIndexOf("վում")) + "վել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("նում")) {
            String result = word.substring(0, word.lastIndexOf("նում")) + "նել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եցի")) {
            String result = word.substring(0, word.lastIndexOf("եցի")) + "ենալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եցի")) {
            String result = word.substring(0, word.lastIndexOf("եցի")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ան")) {
            String result = word.substring(0, word.lastIndexOf("ան")) + "ուն";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("անում")) {
            String result = word.substring(0, word.lastIndexOf("անում")) + "անալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("անից")) {
            String result = word.substring(0, word.lastIndexOf("անից")) + "ուն";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ներում")) {
            String result = word.substring(0, word.lastIndexOf("ներում"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ացող")) {
            String result = word.substring(0, word.lastIndexOf("ացող")) + "ալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ության")) {
            String result = word.substring(0, word.lastIndexOf("ության"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ուղու")) {
            String result = word.substring(0, word.lastIndexOf("ուղու")) + "ուղի";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ներն")) {
            String result = word.substring(0, word.lastIndexOf("ներն"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ներ")) {
            String result = word.substring(0, word.lastIndexOf("ներ"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ներս")) {
            String result = word.substring(0, word.lastIndexOf("ներս"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ությանը")) {
            String result = word.substring(0, word.lastIndexOf("ությանը"));
            if (words.get(result) != null)
                return result;
        }


        if (word.endsWith("ններ")) {
            String result = word.substring(0, word.lastIndexOf("ններ")) + "ն";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("թյամբ")) {
            String result = word.substring(0, word.lastIndexOf("թյամբ")) + "թյուն";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ների")) {
            String result = word.substring(0, word.lastIndexOf("ների"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ում")) {
            String result = word.substring(0, word.lastIndexOf("ում")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եցին")) {
            String result = word.substring(0, word.lastIndexOf("եցին")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("վեցին")) {
            String result = word.substring(0, word.lastIndexOf("վեցին")) + "վել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ած")) {
            String result = word.substring(0, word.lastIndexOf("ած")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ն")) {
            String result = word.substring(0, word.lastIndexOf("ն"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("երն")) {
            String result = word.substring(0, word.lastIndexOf("երն"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ացան")) {
            String result = word.substring(0, word.lastIndexOf("ացան")) + "անալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ացած")) {
            String result = word.substring(0, word.lastIndexOf("ացած")) + "անալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ացավ")) {
            String result = word.substring(0, word.lastIndexOf("ացավ")) + "անալ";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("վա")) {
            String result = word.substring(0, word.lastIndexOf("վա"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եց")) {
            String result = word.substring(0, word.lastIndexOf("եց")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("իցս")) {
            String result = word.substring(0, word.lastIndexOf("իցս"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("յից")) {
            String result = word.substring(0, word.lastIndexOf("յից"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("յում")) {
            String result = word.substring(0, word.lastIndexOf("յում"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("նեմ")) {
            String result = word.substring(0, word.lastIndexOf("նեմ")) + "նել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("րքեր")) {
            String result = word.substring(0, word.lastIndexOf("րքեր")) + "իրք";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("րեց")) {
            String result = word.substring(0, word.lastIndexOf("րեց")) + "նել";
            if (words.get(result) != null)
                return result;
        }


        if (word.endsWith("անց")) {
            String result = word.substring(0, word.lastIndexOf("անց"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եր")) {
            String result = word.substring(0, word.lastIndexOf("եր"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ական")) {
            String result = word.substring(0, word.lastIndexOf("ական")) + "ություն";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("նոջ")) {
            String result = word.substring(0, word.lastIndexOf("նոջ")) + "ին";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ում")) {
            String s = word.substring(0, word.lastIndexOf("ում"));
            if (words.get(s + "ալ") != null)
                return s + "ալ";
            if (words.get(s + "ել") != null)
                return s + "ել";
        }

        if (word.endsWith("ացրի")) {
            String result = word.substring(0, word.lastIndexOf("ացրի")) + "ացնել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("վում")) {
            String result = word.substring(0, word.lastIndexOf("վում")) + " վել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եին")) {
            String result = word.substring(0, word.lastIndexOf("եին")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("եի")) {
            String result = word.substring(0, word.lastIndexOf("եի")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("իս")) {
            String result = word.substring(0, word.lastIndexOf("իս"));
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ության")) {
            String result = word.substring(0, word.lastIndexOf("ության")) + "ել";
            if (words.get(result) != null)
                return result;
        }

        if (word.endsWith("ության")) {
            String result = word.substring(0, word.lastIndexOf("ության")) + "ություն";
            if (words.get(result) != null)
                return result;
        }

        return null;
    }

    private Map<String, String> getColors() {
        Map<String, String> map = new HashMap<>();
        map.put("Գոյական", "orange");
        map.put("Ածական", "lightgreen");
        map.put("Կապ", "aquamarine");
        map.put("Մակբայ", "rgb(245, 162, 245)");
        map.put("Շաղկապ", "lightgray");
        map.put("Դերանուն", "coral");
        map.put("Չեզոք բայ", "lightblue");
        map.put("Բայ", "#f1f1a8");
        map.put("Թվական", "#d0bfff");
        map.put("Անդրադարձ բայ", "lightcyan");
        map.put("Անդրադարձ դերանուն", "lightgoldenrodyellow");
        map.put("Անեզրական գոյական", "lightpink");
        map.put("Անորոշ դերանուն", "lightsalmon");
        map.put("Բաշխական թվական", "lightseagreen");
        map.put("Բարբառային բառ", "#77c3c5");
        map.put("Դասական թվական", "#d2763b");
        map.put("Դարձվածք", "powderblue");
        map.put("Դերբայական ձև", "mistyrose");
        map.put("Եղանակավորող բառ", "burlywood");
        map.put("Ենթադրական դերբայ", "#bebe36");
        map.put("Ենթակայական դերբայ", "moccasin");
        map.put("Կոչական բառ", "#6383e7");
        map.put("Հատուկ անուն", "yellowgreen");
        map.put("հարակատար դերբայ", "tan");
        map.put("հարկադրական դերբայ", "tomato");
        map.put("հարցական դերանուն", "#ff54ff");
        map.put("Ձայնարկություն", "cadetblue");
        map.put("Միադիմի բայ", "#e77a53");
        map.put("Ներգործական բայ", "#e38d9a");
        map.put("Ներկա դերբայ", "#6060a1");
        map.put("Պակասավոր բայ", "silver");
        map.put("Պատճառական բայ", "paleturquoise");
        map.put("Վաղակատար դերբայ", "wheat");
        map.put("Վերաբերական", "#c29f44");
        map.put("Տեղանուն", "#e59f5e");
        map.put("Ցուցական դերանուն", "plum");
        map.put("Փոխադարձ բայ", "rgb(255, 204, 108)");

        return map;
    }

    private JSONArray wordsAndColors(Map<String, Word> analysedWords) {
        Map<String, String> colors = getColors();
        JSONArray highlight = new JSONArray();
        analysedWords.forEach((key, value) -> {
            JSONObject object = new JSONObject();
            value.getExplanations().forEach(s -> {
                if (colors.get(s.getExplanation()) != null){
                    object.put("word", key);
                    object.put("color", colors.get(s.getExplanation()));
                }
            });
            object.put("lemma", value.getLemma());
            String explanation = "";
            for (int i = 0; i < value.getExplanations().size(); i++) {
                explanation += value.getExplanations().get(i).getExplanation();
                if (i != value.getExplanations().size() - 1)
                    explanation += ", ";
            }
            object.put("explanation", explanation);
            highlight.add(object);

        });
        return highlight;
    }

    private JSONObject chart(Map<String, List<String>> wordsAndExplanations,
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
        cartSeriesObject.put("data", chartSeriesData(wordsAndExplanations));


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
        drilldown.put("series", drilldownSeriesData(wordsAndExplanations, withoutStopWords));


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

    private JSONArray drilldownSeriesData(Map<String, List<String>> wordsAndExplanations,
                                          List<String> withoutStopWords) {

        Set<String> explanations = new HashSet<>();
        wordsAndExplanations.values().forEach(explanations::addAll);

        Map<String, List<Map.Entry<String, Integer>>> data = new HashMap<>();

        explanations.forEach(s -> {
            List<String> words = new ArrayList<>();
            wordsAndExplanations.forEach((key, value) -> {
                if (value.contains(s)){
                    words.add(key);
                }
            });

            Map<String, Integer> result = new HashMap<>();
            words.forEach(word -> {
                AtomicInteger count = new AtomicInteger();
                withoutStopWords.forEach(withoutStopWord -> {
                    if (word.equals(withoutStopWord))
                        count.getAndIncrement();
                });
                result.putIfAbsent(word, count.get());
            });

            List<Map.Entry<String, Integer>> entries = new ArrayList<>(result.entrySet());
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            List<Map.Entry<String, Integer>> topTenWords = entries
                    .stream()
                    .limit(10)
                    .sorted(Map.Entry
                            .comparingByValue(Comparator
                                    .naturalOrder()))
                    .collect(Collectors.toList());

            data.putIfAbsent(s, topTenWords);
        });

        JSONArray dataArray = new JSONArray();

        data.forEach((key, value) -> {
            JSONObject object = new JSONObject();
            object.put("name", key);
            object.put("id", key);
            JSONArray finalDataArray = new JSONArray();
            value.forEach(entry -> {
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

    private JSONArray chartSeriesData (Map<String, List<String>> wordsAndExplanations) {
        Map<String, Integer> result = new HashMap<>();
        Set<String> explanations = new HashSet<>();
        wordsAndExplanations.values().forEach(explanations::addAll);

        explanations.forEach(s -> {
            AtomicInteger count = new AtomicInteger();
            wordsAndExplanations.forEach((key, value) -> {
                if (value.contains(s)){
                    count.getAndIncrement();
                }
            });

            if (count.get() != 0){
                result.put(s, count.get());
            }
        });

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(result.entrySet());
        entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<Map.Entry<String, Integer>> explanationOrdered = entries
                .stream()
                .limit(10)
                .sorted(Map.Entry
                        .comparingByValue(Comparator
                                .naturalOrder()))
                .collect(Collectors.toList());


    JSONArray jsonArray = new JSONArray();

        explanationOrdered.forEach(entry -> {
            JSONObject object = new JSONObject();
            object.put("name", entry.getKey());
            object.put("color", getColors().get(entry.getKey()));
            object.put("y", entry.getValue());
            object.put("drilldown", entry.getKey());
            jsonArray.add(object);
        });

        return jsonArray;
    }
}
