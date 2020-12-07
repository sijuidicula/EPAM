package com.yara.ss.domain;

import java.util.Objects;

public class Unit extends Thing {

    private String name;
    private String tag;

    public Unit(String source, String className, String id, String name, String tag) {
        super(source, className, id, name);
        this.name = name;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return Objects.equals(name, unit.name) &&
                Objects.equals(tag, unit.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tag);
    }

    @Override
    public String toString() {
        return "Unit{" +
                "name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
