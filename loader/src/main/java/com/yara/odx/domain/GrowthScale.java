package com.yara.odx.domain;

public class GrowthScale extends Thing {

    private String name;

    public GrowthScale(String source, String className, String id, String name) {
        super(source, className, id);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
