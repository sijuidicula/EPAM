package com.yara.ss.domain;

public class CropGroup extends Thing {

    private String faoId;
    private String mediaUri;
    private String name;

    public CropGroup(String source, String className, String id, String faoId, String mediaUri, String name) {
        super(source, className, id);
        this.name = name;
        this.faoId = faoId;
        this.mediaUri = mediaUri;
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
                "source='" + super.getSource() + '\'' +
                ", className='" + super.getClassName() + '\'' +
                ", id='" + super.getId() + '\'' +
                ", UUid='" + super.getUuId() + '\'' +
                ", faoId='" + faoId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
