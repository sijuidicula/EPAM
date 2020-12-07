package com.yara.ss.domain;

import java.util.Objects;

public class UnitConversion extends Thing {
    private String originalUnitId;
    private String convertToUnitId;
    private String multiplier;
    private String countryIdRef;
    private String name;

    public UnitConversion(String source, String className, String id, String name, String originalUnitId, String convertToUnitId, String multiplier, String countryIdRef) {
        super(source, className, id, name);
        this.originalUnitId = originalUnitId;
        this.convertToUnitId = convertToUnitId;
        this.multiplier = multiplier;
        this.countryIdRef = countryIdRef;
        this.name = name;
    }

    public String getOriginalUnitId() {
        return originalUnitId;
    }

    public String getConvertToUnitId() {
        return convertToUnitId;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public String getCountryIdRef() {
        return countryIdRef;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitConversion that = (UnitConversion) o;
        return Objects.equals(originalUnitId, that.originalUnitId) &&
                Objects.equals(convertToUnitId, that.convertToUnitId) &&
                Objects.equals(multiplier, that.multiplier) &&
                Objects.equals(countryIdRef, that.countryIdRef) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalUnitId, convertToUnitId, multiplier, countryIdRef, name);
    }

    @Override
    public String toString() {
        return "UnitConversion{" +
                "originalUnitId='" + originalUnitId + '\'' +
                ", convertToUnitId='" + convertToUnitId + '\'' +
                ", multiplier='" + multiplier + '\'' +
                ", countryIdRef='" + countryIdRef + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
