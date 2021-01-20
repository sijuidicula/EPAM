package com.yara.odx.domain;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Thing {

    private String source;
    private String className;
    private String id;
    private UUID uuId;

    private String uri;

    public Thing(String source, String className, String id) {
        this.id = id;
        this.source = source;
        this.className = className;
        computeUUid();
        assembleUri();
    }

    public String getSource() {
        return source;
    }

    public String getClassName() {
        return className;
    }

    public String getId() {
        return id;
    }

    public UUID getUuId() {
        return uuId;
    }

    public String getUri() {
        return uri;
    }

    private void computeUUid() {
        byte[] arr = (source + className + id).getBytes(StandardCharsets.UTF_8);
        uuId = UUID.nameUUIDFromBytes(arr);
    }

    private void assembleUri() {
        this.uri = "ODX/" + className + "/" + uuId;
    }
}
