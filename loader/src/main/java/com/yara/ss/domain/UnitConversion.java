package com.yara.ss.domain;

import java.util.Objects;

public class UnitConversion extends Thing {
    private String unitIdRef;
    private String convertToUnitId;
    private String multiplier;
    private String countryIdRef;
    private String name;

    public UnitConversion(String source, String className, String id, String unitIdRef, String convertToUnitId, String multiplier, String countryIdRef) {
        super(source, className, id);
        this.unitIdRef = unitIdRef;
        this.convertToUnitId = convertToUnitId;
        this.multiplier = multiplier;
        this.countryIdRef = countryIdRef;
        this.name = "originally_empty_name";
    }

    public String getUnitIdRef() {
        return unitIdRef;
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
        return Objects.equals(unitIdRef, that.unitIdRef) &&
                Objects.equals(convertToUnitId, that.convertToUnitId) &&
                Objects.equals(multiplier, that.multiplier) &&
                Objects.equals(countryIdRef, that.countryIdRef) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitIdRef, convertToUnitId, multiplier, countryIdRef, name);
    }

    @Override
    public String toString() {
        return "UnitConversion{" +
                "originalUnitId='" + unitIdRef + '\'' +
                ", convertToUnitId='" + convertToUnitId + '\'' +
                ", multiplier='" + multiplier + '\'' +
                ", countryIdRef='" + countryIdRef + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
