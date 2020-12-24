package com.yara.ss.validator;

import com.yara.ss.domain.OntologyStructure;
import com.yara.ss.requestor.Requestor;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class StatisticsValidator {

    public void validateNodesCount(Requestor requestor, Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int classStat = entry.getValue();
            int nodesCount = requestor.getNodesCount(className);

            if (classStat == nodesCount) {
                System.out.println(String.format("Class %s stats are equal to numbers in DB", className));
            } else {
                System.out.println(String.format("Class %s stats NOT equal to numbers in DB", className));
                System.out.println(String.format("%s in stats: %d; in DB: %d", className, classStat, nodesCount));
                System.out.println("****************************************");
            }
        }
    }

    public void validateNonEmptyUuids(Requestor requestor, Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int nodesWithEmptyUuidCount = requestor.getEmptyUuidsCount(className);

            if (nodesWithEmptyUuidCount == 0) {
                System.out.println(String.format("Class %s has no nodes with empty UUIds", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty UUIds", className, nodesWithEmptyUuidCount));
                System.out.println("****************************************");
            }
        }
    }

    public void validateNonEmptyURIs(Requestor requestor, Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int nodesWithEmptyUriCount = requestor.getEmptyUrisCount(className);

            if (nodesWithEmptyUriCount == 0) {
                System.out.println(String.format("Class %s has no nodes with empty URIs", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty URIs", className, nodesWithEmptyUriCount));
                System.out.println("****************************************");
            }
        }
    }

    public void validateNonEmptyLabels(Requestor requestor, Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int nodesWithEmptyLabels = requestor.getEmptyLabelsCount(className);

            if (nodesWithEmptyLabels == 0) {
                System.out.println(String.format("Class %s has no nodes with empty labels", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty labels", className, nodesWithEmptyLabels));
                System.out.println("****************************************");
            }
        }

    }

    public void validateUseCases(Requestor requestor, List<String> useCases) {
        for (int i = 0; i < useCases.size(); i++) {
            String useCase = useCases.get(i);
            validateUseCase(requestor, useCase, i);
        }
    }

    private void validateUseCase(Requestor requestor, String useCase, int index) {
        List<Record> records = requestor.requestUseCase(useCase);

        //here need actually to parse through result and check if required nodes and relations are there
        if (records.isEmpty()) {
            System.out.printf("Result for UseCase # %d is empty\n", ++index);
        } else {
            System.out.printf("Result for UseCase # %d is NOT empty\n", ++index);
        }
    }

    public void validateSchema(Requestor requestor, OntologyStructure ontologyStructure) {
        validateClassNames(requestor, ontologyStructure);
        validateRelationshipNames(requestor, ontologyStructure);
        validateAttributeNames(requestor, ontologyStructure);
    }

    private void validateClassNames(Requestor requestor, OntologyStructure ontologyStructure) {
        List<String> ontologyClassNames = ontologyStructure.getClassNames();
        List<String> databaseClassNames = requestor.getAllClassNames();

//      Node exist in Neo4j as part of Shacl validation configuration
        databaseClassNames.remove("_n10sValidatorConfig");

        validateClassesCount(ontologyClassNames, databaseClassNames);
        validateMissingClasses(ontologyClassNames, databaseClassNames);
        validateExtraClasses(ontologyClassNames, databaseClassNames);
    }

    private void validateClassesCount(List<String> ontologyClassNames, List<String> databaseClassNames) {
        boolean classesCountEquals = ontologyClassNames.size() == databaseClassNames.size();
        if (classesCountEquals) {
            System.out.println("DB matches class names count with Ontology");
        } else {
            System.out.printf("DB DOES NOT match class names count with Ontology. DB has %d and Ontology has %d class names.\n",
                    databaseClassNames.size(), ontologyClassNames.size());
        }
    }

    private void validateExtraClasses(List<String> ontologyClassNames, List<String> databaseClassNames) {
        boolean gotExtraClasses = !ontologyClassNames.containsAll(databaseClassNames);
        if (gotExtraClasses) {
            System.out.println("DB got extra class names in comparison with Ontology. Extra classes from DB which are not in Ontology: ");
            databaseClassNames.stream()
                    .filter(name -> !ontologyClassNames.contains(name))
                    .forEach(System.out::println);
        } else {
            System.out.println("DB has no extra classes in comparison with Ontology");
        }
    }

    private void validateMissingClasses(List<String> ontologyClassNames, List<String> databaseClassNames) {
        boolean containsAllClasses = databaseClassNames.containsAll(ontologyClassNames);
        if (containsAllClasses) {
            System.out.println("All class names from Ontology exist in DB");
        } else {
            System.out.println("Not all class names from Ontology exist in DB. Ontology classes which are not in DB: ");
            ontologyClassNames.stream()
                    .filter(name -> !databaseClassNames.contains(name))
                    .forEach(System.out::println);
        }
    }

    private void validateRelationshipNames(Requestor requestor, OntologyStructure ontologyStructure) {
        List<String> ontologyRelations = ontologyStructure.getRelationshipNames();
        List<String> databaseRelations = requestor.getAllRelationshipNames();

        validateRelationsCount(ontologyRelations, databaseRelations);
        validateMissingRelations(ontologyRelations, databaseRelations);
        validateExtraRelations(ontologyRelations, databaseRelations);
    }

    private void validateRelationsCount(List<String> ontologyRelations, List<String> databaseRelations) {
        boolean relationsCountEquals = ontologyRelations.size() == databaseRelations.size();
        if (relationsCountEquals) {
            System.out.println("DB matches relationship count with Ontology");
        } else {
            System.out.printf("DB DOES NOT match relationship count with Ontology. DB has %d and Ontology has %d relationships.\n",
                    databaseRelations.size(), ontologyRelations.size());
        }
    }

    private void validateMissingRelations(List<String> ontologyRelations, List<String> databaseRelations) {
        boolean containsAllRelations = databaseRelations.containsAll(ontologyRelations);
        if (containsAllRelations) {
            System.out.println("All relationship names from Ontology exist in DB");
        } else {
            System.out.println("Not all relationship names from Ontology exist in DB. Ontology relationships which are not in DB: ");
            ontologyRelations.stream()
                    .filter(name -> !databaseRelations.contains(name))
                    .forEach(System.out::println);
        }
    }

    private void validateExtraRelations(List<String> ontologyRelations, List<String> databaseRelations) {
        boolean gotExtraRelations = !ontologyRelations.containsAll(databaseRelations);
        if (gotExtraRelations) {
            System.out.println("DB got extra relationship names in comparison with Ontology. Extra relationships from DB which are not in Ontology: ");
            databaseRelations.stream()
                    .filter(relation -> !ontologyRelations.contains(relation))
                    .forEach(System.out::println);
        } else {
            System.out.println("DB has no extra relationships in comparison with Ontology");
        }
    }

    private void validateAttributeNames(Requestor requestor, OntologyStructure ontologyStructure) {
        Map<String, List<String>> ontologyAttributesMap = ontologyStructure.getAttributesMap();
        Map<String, List<String>> databaseAttributesMap = requestor.getAllNodesAttributes();

        validateAttributesCount(ontologyAttributesMap, databaseAttributesMap);
        validateMissingAttributes(ontologyAttributesMap, databaseAttributesMap);
        validateExtraAttributes(ontologyAttributesMap, databaseAttributesMap);
    }

    private void validateAttributesCount(Map<String, List<String>> ontologyAttributesMap, Map<String, List<String>> databaseAttributesMap) {
        ontologyAttributesMap.forEach((className, ontologyAttributes) -> {
            List<String> databaseAttributes = databaseAttributesMap.get(className);
            boolean attributesCountEquals = ontologyAttributes.size() == databaseAttributes.size();
            if (attributesCountEquals) {
                System.out.printf("DB matches %s class attributes count with Ontology.\n", className);
            } else {
                System.out.printf("DB DOES NOT match %1$s class attributes count with Ontology. DB %1$s class has %2$d and Ontology has %3$d attributes.\n",
                        className, databaseAttributes.size(), ontologyAttributes.size());
            }
        });


    }

    private void validateExtraAttributes(Map<String, List<String>> ontologyAttributesMap, Map<String, List<String>> databaseAttributesMap) {
        List<String> extraAttributes = new ArrayList<>();
        AtomicBoolean gotExtraAttributes = new AtomicBoolean(false);

        ontologyAttributesMap.forEach((className, ontologyAttributes) -> {
            List<String> databaseAttributes = databaseAttributesMap.get(className);
            boolean gotExtra = !ontologyAttributes.containsAll(databaseAttributes);
            if (gotExtra) {
                gotExtraAttributes.set(true);
                databaseAttributes.stream()
                        .filter(attribute -> !ontologyAttributes.contains(attribute))
                        .forEach(extraAttributes::add);
            }
        });

        if (gotExtraAttributes.get()) {
            System.out.println("DB got extra attribute names in comparison with Ontology. Extra attributes from DB which are not in Ontology: ");
            extraAttributes.forEach(System.out::println);
        } else {
            System.out.println("DB has no extra attributes in comparison with Ontology");
        }
    }

    private void validateMissingAttributes(Map<String, List<String>> ontologyAttributesMap, Map<String, List<String>> databaseAttributesMap) {
        List<String> missingAttributes = new ArrayList<>();
        AtomicBoolean containsAllAttributes = new AtomicBoolean(true);

        ontologyAttributesMap.forEach((className, ontologyAttributes) -> {
            List<String> databaseAttributes = databaseAttributesMap.get(className);
            boolean containsAll = databaseAttributes.containsAll(ontologyAttributes);
            if (!containsAll) {
                containsAllAttributes.set(false);
                ontologyAttributes.stream()
                        .filter(attribute -> !databaseAttributes.contains(attribute))
                        .forEach(missingAttributes::add);
            }
        });

        if (containsAllAttributes.get()) {
            System.out.println("All attribute names from Ontology exist in DB");
        } else {
            System.out.println("Not all attribute names from Ontology exist in DB. Ontology attributes which are not in DB: ");
            missingAttributes.forEach(System.out::println);
        }
    }
}
