package com.yara.ss.domain;

public class Country extends Thing {

    private String name;

    public Country(String source, String className ,String id, String name) {
        super(source, className, id);
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
}
