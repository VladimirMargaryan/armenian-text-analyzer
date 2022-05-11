package com.app.armlangtextanalyzer.model;

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
    private String explanation;
}
