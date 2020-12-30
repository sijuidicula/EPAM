package com.yara.odx.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yara.odx.domain.OntologyStructure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class OntologyReader {

    private static final String ONTOLOGY_STRUCTURE_FILE_NAME = "validator/src/test/resources/OntologyStructure.json";

    public OntologyStructure getOntologyStructure() {
        ObjectMapper mapper = new ObjectMapper();
        OntologyStructure ontologyStructure = new OntologyStructure();
        try (FileReader reader = new FileReader(ONTOLOGY_STRUCTURE_FILE_NAME);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            while (bufferedReader.ready()) {
                builder.append(bufferedReader.readLine());
            }
            String json = builder.toString();
            ontologyStructure = mapper.readValue(json, OntologyStructure.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ontologyStructure;
    }
}
