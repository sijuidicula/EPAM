package com.yara.odx.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yara.odx.domain.UseCaseAnswer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class AnswerReader {
    private static final String ANSWERS_FILE = "src/test/resources/UseCasesExpectedAnswers.json";

    public List<UseCaseAnswer> readValidAnswers() {
        ObjectMapper mapper = new ObjectMapper();
        List<UseCaseAnswer> record = null;
        try (FileReader fileReader = new FileReader(ANSWERS_FILE);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine());
            }
            String json = builder.toString();
            TypeReference<List<UseCaseAnswer>> typeRef = new TypeReference<List<UseCaseAnswer>>() {};
            record = mapper.readValue(json, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return record;
    }
}
