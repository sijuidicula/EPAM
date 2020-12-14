package com.yara.ss.tmp;

import com.yara.ss.domain.*;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.util.List;

public class Experiments {
    public static void main(String[] args) {

        String fileName = "loader/src/main/resources/Polaris_DB_Data_V4.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<Country> countries = reader.readCountryFromExcel(fileName);
        List<Region> regions = reader.readRegionFromExcel(fileName);
        List<CropGroup> cropGroups = reader.readCropGroupFromExcel(fileName);
        List<CropClass> cropClasses = reader.readCropClassFromExcel(fileName);
        List<CropSubClass> cropSubClasses = reader.readCropSubClassFromExcel(fileName);
        List<CropVariety> cropVarieties = reader.readCropVarietyFromExcel(fileName);
        List<CropDescription> cropDescriptions = reader.readCropDescriptionsFromExcel(fileName);
        List<CropDescriptionVariety> cropDescVars = reader.readCropDescriptionVarietyFromExcel(fileName);
        List<GrowthScale> growthScales = reader.readGrowthScaleFromExcel(fileName);
        List<GrowthScaleStage> growthScaleStages = reader.readGrowthScaleStageFromExcel(fileName);
        List<CropRegion> cropRegions = reader.readCropRegionsFromExcel(fileName);
        List<Nutrient> nutrients = reader.readNutrientsFromExcel(fileName);
        List<Unit> units = reader.readUnitsFromExcel(fileName);
        List<UnitConversion> unitConversions = reader.readUnitConversionsFromExcel(fileName);
        List<Fertilizer> fertilizers = reader.readFertilizersFromExcel(fileName);
        List<FertilizerRegion> fertilizerRegions = reader.readFertilizerRegionsFromExcel(fileName);

    }
}
