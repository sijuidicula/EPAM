package com.yara.ss;

import com.yara.ss.domain.CropClass;
import com.yara.ss.domain.CropGroup;
import com.yara.ss.domain.CropSubClass;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF4J;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.shacl.ShaclSail;
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException;
import org.eclipse.rdf4j.sail.shacl.results.ValidationReport;

import java.io.*;

public class ShaclValidationInJavaMain {
    public static void main(String[] args) {
        // Need to create file in directory
        String shaclFileName = "loader/src/main/resources/test_shacl_for_rdf.ttl";

        CropGroup cereals = new CropGroup(
                "bc09457f-cf85-4295-9fa3-9644a1eaf318",
                "1",
                "",
                "Cereals");

        CropGroup veg = new CropGroup(
                "d79d1e7e-00c4-4aaa-bd6f-a1fe28362a2b",
                "2",
                "",
                "Vegetables_and_melons");

        CropClass wheat = new CropClass(
                "7be740b9-6980-44fb-aeab-ca80423cfdd5",
                "bc09457f-cf85-4295-9fa3-9644a1eaf318",
                "11",
                "",
                "Wheat");

        CropSubClass springWheat = new CropSubClass(
                "862e97f5-4305-4354-b53c-b4b802ed2c25",
                "7be740b9-6980-44fb-aeab-ca80423cfdd5",
                "",
                "",
                "Spring_wheat");

        ModelBuilder builder = new ModelBuilder();
        Model model = builder
                .setNamespace("crop", "http://yara.ontology.crop.com#")
                .setNamespace("rdf", "https://www.w3.org/1999/02/22-rdf-syntax-ns#")
//                .setNamespace("sh", "http://www.w3.org/ns/shacl#")
//                .setNamespace("cropGroup", "http://yara.ontology.crop.com/CropGroup#")
//                .setNamespace("cropClass", "http://yara.ontology.crop.com/CropClass#")
//                .setNamespace("cropSubClass", "http://yara.ontology.crop.com/CropSubClass#")
                .subject("crop:" + cereals.getName())
                .add("rdf:type", "crop:CropGroup")
                .add("crop:cropGroupId", cereals.getId())
                .add("crop:faoId", cereals.getFaoId())
                .add("crop:mediaUri", cereals.getMediaUri())
                .add("crop:cropGroupName", cereals.getName())
                .add("crop:hasCropClass", "crop:" + wheat.getName())
                .subject("crop:" + veg.getName())
                .add("rdf:type", "crop:CropGroup")
                .add("crop:cropGroupId", veg.getId())
                .add("crop:faoId", veg.getFaoId())
                .add("crop:mediaUri", veg.getMediaUri())
                .add("crop:cropGroupName", veg.getName())
                .add("crop:hasCropClass", "crop:" + springWheat.getName())
                .subject("crop:" + wheat.getName())
                .add("rdf:type", "crop:CropClass")
                .add("crop:cropClassId", wheat.getId())
                .add("crop:cropGroupId", wheat.getGroupId())
                .add("crop:faoId", wheat.getFaoId())
                .add("crop:mediaUri", wheat.getMediaUri())
                .add("crop:cropClassName", wheat.getName())
                .subject("crop:" + springWheat.getName())
                .add("rdf:type", "crop:CropSubClass")
                .add("crop:cropSubClassId", springWheat.getId())
                .add("crop:cropClassId", springWheat.getClassId())
                .add("crop:faoId", springWheat.getFaoId())
                .add("crop:mediaUri", springWheat.getMediaUri())
                .add("crop:cropSubClassName", springWheat.getName())
                .build();

        for (Statement st : model) {
            Resource subject = st.getSubject();
            Resource predicate = st.getPredicate();
            Value object = st.getObject();

            System.out.println(subject.toString() + " - " + predicate.toString() + " - " + object.toString());
        }

        ShaclSail shaclSail = new ShaclSail(new MemoryStore());
        Repository repo = new SailRepository(shaclSail);

        try (RepositoryConnection connection = repo.getConnection()) {


            // add shapes
            connection.begin();

            Reader shaclRules = new FileReader(shaclFileName);
//            StringReader shaclRules = new StringReader(
//                    String.join("\n", "",
//                            "@prefix ex: <http://example.com/ns#> .",
//                            "@prefix sh: <http://www.w3.org/ns/shacl#> .",
//                            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
//                            "@prefix foaf: <http://xmlns.com/foaf/0.1/>.",
//
//                            "ex:PersonShape",
//                            "  a sh:NodeShape  ;",
//                            "  sh:targetClass foaf:Person ;",
//                            "  sh:property ex:PersonShapeProperty .",
//
//                            "ex:PersonShapeProperty ",
//                            "  sh:path foaf:age ;",
//                            "  sh:datatype xsd:int ;",
//                            "  sh:maxCount 1 ;",
//                            "  sh:minCount 1 ."
//                    ));

            connection.add(shaclRules, "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH);
            connection.commit();

            connection.begin();
            connection.add(model);

//            StringReader invalidSampleData = new StringReader(
//                    String.join("\n", "",
//                            "@prefix ex: <http://example.com/ns#> .",
//                            "@prefix foaf: <http://xmlns.com/foaf/0.1/>.",
//                            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .",
//
//                            "ex:peter a foaf:Person ;",
//                            "  foaf:age 20, \"30\"^^xsd:int  ."
//
//                    ));
//            ModelBuilder builder2 = new ModelBuilder();
//            Model model2 = builder2
//                    .setNamespace("ex", "http://example.com/ns#")
//                    .setNamespace("foaf", "http://xmlns.com/foaf/0.1/")
//                    .setNamespace("xsd", "http://www.w3.org/2001/XMLSchema#")
//                    .setNamespace("rdf", "https://www.w3.org/1999/02/22-rdf-syntax-ns#")
//                    .subject("ex:peter")
//                    .add("rdf:type", "foaf:Person")
//                    .add("foaf:age", "20, \"30\"^^xsd:int")
//                    .build();
//
//            for (Statement st : model2) {
//                Resource subject = st.getSubject();
//                Resource predicate = st.getPredicate();
//                Value object = st.getObject();

//                System.out.println(subject.toString() + " - " + predicate.toString() + " - " + object.toString());
//            }

//            connection.add(invalidSampleData, "", RDFFormat.TURTLE);
            try {
                connection.commit();
                System.out.println("Validation completed");
            } catch (RepositoryException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof ShaclSailValidationException) {
                    ValidationReport validationReport = ((ShaclSailValidationException) cause).getValidationReport();
                    Model validationReportModel = ((ShaclSailValidationException) cause).validationReportAsModel();
                    // use validationReport or validationReportModel to understand validation violations
                    Rio.write(validationReportModel, System.out, RDFFormat.TURTLE);
                }
                throw exception;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
