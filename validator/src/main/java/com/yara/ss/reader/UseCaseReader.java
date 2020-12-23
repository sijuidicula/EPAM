package com.yara.ss.reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UseCaseReader {

    private static final String USE_CASES_FILE = "validator/src/main/resources/UseCases.cypher";

    private List<String> useCases = new ArrayList<>();

    public void readUseCases() {
        try (FileReader fileReader = new FileReader(USE_CASES_FILE);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.equals("")) {
                    useCases.add(builder.toString());
                    builder.delete(0, builder.length());
                    continue;
                } else {
                    builder.append(line);
                }
            }
            useCases.add(builder.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getUseCases() {
        return useCases;
    }
}
