package com.app.armlangtextanalyzer.service.dto;

public enum PrefixType {
    ADJECTIVE("Ածականակերտ նախածանց"),
    NOUN("Գոյականակերտ նախածանց");

    private final String value;

    PrefixType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
