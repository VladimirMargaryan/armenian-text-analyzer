package com.app.armlangtextanalyzer.model;

public enum POS {

    ADJ("Ածական"),
    ADP("Ընդունելություն"),
    PUNCT("Կետադրական նշան"),
    ADV("Մակբայ"),
    AUX("Օժանդակ բայ"),
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
    SCONJ("Շաղկապ"),
    X("Այլ");

    private final String value;

    POS(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
