package com.yara.ss.domain;

import java.util.Objects;

public class CropVariety extends Thing {

    private String subClassId;
    private String name;

    public CropVariety(String source, String className, String id, String subClassId, String name) {
        super(source, className, id, name);
        this.subClassId = subClassId;
        this.name = name;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropVariety variety = (CropVariety) o;
        return Objects.equals(subClassId, variety.subClassId) &&
                Objects.equals(name, variety.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subClassId, name);
    }
}
