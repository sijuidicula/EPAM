package com.yara.odx.domain;

import java.util.Objects;

public class CropRegion implements Duplicate {

    private String id;
    private String descriptionId;
    private String countryIdRef;
    private String regionIdRef;
    private String growthScaleIdRef;
    private String defaultSeedingDate;
    private String defaultHarvestDate;
    private String defaultYield;
    private String yieldBaseUnitId;
    private String demandBaseUnitId;
    private String additionalProperties;

    public CropRegion(String id,
                      String descriptionId,
                      String countryIdRef,
                      String regionIdRef,
                      String growthScaleIdRef,
                      String defaultSeedingDate,
                      String defaultHarvestDate,
                      String defaultYield,
                      String yieldBaseUnitId,
                      String demandBaseUnitId,
                      String additionalProperties) {
        this.id = id;
        this.descriptionId = descriptionId;
        this.countryIdRef = countryIdRef;
        this.regionIdRef = regionIdRef;
        this.growthScaleIdRef = growthScaleIdRef;
        this.defaultSeedingDate = defaultSeedingDate;
        this.defaultHarvestDate = defaultHarvestDate;
        this.defaultYield = defaultYield;
        this.yieldBaseUnitId = yieldBaseUnitId;
        this.demandBaseUnitId = demandBaseUnitId;
        this.additionalProperties = additionalProperties;
    }

    public String getId() {
        return id;
    }

    public String getDescriptionId() {
        return descriptionId;
    }

    public String getCountryIdRef() {
        return countryIdRef;
    }

    public String getRegionIdRef() {
        return regionIdRef;
    }

    public String getGrowthScaleIdRef() {
        return growthScaleIdRef;
    }

    public String getDefaultSeedingDate() {
        return defaultSeedingDate;
    }

    public String getDefaultHarvestDate() {
        return defaultHarvestDate;
    }

    public String getDefaultYield() {
        return defaultYield;
    }

    public String getYieldBaseUnitId() {
        return yieldBaseUnitId;
    }

    public String getDemandBaseUnitId() {
        return demandBaseUnitId;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    @Override
    public String toString() {
        return "CropRegion{" +
                "id='" + id + '\'' +
                ", descriptionId='" + descriptionId + '\'' +
                ", countryIdRef='" + countryIdRef + '\'' +
                ", regionIdRef='" + regionIdRef + '\'' +
                ", growthScaleIdRef='" + growthScaleIdRef + '\'' +
                ", defaultSeedingDate='" + defaultSeedingDate + '\'' +
                ", defaultHarvestDate='" + defaultHarvestDate + '\'' +
                ", defaultYield='" + defaultYield + '\'' +
                ", yieldBaseUnitId='" + yieldBaseUnitId + '\'' +
                ", demandBaseUnitId='" + demandBaseUnitId + '\'' +
                ", additionalProperties='" + additionalProperties + '\'' +
                "}\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropRegion that = (CropRegion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(descriptionId, that.descriptionId) &&
                Objects.equals(countryIdRef, that.countryIdRef) &&
                Objects.equals(regionIdRef, that.regionIdRef) &&
                Objects.equals(growthScaleIdRef, that.growthScaleIdRef) &&
                Objects.equals(defaultSeedingDate, that.defaultSeedingDate) &&
                Objects.equals(defaultHarvestDate, that.defaultHarvestDate) &&
                Objects.equals(defaultYield, that.defaultYield) &&
                Objects.equals(yieldBaseUnitId, that.yieldBaseUnitId) &&
                Objects.equals(demandBaseUnitId, that.demandBaseUnitId) &&
                Objects.equals(additionalProperties, that.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, descriptionId, countryIdRef, regionIdRef, growthScaleIdRef, defaultSeedingDate, defaultHarvestDate, defaultYield, yieldBaseUnitId, demandBaseUnitId, additionalProperties);
    }

    @Override
    public boolean sameAs(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropRegion that = (CropRegion) o;
        return Objects.equals(descriptionId, that.descriptionId) &&
                Objects.equals(countryIdRef, that.countryIdRef) &&
                Objects.equals(regionIdRef, that.regionIdRef) &&
                Objects.equals(growthScaleIdRef, that.growthScaleIdRef) &&
//                Objects.equals(def aultSeedingDate, that.defaultSeedingDate) &&
//                Objects.equals(defaultHarvestDate, that.defaultHarvestDate) &&
//                Objects.equals(defaultYield, that.defaultYield) &&
                Objects.equals(yieldBaseUnitId, that.yieldBaseUnitId) &&
                Objects.equals(demandBaseUnitId, that.demandBaseUnitId) &&
                Objects.equals(additionalProperties, that.additionalProperties);
    }
}
