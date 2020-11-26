package com.yara.ss.tmp;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Experiments {
    public static void main(String[] args) {
        String str1 = "Polaris" + "CropGroup" + "bc09457f-cf85-4295-9fa3-9644a1eaf318";
        String str2 = "NotPolaris" + "CropGroup" + "bc09457f-cf85-4295-9fa3-9644a1eaf318";
        byte[] arr1 = str1.getBytes(StandardCharsets.UTF_8);
        byte[] arr2 = str2.getBytes(StandardCharsets.UTF_8);
        UUID id1 = UUID.nameUUIDFromBytes(arr1);
        UUID id2 = UUID.nameUUIDFromBytes(arr2);

        System.out.println(id1);
        System.out.println(id2);
    }
}
