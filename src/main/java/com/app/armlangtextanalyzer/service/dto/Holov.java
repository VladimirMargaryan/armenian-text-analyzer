package com.app.armlangtextanalyzer.service.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class Holov {

    @NotBlank
    private String name;
    @NotBlank
    private String singular;
    @NotBlank
    private String plural;

    public Holov(String name) {
        this.name = name;
    }
}
