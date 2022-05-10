package com.app.armlangtextanalyzer.service.dto;

public enum POS {

    ADJ("Ածական"),
    DERBY("Դերբայ"),
    PUNCT("Կետադրական նշան"),
    ADV("Մակբայ"),
    AUX("Օժանդակ"),
    SYM("Սիմվոլ"),
    INTJ("Ձայնարկություն"),
    CCONJ("Կապ"),
    NOUN("Գոյական"),
    DET("Որոշյալ"),
    PROPN("Հատուկ անուն"),
    NUM("Թվական"),
    VERB("Բայ"),
    PART("Մասնիկ"),
    PRON("Դերանուն"),
    SCONJ("Շաղկապ");

    private final String value;

    POS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
