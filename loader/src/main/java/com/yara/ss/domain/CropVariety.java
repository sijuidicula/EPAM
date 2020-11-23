package com.yara.ss.domain;

public class CropVariety {

    private String id;
    private String subClassId;
    private String name;

    public CropVariety(String id, String subClassId, String name) {
        this.id = id;
        this.subClassId = subClassId;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public String getName() {
        return name;
    }
}
