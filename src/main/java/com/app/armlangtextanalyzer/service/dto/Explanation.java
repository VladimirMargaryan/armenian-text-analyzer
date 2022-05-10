package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Explanation {
    private String explanation;

    public Explanation(String explanation) {
        this.explanation = explanation;
    }
}
