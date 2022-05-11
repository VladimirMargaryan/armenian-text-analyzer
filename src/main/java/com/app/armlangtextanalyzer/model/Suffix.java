package com.app.armlangtextanalyzer.model;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class Suffix {

    @NotBlank
    private String suffix;
    @NotBlank
    private SuffixType suffixType;
}
