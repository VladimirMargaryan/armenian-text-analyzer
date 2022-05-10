package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class StopWord {

    @NotBlank
    private String stopWordName;
}
