package com.yara.ss.domain;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "CropSubClass{" +
                "classId='" + classId + '\'' +
                ", faoId='" + faoId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropSubClass subClass = (CropSubClass) o;
        return Objects.equals(classId, subClass.classId) &&
                Objects.equals(faoId, subClass.faoId) &&
                Objects.equals(mediaUri, subClass.mediaUri) &&
                Objects.equals(name, subClass.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, faoId, mediaUri, name);
    }
}
