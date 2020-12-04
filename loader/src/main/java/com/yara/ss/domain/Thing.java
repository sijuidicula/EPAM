package com.yara.ss.domain;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Thing {

    private String source;
    private String className;
    private String id;
    private UUID uuId;

    //TODO need to remove "name" from arguments or from all child classes
    public Thing(String source, String className, String id, String name) {
        this.id = id;
        this.source = source;
        this.className = className;
        computeUUid(source + className + id + name);
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

    private void computeUUid(String string) {
        byte[] arr = string.getBytes(StandardCharsets.UTF_8);
        uuId = UUID.nameUUIDFromBytes(arr);
    }
}
