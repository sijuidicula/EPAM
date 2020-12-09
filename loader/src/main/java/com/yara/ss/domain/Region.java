package com.yara.ss.domain;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region region = (Region) o;
        return Objects.equals(countryId, region.countryId) &&
                Objects.equals(name, region.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId, name);
    }
}
