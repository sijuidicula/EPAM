package com.yara.odx.domain;

import java.util.Objects;

public class FertilizerRegion implements Duplicate {

    private String id;
    private String countryId;
    private String regionId;
    private String localizedName;
    private String productId;
    private String isAvailable;
    private String applicationTags;

    public FertilizerRegion(String id, String countryId, String regionId, String localizedName, String productId, String isAvailable, String applicationTags) {
        this.id = id;
        this.countryId = countryId;
        this.regionId = regionId;
        this.localizedName = localizedName;
        this.productId = productId;
        this.isAvailable = isAvailable;
        this.applicationTags = applicationTags;
    }

    public String getId() {
        return id;
    }

    public String getCountryId() {
        return countryId;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public String getProductId() {
        return productId;
    }

    public String getIsAvailable() {
        return isAvailable;
    }

    public String getApplicationTags() {
        return applicationTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FertilizerRegion that = (FertilizerRegion) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(countryId, that.countryId) &&
                Objects.equals(regionId, that.regionId) &&
                Objects.equals(localizedName, that.localizedName) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(isAvailable, that.isAvailable) &&
                Objects.equals(applicationTags, that.applicationTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, countryId, regionId, localizedName, productId, isAvailable, applicationTags);
    }

    @Override
    public String toString() {
        return "FertilizerRegion{" +
                "id='" + id + '\'' +
                ", countryId='" + countryId + '\'' +
                ", regionId='" + regionId + '\'' +
                ", localizedName='" + localizedName + '\'' +
                ", productId='" + productId + '\'' +
                ", isAvailable='" + isAvailable + '\'' +
                ", applicationTags='" + applicationTags + '\'' +
                '}';
    }

    @Override
    public boolean sameAs(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FertilizerRegion that = (FertilizerRegion) o;
        return Objects.equals(countryId, that.countryId) &&
                Objects.equals(regionId, that.regionId) &&
                Objects.equals(localizedName, that.localizedName) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(isAvailable, that.isAvailable) &&
                Objects.equals(applicationTags, that.applicationTags);
    }
}
