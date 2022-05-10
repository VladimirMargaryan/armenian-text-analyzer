package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class Prefix {

    @NotBlank
    private String prefix;
    @NotBlank
    private PrefixType prefixType;
}
