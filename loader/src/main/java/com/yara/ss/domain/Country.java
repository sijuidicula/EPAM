package com.yara.ss.domain;

import java.util.Objects;

public class Country extends Thing {

    private String name;

    public Country(String source, String className ,String id, String name) {
        super(source, className, id, name);
        this.name = name;
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Country{" +
                "source='" + super.getSource() + '\'' +
                ", className='" + super.getClassName() + '\'' +
                ", id='" + super.getId() + '\'' +
                ", UUid='" + super.getUuId() + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(name, country.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
