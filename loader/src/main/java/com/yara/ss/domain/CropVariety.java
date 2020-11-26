package com.yara.ss.domain;

public class CropVariety extends Thing {

    private String subClassId;
    private String name;

    public CropVariety(String source, String className, String id, String subClassId, String name) {
        super(source, className, id);
        this.subClassId = subClassId;
        this.name = name;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public String getName() {
        return name;
    }
}
