package com.yara.odx.domain;

import java.util.Objects;

public class Country extends Thing {

    private String name;
    private String productSetCode;

    public Country(String source, String className, String id, String name, String productSetCode) {
        super(source, className, id);
        this.name = name;
        this.productSetCode = productSetCode;
    }

    public String getName() {
        return name;
    }

    public String getProductSetCode() {
        return productSetCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(name, country.name) &&
                Objects.equals(productSetCode, country.productSetCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, productSetCode);
    }

    @Override
    public String toString() {
        return "Country{" +
                "name='" + name + '\'' +
                ", productSetCode='" + productSetCode + '\'' +
                '}';
    }
}
