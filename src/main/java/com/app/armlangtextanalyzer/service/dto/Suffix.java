package com.app.armlangtextanalyzer.service.dto;

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
