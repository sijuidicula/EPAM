package com.yara.odx.tmp;

import com.yara.odx.domain.*;
import com.yara.odx.loader.PropertyGraphUploader;
import com.yara.odx.reader.ExcelWorkbookReader;

import java.util.List;

public class UploadRecordsWithShaclMain {

    public static void main(String[] args) {
        String shaclFileName = "C:/dev/repository/yara/loader/src/main/resources/yara_crop_shacl.ttl";

        String countryFileName = "loader/src/main/resources/Country.xlsx";
        String regionFileName = "loader/src/main/resources/Region.xlsx";
        String cropGroupFileName = "loader/src/main/resources/CropGroup.xlsx";
        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";
        String cropSubClassFileName = "loader/src/main/resources/CropSubClass.xlsx";
        String cropVarietyFileName = "loader/src/main/resources/CropVariety.xlsx";
        String cropDescriptionFileName = "loader/src/main/resources/CropDescription.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        uploader.uploadShacl(shaclFileName);
        uploader.activateShaclValidationOfTransactions();

        List<Country> countries = reader.readCountryFromExcel(countryFileName);
        List<Region> regions = reader.readRegionFromExcel(regionFileName);
        List<CropGroup> cropGroups = reader.readCropGroupFromExcel(cropGroupFileName);
        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);
        List<CropSubClass> cropSubClasses = reader.readCropSubClassFromExcel(cropSubClassFileName);
        List<CropVariety> cropVarieties = reader.readCropVarietyFromExcel(cropVarietyFileName);
        List<CropDescription> cropDescriptions = reader.readCropDescriptionsFromExcel(cropDescriptionFileName);

//        cropClasses.forEach(cropClass -> uploader.uploadCropClassAsRecord(cropClass));

//        TODO
//        uploader.createIncorrectCropClassRecord();

        uploader.close();
    }
}
