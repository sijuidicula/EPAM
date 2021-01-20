package com.yara.odx.domain;

import java.util.Objects;

public class CropGroup extends Thing {

    private String faoId;
    private String name;

    public CropGroup(String source, String className, String id, String faoId, String name) {
        super(source, className, id);
        this.name = name;
        this.faoId = faoId;
    }

    public String getFaoId() {
        return faoId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CropGroup{" +
                "source='" + super.getSource() + '\'' +
                ", className='" + super.getClassName() + '\'' +
                ", id='" + super.getId() + '\'' +
                ", UUid='" + super.getUuId() + '\'' +
                ", faoId='" + faoId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropGroup group = (CropGroup) o;
        return Objects.equals(faoId, group.faoId) &&
                Objects.equals(name, group.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(faoId, name);
    }
}
