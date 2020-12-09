package com.yara.ss.tmp;

import com.yara.ss.domain.*;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.util.List;

public class UploadRecordsMain {

    public static void main(String[] args) {
//        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";
//        String cropClassFileName = "loader/src/main/resources/CropClass_v2.xlsx";
//        String cropClassFileName = "loader/src/main/resources/CropClass_v3.xlsx";
        String cropClassFileName = "loader/src/main/resources/CropClass_v4.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);

//        cropClasses.forEach(cc -> System.out.println(cc));
//        System.out.println(cropClasses.size());

//        cropClasses.forEach(cropClass -> uploader.uploadCropClassAsRecord(cropClass));

        uploader.close();
    }
}
