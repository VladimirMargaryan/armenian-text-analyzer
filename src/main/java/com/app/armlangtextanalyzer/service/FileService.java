package com.app.armlangtextanalyzer.service;

import com.app.armlangtextanalyzer.service.dto.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class FileService {

    @Value("${csv.folder.path}")
    private String folderPath;

    public List<Word> getWords() {
        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().equals("word.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);
                Map<String, String> map = new HashMap<>();

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String word = csvRecord.get("\uFEFFword").toLowerCase();
                        String explanation = csvRecord.get("explanation");

                        if (map.get(word) == null) {
                            map.put(word, explanation);
                        } else {
                            if (map.get(word).length() < explanation.length())
                                map.replace(word, map.get(word), explanation);
                        }
                    }
                }

                List<Word> words = new ArrayList<>();

                final Word[] wordDtos = new Word[1];
                map.forEach((s, s2) -> {
                    List<Explanation> explanations = Arrays.stream(s2.split("\\|"))
                            .map(Explanation::new)
                            .filter(distinctByKey(Explanation::getExplanation))
                            .collect(Collectors.toList());

                    wordDtos[0] = new Word(
                            s,
                            explanations
                    );

                    words.add(wordDtos[0]);

                });

                return words
                        .stream()
                        .filter(distinctByKey(word -> word.getWord().toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return null;
    }

    public List<Prefix> getPrefixes() {
        List<Prefix> prefixes = new ArrayList<>();

        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().endsWith("_prefix.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String prefixName = csvRecord.get(getColumnName(file.getName()));
                        PrefixType prefixType = getPrefixType(file.getName());
                        prefixes.add(new Prefix(prefixName, prefixType));
                    }
                }
            }
        }
        return prefixes;

    }

    public List<Suffix> getSuffixes() {
        List<Suffix> suffixes = new ArrayList<>();

        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().endsWith("_suffix.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String suffixName = csvRecord.get(getColumnName(file.getName()));
                        SuffixType suffixType = getSuffixType(file.getName());
                        suffixes.add(new Suffix(suffixName, suffixType));
                    }
                }
            }
        }
        return suffixes;

    }

    public List<StopWord> getStopWords() {
        List<StopWord> stopWords = new ArrayList<>();

        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().equals("stopword.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String suffixName = csvRecord.get("\uFEFFstopword");
                        stopWords.add(new StopWord(suffixName));
                    }
                }
            }
        }
        return stopWords;

    }

    public List<Holov> getHolovs() {

        List<Holov> holovs = new ArrayList<>();

        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().equals("holov.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String name = csvRecord.get("\uFEFFholov");
                        String singular = csvRecord.get("singular");
                        String plural = csvRecord.get("plural");

                        holovs.add(new Holov(name, singular, plural));

                    }
                }
            }
        }
        return holovs;
    }


    public List<Lemma> getNouns() {
        List<Lemma> lemmas = new ArrayList<>();

        File folder = getFolder();
        File[] files = new File[0];
        if (folder != null) {
            files = folder.listFiles(pathname -> pathname.getName().equals("noun.csv"));
        }

        if (files != null) {
            for (File file : files) {
                List<CSVRecord> csvRecords = readFile(file);

                if (csvRecords != null) {
                    for (CSVRecord csvRecord : csvRecords) {
                        String original = csvRecord.get("\uFEFForiginal");

                        Map<String, String> lemmaForms = new HashMap<>();

                        String AP = csvRecord.get("AP");
                        lemmaForms.put(AP, AP);

                        String APF = csvRecord.get("APF");
                        lemmaForms.put(APF,APF);

                        String APS = csvRecord.get("APS");
                        lemmaForms.put(APS,APS);

                        String AS = csvRecord.get("AS");
                        lemmaForms.put(AS,AS);

                        String ASF = csvRecord.get("ASF");
                        lemmaForms.put(ASF,ASF);

                        String ASS = csvRecord.get("ASS");
                        lemmaForms.put(ASS,ASS);

                        String DP = csvRecord.get("DP");
                        lemmaForms.put(DP,DP);

                        String DPD = csvRecord.get("DPD");
                        lemmaForms.put(DPD,DPD);

                        String DPF = csvRecord.get("DPF");
                        lemmaForms.put(DPF,DPF);

                        String DPS = csvRecord.get("DPS");
                        lemmaForms.put(DPS,DPS);

                        String DS = csvRecord.get("DS");
                        lemmaForms.put(DS,DS);

                        String DSD = csvRecord.get("DSD");
                        lemmaForms.put(DSD,DSD);

                        String DSF = csvRecord.get("DSF");
                        lemmaForms.put(DSF,DSF);

                        String DSS = csvRecord.get("DSS");
                        lemmaForms.put(DSS,DSS);

                        String IP = csvRecord.get("IP");
                        lemmaForms.put(IP,IP);

                        String IPF = csvRecord.get("IPF");
                        lemmaForms.put(IPF,IPF);

                        String IPS = csvRecord.get("IPS");
                        lemmaForms.put(IPS,IPS);

                        String IS = csvRecord.get("IS");
                        lemmaForms.put(IS,IS);

                        String ISF = csvRecord.get("ISF");
                        lemmaForms.put(ISF,ISF);

                        String ISS = csvRecord.get("ISS");
                        lemmaForms.put(ISS,ISS);

                        String LP = csvRecord.get("LP");
                        lemmaForms.put(LP,LP);

                        String LPF = csvRecord.get("LPF");
                        lemmaForms.put(LPF,LPF);

                        String LPS = csvRecord.get("LPS");
                        lemmaForms.put(LPS,LPS);

                        String LS = csvRecord.get("LS");
                        lemmaForms.put(LS,LS);

                        String LSF = csvRecord.get("LSF");
                        lemmaForms.put(LSF,LSF);

                        String LSS = csvRecord.get("LSS");
                        lemmaForms.put(LSS,LSS);

                        String NP = csvRecord.get("NP");
                        lemmaForms.put(NP,NP);

                        String NPD = csvRecord.get("NPD");
                        lemmaForms.put(NPD,NPD);

                        String NPF = csvRecord.get("NPF");
                        lemmaForms.put(NPF,NPF);

                        String NPS = csvRecord.get("NPS");
                        lemmaForms.put(NPS,NPS);

                        String NS = csvRecord.get("NS");
                        lemmaForms.put(NS,NS);

                        String NSD = csvRecord.get("NSD");
                        lemmaForms.put(NSD,NSD);

                        String NSF = csvRecord.get("NSF");
                        lemmaForms.put(NSF,NSF);

                        String NSS = csvRecord.get("NSS");
                        lemmaForms.put(NSS,NSS);

                        lemmas.add(new Lemma(original, "Գոյական", lemmaForms));
                    }
                }
            }
        }

        return lemmas;
    }

    public List<Lemma> getVerbs() throws IOException {
        List<Lemma> lemmas = new ArrayList<>();

        Reader in = new FileReader(folderPath + File.separator + "verb.csv");
        Iterable<CSVRecord> csvRecords = CSVFormat.newFormat(';').withTrim().parse(in);
        int j = 0;
        for (CSVRecord csvRecord : csvRecords) {
            if (j != 0) {
                Lemma lemma = new Lemma();
                Map<String, String> lemmaForms = new HashMap<>();
                lemma.setLemma(csvRecord.get(0));
                lemma.setPartOfSpeech("Բայ");
                for (int i = 1; i <= 200; i++) {
                lemmaForms.put(csvRecord.get(i), csvRecord.get(i));
                }
                lemma.setForms(lemmaForms);
                lemmas.add(lemma);
            }
            j++;
        }
        return lemmas;
    }


    private List<CSVRecord> readFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            CSVParser csvParser =
                    CSVFormat.newFormat(';')
                            .withFirstRecordAsHeader()
                            .withIgnoreEmptyLines(true)
                            .withTrim()
                            .parse(reader);

            return csvParser.getRecords();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;

    }

    private File getFolder() {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory())
            return folder;
        return null;
    }

    private String getColumnName(String filename) {
        return "\uFEFF" + filename.substring(0, filename.indexOf("."));
    }

    private PrefixType getPrefixType(String filename) {
        PrefixType prefixType = null;

        if (filename.equals("adjective_prefix.csv"))
            prefixType = PrefixType.ADJECTIVE;

        if (filename.equals("noun_prefix.csv"))
            prefixType = PrefixType.NOUN;

        return prefixType;
    }

    private SuffixType getSuffixType(String filename) {
        SuffixType suffixType = null;

        if (filename.equals("adjective_suffix.csv"))
            suffixType = SuffixType.ADJECTIVE;

        if (filename.equals("adverb_suffix.csv"))
            suffixType = SuffixType.ADVERB;

        if (filename.equals("noun_suffix.csv"))
            suffixType = SuffixType.NOUN;

        if (filename.equals("number_suffix.csv"))
            suffixType = SuffixType.NUMBER;

        if (filename.equals("verb-derby_suffix.csv"))
            suffixType = SuffixType.VERB_DERBY;

        return suffixType;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> keySet = ConcurrentHashMap.newKeySet();
        return t -> keySet.add(keyExtractor.apply(t));
    }


}
