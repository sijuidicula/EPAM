package com.yara.odx.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UseCaseReader {

    private static final String USE_CASES_FILE = "src/test/resources/UseCasesWithRelations.cypher";

    public List<String> readUseCases() {
        List<String> useCases = new ArrayList<>();
        try (FileReader fileReader = new FileReader(USE_CASES_FILE);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.equals("")) {
                    useCases.add(builder.toString());
                    builder.delete(0, builder.length());
                } else {
                    builder.append(line);
                }
            }
            useCases.add(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return useCases;
    }
}
