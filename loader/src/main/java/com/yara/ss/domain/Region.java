package com.yara.ss.domain;

public class Region {

    private String id;
    private String countryId;
    private String name;

    public Region(String id, String countryId, String name) {
        this.id = id;
        this.countryId = countryId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getName() {
        return name;
    }
}
