package com.yara.odx.domain;

import java.util.Objects;

public class CropSubClass extends Thing {

    private String classId;
    private String faoId;
    private String name;

    public CropSubClass(String source, String className, String id, String classId, String faoId, String name) {
        super(source, className, id);
        this.classId = classId;
        this.faoId = faoId;
        this.name = name;
    }

    public String getClassId() {
        return classId;
    }

    public String getFaoId() {
        return faoId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CropSubClass{" +
                "classId='" + classId + '\'' +
                ", faoId='" + faoId + '\'' +
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
                Objects.equals(name, subClass.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, faoId, name);
    }
}
