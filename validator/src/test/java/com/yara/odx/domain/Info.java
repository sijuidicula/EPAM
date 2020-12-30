package com.yara.odx.domain;

import java.util.List;
import java.util.Map;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Info {
    private int identity;
    private int start;
    private int end;
    private String type;
    private List<String> labels;
    private Map<String, String> properties;

    public Info() {
    }

    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Record{" +
                "identity=" + identity +
                ", start=" + start +
                ", end=" + end +
                ", type='" + type + '\'' +
                ", labels=" + labels +
                ", properties=" + properties +
                "}\n";
    }
}
