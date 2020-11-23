package com.yara.ss.domain;

public class CropSubClass {

    private String id;
    private String classId;
    private String faoId;
    private String mediaUri;
    private String name;

    public CropSubClass(String id, String classId, String faoId, String mediaUri, String name) {
        this.id = id;
        this.classId = classId;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getClassId() {
        return classId;
    }

    public String getFaoId() {
        return faoId;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public String getName() {
        return name;
    }
}
