package com.yara.ss.tmp;

import com.yara.ss.domain.CropClass;
import com.yara.ss.loader.PropertyGraphUploader;

public class ShaclTestMain {

    public static void main(String[] args) {

        // Need to create file in directory
        String shaclFileName = "loader/src/main/resources/test_shacl.ttl";

        CropClass cropClass = new CropClass(
                "Polaris",
                "CropClass",
                "test_crop_class",
                "9dff9769-dc60-4198-8541-420aab267d04",
                "test_crop_class",
                "test_crop_class",
                "test_crop_class");

        PropertyGraphUploader uploader = new PropertyGraphUploader();

        uploader.uploadShacl(shaclFileName);
        uploader.activateShaclValidationOfTransactions();

//        uploader.uploadCropClassAsRecord(cropClass);
//        uploader.uploadAnotherCropGroup();
//        uploader.createIncorrectCropSubClassRelation2();

        uploader.close();
    }
}
