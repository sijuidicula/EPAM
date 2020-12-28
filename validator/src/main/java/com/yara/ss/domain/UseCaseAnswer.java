package com.yara.ss.domain;

import java.util.Map;

public class UseCaseAnswer {

    private Map<String, Info> infoMap;

    public UseCaseAnswer() {
    }

    public Map<String, Info> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, Info> infoMap) {
        this.infoMap = infoMap;
    }

    @Override
    public String toString() {
        return "UseCaseAnswer{" +
                "infoMap=" + infoMap +
                '}';
    }
}
