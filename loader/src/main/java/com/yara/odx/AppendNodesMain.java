package com.yara.odx;

import com.yara.odx.domain.*;
import com.yara.odx.loader.PropertyGraphUploader;
import com.yara.odx.reader.ExcelWorkbookReader;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class AppendNodesMain {

    public static void main(String[] args) {
        Instant start = Instant.now();

//        Input your Uri, login and password as program arguments
        String uri = args[0];
        String login = args[1];
        String password = args[2];

        String countryFileName = "loader/src/main/resources/Country_new1.xlsx";
        String regionFileName = "loader/src/main/resources/Region_new1.xlsx";
        String cropGroupFileName = "loader/src/main/resources/CropGroup_new1.xlsx";
        String cropClassFileName = "loader/src/main/resources/CropClass_new1.xlsx";
        String cropSubClassFileName = "loader/src/main/resources/CropSubClass_new1.xlsx";
        String cropVarietyFileName = "loader/src/main/resources/CropVariety_new1.xlsx";
        String cropDescriptionFileName = "loader/src/main/resources/CropDescription_new1.xlsx";
        String cropDescriptionVarietyFileName = "loader/src/main/resources/CropDescriptionVariety_new1.xlsx";
        String growthScaleFileName = "loader/src/main/resources/GrowthScale_new1.xlsx";
        String growthScaleStageFileName = "loader/src/main/resources/GrowthScaleStages_new1.xlsx";
        String cropRegionFileName = "loader/src/main/resources/CropRegion_new1.xlsx";
        String nutrientFileName = "loader/src/main/resources/Nutrient_new1.xlsx";
        String unitsFileName = "loader/src/main/resources/Units_new1.xlsx";
        String unitConversionsFileName = "loader/src/main/resources/UnitConversion_new1.xlsx";
        String fertilizersFileName = "loader/src/main/resources/Fertilizers_new1.xlsx";
        String fertilizerRegionFileName = "loader/src/main/resources/Fertilizers_Reg_new1.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader(uri, login, password);

        List<Country> countries = reader.readCountryFromExcel(countryFileName);
        List<Region> regions = reader.readRegionFromExcel(regionFileName);
        List<CropGroup> cropGroups = reader.readCropGroupFromExcel(cropGroupFileName);
        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);
        List<CropSubClass> cropSubClasses = reader.readCropSubClassFromExcel(cropSubClassFileName);
        List<CropVariety> cropVarieties = reader.readCropVarietyFromExcel(cropVarietyFileName);
        List<CropDescription> cropDescriptions = reader.readCropDescriptionsFromExcel(cropDescriptionFileName);
        List<CropDescriptionVariety> cropDescVars = reader.readCropDescriptionVarietyFromExcel(cropDescriptionVarietyFileName);
        List<GrowthScale> growthScales = reader.readGrowthScaleFromExcel(growthScaleFileName);
        List<GrowthScaleStages> growthScaleStages = reader.readGrowthScaleStageFromExcel(growthScaleStageFileName);
        List<CropRegion> cropRegions = reader.readCropRegionsFromExcel(cropRegionFileName);
        List<Nutrient> nutrients = reader.readNutrientsFromExcel(nutrientFileName);
        List<Units> units = reader.readUnitsFromExcel(unitsFileName);
        List<UnitConversion> unitConversions = reader.readUnitConversionsFromExcel(unitConversionsFileName);
        List<Fertilizers> fertilizers = reader.readFertilizersFromExcel(fertilizersFileName);
        List<FertilizerRegion> fertilizerRegions = reader.readFertilizerRegionsFromExcel(fertilizerRegionFileName);

        uploader.mergeCountries(countries);
        uploader.mergeRegions(regions);
        uploader.mergeCropGroups(cropGroups);
        uploader.mergeCropClasses(cropClasses);
        uploader.mergeCropSubClasses(cropSubClasses);
        uploader.mergeCropVarieties(cropVarieties);
        uploader.mergeGrowthScales(growthScales);
        uploader.mergeCropDescriptions(cropDescriptions, cropRegions);
        uploader.mergeGrowthScaleStages(growthScaleStages);
        uploader.mergeNutrients(nutrients);
        uploader.mergeUnits(units);
        uploader.mergeUnitConversions(unitConversions);
        uploader.mergeFertilizers(fertilizers);

        uploader.mergeCountryToRegionRelations(regions);
        uploader.mergeCropGroupToClassRelations(cropClasses);
        uploader.mergeCropClassToSubClassRelations(cropSubClasses);
        uploader.mergeCropSubClassToVarietyRelations(cropVarieties);
        uploader.mergeCropSubClassToDescriptionRelations(cropDescriptions);
        uploader.mergeCropVarietyToDescriptionRelations(cropDescVars);
        uploader.mergeGrowthScaleToStagesRelations(growthScaleStages);
        uploader.mergeCropDescriptionsToRegionsRelations(cropRegions);
        uploader.mergeCropDescriptionsToGrowthScaleRelations(cropRegions);
        uploader.mergeNutrientsToUnitsRelations(nutrients);
        uploader.mergeUnitsToConversionsRelations(unitConversions);
        uploader.mergeFertilizersToRegionsRelations(fertilizerRegions);
        uploader.mergeFertilizersToNutrientsRelations(fertilizers, nutrients, units);

        uploader.close();

        Instant finish = Instant.now();
        long elapsedTimeMillis = Duration.between(start, finish).toMillis();
        long elapsedTimeMinutes = Duration.between(start, finish).toMinutes();
        System.out.println("Total app runtime: " + elapsedTimeMillis + " milliseconds");
        System.out.println("Total app runtime: " + elapsedTimeMillis / 1000 + " seconds");
        System.out.println("Total app runtime: " + elapsedTimeMinutes + " minutes");
    }
}
