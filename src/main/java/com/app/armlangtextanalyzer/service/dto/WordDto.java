package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WordDto {

    private String word;
    private String lemma;
    private String explanation;
}
