package com.yara.odx.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yara.odx.collector.StatisticsCollector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class StatisticsReporter {

    private static final String STATISTICS_FILE_NAME = "resources/statistics.txt";

    public void createStatisticsFile() {
        try (FileWriter fileWriter = new FileWriter(STATISTICS_FILE_NAME)) {
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeStatisticsToFile(List<?> list) {
        String collectionGenericType = list.get(0).getClass().getSimpleName();
        int collectionSize = list.size();
        String lineFormat = "%s,%d\n";
        try (FileWriter fileWriter = new FileWriter(STATISTICS_FILE_NAME, true)) {
            String line = String.format(lineFormat, collectionGenericType, collectionSize);
            fileWriter.write(line);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeStatisticsToFileAsJson(StatisticsCollector collector) {
        try (FileWriter fileWriter = new FileWriter(STATISTICS_FILE_NAME, true)) {
            String json = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(collector.getStatistics());
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
