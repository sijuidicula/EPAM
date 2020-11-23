package com.yara.ss.domain;

public class Country {

    private String id;
    private String name;

    public Country(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + id + '\'' +
                ", countryName='" + name + '\'' +
                '}';
    }
}
