package com.yara.ss.domain;

public class CropDescription extends Thing {

    private String subClassId;
    private boolean chlorideSensitive;
    private String mediaUri;
    private String name;

    public CropDescription(String source, String className, String id, String subClassId, boolean chlorideSensitive, String mediaUri, String name) {
        super(source, className, id);
        this.subClassId = subClassId;
        this.chlorideSensitive = chlorideSensitive;
        this.mediaUri = mediaUri;
        this.name = name;
    }

    public String getSubClassId() {
        return subClassId;
    }

    public boolean isChlorideSensitive() {
        return chlorideSensitive;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public String getName() {
        return name;
    }
}
