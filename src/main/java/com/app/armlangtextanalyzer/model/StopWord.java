package com.app.armlangtextanalyzer.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class StopWord {

    @NotBlank
    private String stopWordName;
}
