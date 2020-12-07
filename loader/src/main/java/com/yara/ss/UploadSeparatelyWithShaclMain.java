package com.yara.ss;

import com.yara.ss.domain.*;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class UploadSeparatelyWithShaclMain {

    public static void main(String[] args) {
        Instant start = Instant.now();

        String shaclFileName = "C:/dev/repository/yara/loader/src/main/resources/yara_crop_shacl.ttl";

        String countryFileName = "loader/src/main/resources/Country.xlsx";
        String regionFileName = "loader/src/main/resources/Region.xlsx";
        String cropGroupFileName = "loader/src/main/resources/CropGroup.xlsx";
        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";
        String cropSubClassFileName = "loader/src/main/resources/CropSubClass.xlsx";
        String cropVarietyFileName = "loader/src/main/resources/CropVariety.xlsx";
        String cropDescriptionFileName = "loader/src/main/resources/CropDescription.xlsx";
        String cropDescriptionVarietyFileName = "loader/src/main/resources/CropDescriptionVariety.xlsx";
        String growthScaleFileName = "loader/src/main/resources/GrowthScale.xlsx";
        String growthScaleStageFileName = "loader/src/main/resources/GrowthScaleStage.xlsx";
        String cropRegionFileName = "loader/src/main/resources/CropRegion.xlsx";
        String nutrientFileName = "loader/src/main/resources/Nutrient.xlsx";
        String unitsFileName = "loader/src/main/resources/Unit.xlsx";
        String unitConversionsFileName = "loader/src/main/resources/UnitConversion.xlsx";
        String fertilizersFileName = "loader/src/main/resources/Fertilizer.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<Country> countries = reader.readCountryFromExcel(countryFileName);
        List<Region> regions = reader.readRegionFromExcel(regionFileName);
        List<CropGroup> cropGroups = reader.readCropGroupFromExcel(cropGroupFileName);
        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);
        List<CropSubClass> cropSubClasses = reader.readCropSubClassFromExcel(cropSubClassFileName);
        List<CropVariety> cropVarieties = reader.readCropVarietyFromExcel(cropVarietyFileName);
        List<CropDescription> cropDescriptions = reader.readCropDescriptionsFromExcel(cropDescriptionFileName);
        List<CropDescriptionVariety> cropDescVars = reader.readCropDescriptionVarietyFromExcel(cropDescriptionVarietyFileName);
        List<GrowthScale> growthScales = reader.readGrowthScaleFromExcel(growthScaleFileName);
        List<GrowthScaleStage> growthScaleStages = reader.readGrowthScaleStageFromExcel(growthScaleStageFileName);
        List<CropRegion> cropRegions = reader.readCropRegionsFromExcel(cropRegionFileName);
        List<Nutrient> nutrients = reader.readNutrientsFromExcel(nutrientFileName);
        List<Unit> units = reader.readUnitsFromExcel(unitsFileName);
        List<UnitConversion> unitConversions = reader.readUnitConversionsFromExcel(unitConversionsFileName);
        List<Fertilizer> fertilizers = reader.readFertilizersFromExcel(fertilizersFileName);

        uploader.uploadShacl(shaclFileName);
        uploader.activateShaclValidationOfTransactions();

//        uploader.uploadCountries(countries);
//        uploader.uploadRegions(regions);
//        uploader.createCountryToRegionRelations(countries, regions);
//        uploader.uploadCropGroups(cropGroups);
//        uploader.uploadCropClasses(cropClasses);
//        uploader.createCropGroupToClassRelations(cropGroups, cropClasses);
//        uploader.uploadCropSubClasses(cropSubClasses);
//        uploader.createCropClassToSubClassRelations(cropClasses, cropSubClasses);
//        uploader.uploadCropVarieties(cropVarieties);
//        uploader.createCropSubClassToVarietyRelations(cropSubClasses, cropVarieties);
//        uploader.uploadCropDescriptions(cropDescriptions);
//        uploader.createCropSubClassToDescriptionRelations(cropSubClasses, cropDescriptions);
//        uploader.createCropVarietyToDescriptionRelations(cropVarieties, cropDescriptions, cropDescVars);
//        uploader.createDescriptionToRegionRelations(cropDescriptions, regions);
//        uploader.uploadGrowthScales(growthScales);
//        uploader.uploadGrowthScaleStages(growthScaleStages);
//        uploader.createGrowthScaleToStagesRelations(growthScales, growthScaleStages);
//        uploader.createCropDescriptionsToRegionsRelations(cropDescriptions, regions, cropRegions);
//        uploader.createCropDescriptionsToGrowthScaleRelations(cropDescriptions, growthScales, cropRegions);
//        uploader.uploadNutrients(nutrients);
//        uploader.uploadUnits(units);
//        uploader.createNutrientsToUnitsRelations(nutrients, units);
//        uploader.uploadUnitConversions(unitConversions);
//        uploader.createUnitsToConversionsRelations(units, unitConversions);
        uploader.uploadFertilizers(fertilizers);

//        TODO
//        uploader.createFertilizersToNutrientsRelations(fertilizers, nutrients);
//        uploader.createFertilizersToRegionsRelations(fertilizers, regions);


//        TODO
//         uploader.createIncorrectGroupClassRelation();

        uploader.close();

        Instant finish = Instant.now();
        long elapsedTimeMillis = Duration.between(start, finish).toMillis();
        long elapsedTimeMinutes = Duration.between(start, finish).toMinutes();
        System.out.println("Total app runtime: " + elapsedTimeMillis + " milliseconds");
        System.out.println("Total app runtime: " + elapsedTimeMinutes + " minutes");
    }
}
