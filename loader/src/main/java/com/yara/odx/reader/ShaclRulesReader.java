package com.yara.odx.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShaclRulesReader {

    public String readShaclRules(String fileName) {
        String shaclRules = "";
        try (FileReader reader = new FileReader(fileName);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine()).append("\n");
            }
            shaclRules = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shaclRules;
    }
}
