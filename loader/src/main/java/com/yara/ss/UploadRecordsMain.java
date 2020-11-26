package com.yara.ss;

import com.yara.ss.domain.*;
import com.yara.ss.loader.PropertyGraphUploader;
import com.yara.ss.reader.ExcelWorkbookReader;

import java.util.List;

public class UploadRecordsMain {

    public static void main(String[] args) {
        String cropClassFileName = "loader/src/main/resources/CropClass.xlsx";

        ExcelWorkbookReader reader = new ExcelWorkbookReader();
        PropertyGraphUploader uploader = new PropertyGraphUploader();

        List<CropClass> cropClasses = reader.readCropClassFromExcel(cropClassFileName);

        cropClasses.forEach(cropClass -> uploader.uploadCropClassAsRecord(cropClass));

        uploader.close();
    }
}
