package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Word {

    private String word;
    private String lemma;
    private List<Explanation> explanations;

    public Word(String word, List<Explanation> explanations) {
        this.word = word;
        this.explanations = explanations;
    }
}
