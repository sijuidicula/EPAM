package com.yara.odx.tmp;

import com.yara.odx.domain.*;
import com.yara.odx.loader.PropertyGraphUploader;
import com.yara.odx.reader.ExcelWorkbookReader;

import java.util.List;

public class UploadRecordsMain {

    public static void main(String[] args) {
        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);

//        cropClasses.forEach(cc -> System.out.println(cc));
//        System.out.println(cropClasses.size());

//        cropClasses.forEach(cropClass -> uploader.uploadCropClassAsRecord(cropClass));

        uploader.close();
    }
}
