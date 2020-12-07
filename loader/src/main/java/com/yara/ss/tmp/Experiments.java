package com.yara.ss.tmp;

import com.yara.ss.domain.Fertilizer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Experiments {
    public static void main(String[] args) {

        Fertilizer fertilizer = new Fertilizer.Builder(
                "Stas",
                "classNameXx",
                "xxx",
                "nameXx",
                "familyXx",
                "typeXx",
                "lcXx",
                "dm",
                "sl",
                "dens")
                .n("custom_n")
                .nUnitId("custom_n_unit_is")
                .build();

        System.out.println(fertilizer);


//        String str = "";
//        String str2 = str.replaceAll("^$", "xxx");
//        System.out.println(str);
//        System.out.println(str2);
//        String str1 = "Polaris" + "CropGroup" + "bc09457f-cf85-4295-9fa3-9644a1eaf318";
//        String str2 = "NotPolaris" + "CropGroup" + "bc09457f-cf85-4295-9fa3-9644a1eaf318";
//        byte[] arr1 = str1.getBytes(StandardCharsets.UTF_8);
//        byte[] arr2 = str2.getBytes(StandardCharsets.UTF_8);
//        UUID id1 = UUID.nameUUIDFromBytes(arr1);
//        UUID id2 = UUID.nameUUIDFromBytes(arr2);
//
//        System.out.println(id1);
//        System.out.println(id2);
    }
}
