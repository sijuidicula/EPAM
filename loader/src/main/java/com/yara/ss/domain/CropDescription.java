package com.yara.ss.domain;

import java.util.Objects;

public class CropDescription extends Thing {

    private String subClassId;
    private String chlorideSensitive;
    private String mediaUri;
    private String name;

    public CropDescription(String source, String className, String id, String subClassId, String chlorideSensitive, String mediaUri, String name) {
        super(source, className, id, name);
        this.subClassId = subClassId;
        this.chlorideSensitive = chlorideSensitive;
        this.mediaUri = mediaUri;
        this.name = name;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public String isChlorideSensitive() {
        return chlorideSensitive;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CropDescription{" +
                "subClassId='" + subClassId + '\'' +
                ", chlorideSensitive='" + chlorideSensitive + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropDescription that = (CropDescription) o;
        return Objects.equals(subClassId, that.subClassId) &&
                Objects.equals(chlorideSensitive, that.chlorideSensitive) &&
                Objects.equals(mediaUri, that.mediaUri) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subClassId, chlorideSensitive, mediaUri, name);
    }
}
