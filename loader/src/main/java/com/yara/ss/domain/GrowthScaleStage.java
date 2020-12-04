package com.yara.ss.domain;

import java.util.Objects;

public class GrowthScaleStage extends Thing {

    private String growthScaleId;
    private String growthScaleStageDescription;
    //  Should be float but will be String as the cell in Excel is empty
    private String ordinal;
    //    private float ordinal;
//  Should be float but will be String as the cell in Excel is empty
    private String baseOrdinal;
//    private float baseOrdinal;

    public GrowthScaleStage(String source, String className, String id, String name,
                            String growthScaleId, String growthScaleStageDescription, String ordinal, String baseOrdinal) {
        super(source, className, id, name);
        this.growthScaleId = growthScaleId;
        this.growthScaleStageDescription = growthScaleStageDescription;
        this.ordinal = ordinal;
        this.baseOrdinal = baseOrdinal;
    }

    public String getGrowthScaleId() {
        return growthScaleId;
    }

    public String getGrowthScaleStageDescription() {
        return growthScaleStageDescription;
    }

    public String getOrdinal() {
        return ordinal;
    }

    public String getBaseOrdinal() {
        return baseOrdinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrowthScaleStage that = (GrowthScaleStage) o;
        return Objects.equals(ordinal, that.ordinal) &&
                Objects.equals(baseOrdinal, that.baseOrdinal) &&
                Objects.equals(growthScaleId, that.growthScaleId) &&
                Objects.equals(growthScaleStageDescription, that.growthScaleStageDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(growthScaleId, growthScaleStageDescription, ordinal, baseOrdinal);
    }

    @Override
    public String toString() {
        return "GrowthScaleStage{" +
                "growthScaleId='" + growthScaleId + '\'' +
                ", growthScaleStageDescription='" + growthScaleStageDescription + '\'' +
                ", ordinal=" + ordinal +
                ", baseOrdinal=" + baseOrdinal +
                '}';
    }
}
