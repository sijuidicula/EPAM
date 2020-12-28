package com.yara.ss.reader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yara.ss.domain.UseCaseAnswer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AnswerReader {
    private static final String ANSWERS_FILE = "validator/src/main/resources/UseCasesExpectedAnswers.json";

    public List<UseCaseAnswer> readValidAnswers() {
//    public HashMap<String, Record> readValidAnswers() {
        ObjectMapper mapper = new ObjectMapper();
        List<UseCaseAnswer> record = null;
//        UseCaseAnswer record = null;
//        HashMap<String, Record> record = null;
        try (FileReader fileReader = new FileReader(ANSWERS_FILE);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine());

            }
            String json = builder.toString();

//            System.out.println(json);

            TypeReference<List<UseCaseAnswer>> typeRef
                    = new TypeReference<List<UseCaseAnswer>>() {
            };

//            record = mapper.readValue(json, UseCaseAnswer.class);
            record = mapper.readValue(json, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return record;
    }
}
