package com.app.armlangtextanalyzer.service.dto;

public enum SuffixType {
    ADJECTIVE("Ածականակերտ վերջածանց"),
    ADVERB("Մակբայակերտ վերջածանց"),
    NOUN("Գոյականակերտ վերջածանց"),
    NUMBER("Թվականակերտ վերջածանց"),
    VERB_DERBY("Դերբայակերտ վերջածանց");

    private final String value;

    SuffixType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
