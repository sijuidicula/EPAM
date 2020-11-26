package com.yara.ss.domain;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Thing {

    private String source;
    private String className;
    private String id;
    private UUID uuId;

    public Thing(String source, String className, String id) {
        this.id = id;
        this.source = source;
        this.className = className;
        String str = source + className + id;
        byte[] arr = str.getBytes(StandardCharsets.UTF_8);
        uuId = UUID.nameUUIDFromBytes(arr);
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
}
