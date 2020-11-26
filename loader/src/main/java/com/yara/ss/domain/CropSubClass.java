package com.yara.ss.domain;

public class CropSubClass extends Thing {

    private String classId;
    private String faoId;
    private String mediaUri;
    private String name;

    public CropSubClass(String source, String className, String id, String classId, String faoId, String mediaUri, String name) {
        super(source, className, id);
        this.classId = classId;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
        this.name = name;
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
