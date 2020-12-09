package com.yara.ss.tmp;

import com.yara.ss.domain.CropClass;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.util.List;

public class UploadRecordsWithOdxNodesMain {

    public static void main(String[] args) {
//        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";
        String cropClassFileName = "loader/src/main/resources/CropClass_v2.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);

//        cropClasses.forEach(cropClass -> uploader.uploadCropClassAsRecord(cropClass));

        uploader.close();
    }
}
