package com.yara.ss.domain;

public class CropClass extends Thing{

    private String groupId;
    private String faoId;
    private String mediaUri;
    private String name;

    public CropClass(String source, String className, String id,  String groupId, String faoId, String mediaUri, String name) {
        super(source, className, id);
        this.groupId = groupId;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
        this.name = name;
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
                "source='" + super.getSource() + '\'' +
                ", className='" + super.getClassName() + '\'' +
                ", id='" + super.getId() + '\'' +
                ", UUid='" + super.getUuId() + '\'' +
                ", groupId='" + groupId + '\'' +
                ", faoId='" + faoId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
