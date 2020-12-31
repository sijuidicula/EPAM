package com.yara.odx.reader;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatisticsReader {

    private static final String STATISTICS_FILE_NAME = "src/test/resources/statistics.txt";

    public Map<String, Integer> getStatistics() {
        ObjectMapper mapper = new ObjectMapper();
        Map statistics = new HashMap<>();
        try (FileReader reader = new FileReader(STATISTICS_FILE_NAME);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine());
            }
            String json = builder.toString();
            statistics = mapper.readValue(json, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return statistics;
    }

}
