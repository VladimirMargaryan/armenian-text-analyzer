package com.app.armlangtextanalyzer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lemma {

    private String lemma;
    private String PartOfSpeech;
    private Map <String, String> forms;

    public Lemma(String lemma, String partOfSpeech) {
        this.lemma = lemma;
        PartOfSpeech = partOfSpeech;
    }

    public Lemma(String lemma) {
        this.lemma = lemma;
    }
}
