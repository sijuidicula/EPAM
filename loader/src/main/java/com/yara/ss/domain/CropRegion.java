package com.yara.ss.domain;

public class CropRegion {

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
}
