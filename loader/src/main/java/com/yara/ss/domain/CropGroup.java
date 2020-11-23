package com.yara.ss.domain;

public class CropGroup {

    private String id;
    private String faoId;
    private String mediaUri;
    private String name;

    public CropGroup(String id, String faoId, String mediaUri, String name) {
        this.id = id;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
        this.name = name;
    }

    public String getId() {
        return id;
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
        return "CropGroup{" +
                "id='" + id + '\'' +
                ", faoId='" + faoId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
