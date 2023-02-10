package com.app.armlangtextanalyzer.config;

import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeanConfig {

    @Bean
    public JSONParser jsonParser() {
        return new JSONParser();
    }

    @Bean
    public Map<String, String> colors() {
        Map<String, String> map = new HashMap<>();
        map.put("Գոյական", "orange");
        map.put("Ածական", "lightgreen");
        map.put("Կապ", "aquamarine");
        map.put("Մակբայ", "rgb(245, 162, 245)");
        map.put("Շաղկապ", "#6092ef");
        map.put("Դերանուն", "coral");
        map.put("Մասնիկ", "#8db23d");
        map.put("Բայ", "powderblue");
        map.put("Թվական", "#d0bfff");
        map.put("Հատուկ անուն", "lightcyan");
        map.put("Որոշյալ", "gold");
        map.put("Ձայնարկություն", "lightpink");
        map.put("Սիմվոլ", "lightsalmon");
        map.put("Օժանդակ բայ", "lightseagreen");
        map.put("Կետադրական նշան", "#77c3c5");
        map.put("Ընդունելություն", "paleturquoise");
        map.put("Այլ", "#d2763b");

        return map;
    }
}