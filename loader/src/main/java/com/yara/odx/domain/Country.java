package com.yara.odx.domain;

import java.util.Objects;

public class Country extends Thing {

    private String name;
    private String fips;
    private String iso2Code;
    private String iso3Code;
    private String m49Code;
    private String continentalSectionUuidRef;
    private String productSetCode;
    private String un;

    public Country(String source, String className, String id, String name, String fips, String iso2Code, String iso3Code,
                   String m49Code, String continentalSectionUuidRef, String productSetCode, String un) {
        super(source, className, id);
        this.name = name;
        this.fips = fips;
        this.iso2Code = iso2Code;
        this.iso3Code = iso3Code;
        this.m49Code = m49Code;
        this.continentalSectionUuidRef = continentalSectionUuidRef;
        this.productSetCode = productSetCode;
        this.un = un;
    }

    public String getName() {
        return name;
    }

    public String getProductSetCode() {
        return productSetCode;
    }

    public String getFips() {
        return fips;
    }

    public String getIso2Code() {
        return iso2Code;
    }

    public String getIso3Code() {
        return iso3Code;
    }

    public String getM49Code() {
        return m49Code;
    }

    public String getContinentalSectionUuidRef() {
        return continentalSectionUuidRef;
    }

    public String getUn() {
        return un;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(name, country.name) &&
                Objects.equals(fips, country.fips) &&
                Objects.equals(iso2Code, country.iso2Code) &&
                Objects.equals(iso3Code, country.iso3Code) &&
                Objects.equals(m49Code, country.m49Code) &&
                Objects.equals(continentalSectionUuidRef, country.continentalSectionUuidRef) &&
                Objects.equals(productSetCode, country.productSetCode) &&
                Objects.equals(un, country.un);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fips, iso2Code, iso3Code, m49Code, continentalSectionUuidRef, productSetCode, un);
    }

    @Override
    public String toString() {
        return "Country{" +
                "name='" + name + '\'' +
                ", fips='" + fips + '\'' +
                ", iso2Code='" + iso2Code + '\'' +
                ", iso3Code='" + iso3Code + '\'' +
                ", m49Code='" + m49Code + '\'' +
                ", continentalSectionUuidRef='" + continentalSectionUuidRef + '\'' +
                ", productSetCode='" + productSetCode + '\'' +
                ", un='" + un + '\'' +
                '}';
    }
}
