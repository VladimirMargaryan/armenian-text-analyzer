package com.app.armlangtextanalyzer.config;

import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeanConfig {


    @Bean
    public JSONParser jsonParser(){
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


//        map.put("Գոյական", "orange");
//        map.put("Ածական", "lightgreen");
//        map.put("Կապ", "aquamarine");
//        map.put("Մակբայ", "rgb(245, 162, 245)");
//        map.put("Շաղկապ", "lightgray");
//        map.put("Դերանուն", "coral");
//        map.put("Չեզոք բայ", "lightblue");
//        map.put("Բայ", "#f1f1a8");
//        map.put("Թվական", "#d0bfff");
//        map.put("Անդրադարձ բայ", "lightcyan");
//        map.put("Անդրադարձ դերանուն", "lightgoldenrodyellow");
//        map.put("Անեզրական գոյական", "lightpink");
//        map.put("Անորոշ դերանուն", "lightsalmon");
//        map.put("Բաշխական թվական", "lightseagreen");
//        map.put("Բարբառային բառ", "#77c3c5");
//        map.put("Դասական թվական", "#d2763b");
//        map.put("Դարձվածք", "powderblue");
//        map.put("Դերբայական ձև", "mistyrose");
//        map.put("Եղանակավորող բառ", "burlywood");
//        map.put("Ենթադրական դերբայ", "#bebe36");
//        map.put("Ենթակայական դերբայ", "moccasin");
//        map.put("Կոչական բառ", "#6383e7");
//        map.put("Հատուկ անուն", "yellowgreen");
//        map.put("հարակատար դերբայ", "tan");
//        map.put("հարկադրական դերբայ", "tomato");
//        map.put("հարցական դերանուն", "#ff54ff");
//        map.put("Ձայնարկություն", "cadetblue");
//        map.put("Միադիմի բայ", "#e77a53");
//        map.put("Ներգործական բայ", "#e38d9a");
//        map.put("Ներկա դերբայ", "#6060a1");
//        map.put("Պակասավոր բայ", "silver");
//        map.put("Պատճառական բայ", "paleturquoise");
//        map.put("Վաղակատար դերբայ", "wheat");
//        map.put("Վերաբերական", "#c29f44");
//        map.put("Տեղանուն", "#e59f5e");
//        map.put("Ցուցական դերանուն", "plum");
//        map.put("Փոխադարձ բայ", "rgb(255, 204, 108)");

        return map;
    }

}
