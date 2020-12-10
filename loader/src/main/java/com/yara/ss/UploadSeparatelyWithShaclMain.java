package com.yara.ss;

import com.yara.ss.domain.*;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.mapper.CountryMapper;
import com.yara.ss.mapper.RegionMapper;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

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
        String fertilizerRegionFileName = "loader/src/main/resources/FertilizerRegion.xlsx";

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
        List<FertilizerRegion> fertilizerRegions = reader.readFertilizerRegionsFromExcel(fertilizerRegionFileName);

        uploader.uploadShacl(shaclFileName);
        uploader.activateShaclValidationOfTransactions();

        uploader.uploadCountries(countries);
        uploader.uploadRegions(regions, countries);
        uploader.createCountryToRegionRelations(countries, regions);
        uploader.uploadCropGroups(cropGroups);
        uploader.uploadCropClasses(cropClasses, cropGroups);
        uploader.createCropGroupToClassRelations(cropGroups, cropClasses);
        uploader.uploadCropSubClasses(cropSubClasses, cropClasses);
        uploader.createCropClassToSubClassRelations(cropClasses, cropSubClasses);
        uploader.uploadCropVarieties(cropVarieties, cropSubClasses);
        uploader.createCropSubClassToVarietyRelations(cropSubClasses, cropVarieties);
        uploader.uploadCropDescriptions(cropDescriptions, cropSubClasses);
        uploader.createCropSubClassToDescriptionRelations(cropSubClasses, cropDescriptions);
        uploader.createCropVarietyToDescriptionRelations(cropVarieties, cropDescriptions, cropDescVars);
        uploader.uploadGrowthScales(growthScales);
        uploader.uploadGrowthScaleStages(growthScaleStages, growthScales);
        uploader.createGrowthScaleToStagesRelations(growthScales, growthScaleStages);
        uploader.createCropDescriptionsToRegionsRelations(cropDescriptions, regions, cropRegions);
        uploader.createCropDescriptionsToGrowthScaleRelations(cropDescriptions, growthScales, cropRegions);
        uploader.uploadNutrients(nutrients);
        uploader.uploadUnits(units);
        uploader.createNutrientsToUnitsRelations(nutrients, units);
        uploader.uploadUnitConversions(unitConversions, units);
        uploader.createUnitsToConversionsRelations(units, unitConversions);
        uploader.uploadFertilizers(fertilizers, fertilizerRegions, countries, regions);
        uploader.createFertilizersToRegionsRelations(fertilizers, regions, fertilizerRegions);
        uploader.createFertilizersToNutrientsRelations(fertilizers, nutrients, units);

        uploader.close();

        Instant finish = Instant.now();
        long elapsedTimeMillis = Duration.between(start, finish).toMillis();
        long elapsedTimeMinutes = Duration.between(start, finish).toMinutes();
        System.out.println("Total app runtime: " + elapsedTimeMillis + " milliseconds");
        System.out.println("Total app runtime: " + elapsedTimeMillis / 1000 + " seconds");
        System.out.println("Total app runtime: " + elapsedTimeMinutes + " minutes");

//        String resourcesFolder = "loader/src/main/resources/";
//        String fileExtension = ".xlsx";
//        String countryFileName = "Country";

//        List<Country> countries = (List<Country>) reader.readCollectionFromExcel(resourcesFolder, countryFileName, fileExtension, new CountryMapper());
//        List<Region> regions = (List<Region>) reader.readCollectionFromExcel(resourcesFolder, regionFileName, fileExtension, new RegionMapper());
    }
}
