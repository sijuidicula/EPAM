package com.yara.ss.domain;

public class CropClass {

    private String id;
    private String groupId;
    private String faoId;
    private String mediaUri;
    private String name;

    public CropClass(String id, String groupId, String faoId, String mediaUri, String name) {
        this.id = id;
        this.groupId = groupId;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
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
        return "CropClass{" +
                "id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", faoId='" + faoId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
