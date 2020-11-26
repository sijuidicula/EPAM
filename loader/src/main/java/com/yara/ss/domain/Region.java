package com.yara.ss.domain;

public class Region extends Thing {

    private String countryId;
    private String name;

    public Region(String source, String className, String id, String countryId, String name) {
        super(source, className, id);
        this.countryId = countryId;
        this.name = name;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getName() {
        return name;
    }
}
