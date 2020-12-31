package com.yara.odx;

import com.yara.odx.domain.OntologyStructure;
import com.yara.odx.reader.OntologyReader;
import com.yara.odx.requestor.Requester;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaTest {

    public static final String SHACL_CONFIG_NODE_NAME = "_n10sValidatorConfig";

    private Requester requester = new Requester();
    private OntologyReader ontologyReader = new OntologyReader();

    private OntologyStructure ontologyStructure = ontologyReader.getOntologyStructure();

    @Test
    void testDatabaseClassNamesCountMatchOntology() {
        List<String> ontologyClassNames = ontologyStructure.getClassNames();
        List<String> databaseClassNames = requester.getAllClassNames();
        databaseClassNames.remove(SHACL_CONFIG_NODE_NAME);

        boolean classNamesCountMatch = ontologyClassNames.size() == databaseClassNames.size();
        if (classNamesCountMatch) {
            System.out.println("DB matches class names count with Ontology");
        } else {
            System.out.printf("DB DOES NOT match class names count with Ontology. DB has %d and Ontology has %d class names.\n",
                    databaseClassNames.size(), ontologyClassNames.size());
            System.out.println("****************************************");
        }

        assertTrue(classNamesCountMatch);
    }

    @Test
    void testDatabaseDoesNotMissClassNamesFromOntology() {
        List<String> ontologyClassNames = ontologyStructure.getClassNames();
        List<String> databaseClassNames = requester.getAllClassNames();
        databaseClassNames.remove("_n10sValidatorConfig");

        boolean containsAllClasses = databaseClassNames.containsAll(ontologyClassNames);
        if (containsAllClasses) {
            System.out.println("All class names from Ontology exist in DB");
        } else {
            System.out.println("Not all class names from Ontology exist in DB. Ontology classes which are not in DB: ");
            ontologyClassNames.stream()
                    .filter(name -> !databaseClassNames.contains(name))
                    .forEach(System.out::println);
            System.out.println("****************************************");
        }

        assertTrue(containsAllClasses);
    }

    @Test
    void testDatabaseHasNoExtraClassNames() {
        List<String> ontologyClassNames = ontologyStructure.getClassNames();
        List<String> databaseClassNames = requester.getAllClassNames();
        databaseClassNames.remove("_n10sValidatorConfig");

        boolean gotExtraClasses = !ontologyClassNames.containsAll(databaseClassNames);
        if (gotExtraClasses) {
            System.out.println("DB got extra class names in comparison with Ontology. Extra classes from DB which are not in Ontology: ");
            databaseClassNames.stream()
                    .filter(name -> !ontologyClassNames.contains(name))
                    .forEach(System.out::println);
            System.out.println("****************************************");
        } else {
            System.out.println("DB has no extra classes in comparison with Ontology");
        }

        assertFalse(gotExtraClasses);
    }

    @Test
    void testDatabaseRelationshipsCountMatchOntology() {
        List<String> ontologyRelations = ontologyStructure.getRelationshipNames();
        List<String> databaseRelations = requester.getAllRelationshipNames();

        boolean relationshipsCountMatch = ontologyRelations.size() == databaseRelations.size();
        if (relationshipsCountMatch) {
            System.out.println("DB matches relationship count with Ontology");
        } else {
            System.out.printf("DB DOES NOT match relationship count with Ontology. DB has %d and Ontology has %d relationships.\n",
                    databaseRelations.size(), ontologyRelations.size());
            System.out.println("****************************************");
        }

        assertTrue(relationshipsCountMatch);
    }

    @Test
    void testDatabaseDoesNotMissRelationshipsFromOntology() {
        List<String> ontologyRelations = ontologyStructure.getRelationshipNames();
        List<String> databaseRelations = requester.getAllRelationshipNames();

        boolean containsAllRelations = databaseRelations.containsAll(ontologyRelations);
        if (containsAllRelations) {
            System.out.println("All relationship names from Ontology exist in DB");
        } else {
            System.out.println("Not all relationship names from Ontology exist in DB. Ontology relationships which are not in DB: ");
            ontologyRelations.stream()
                    .filter(name -> !databaseRelations.contains(name))
                    .forEach(System.out::println);
            System.out.println("****************************************");
        }

        assertTrue(containsAllRelations);
    }

    @Test
    void testDatabaseHasNoExtraRelationships() {
        List<String> ontologyRelations = ontologyStructure.getRelationshipNames();
        List<String> databaseRelations = requester.getAllRelationshipNames();

        boolean gotExtraRelations = !ontologyRelations.containsAll(databaseRelations);
        if (gotExtraRelations) {
            System.out.println("DB got extra relationship names in comparison with Ontology. Extra relationships from DB which are not in Ontology: ");
            databaseRelations.stream()
                    .filter(relation -> !ontologyRelations.contains(relation))
                    .forEach(System.out::println);
            System.out.println("****************************************");
        } else {
            System.out.println("DB has no extra relationships in comparison with Ontology");
        }

        assertFalse(gotExtraRelations);
    }

    @Test
    void testDatabaseAttributesCountMatchOntology() {
        Map<String, List<String>> ontologyAttributesMap = ontologyStructure.getAttributesMap();
        Map<String, List<String>> databaseAttributesMap = requester.getAllNodesAttributes();

        ontologyAttributesMap.forEach((className, ontologyAttributes) -> {
            List<String> databaseAttributes = databaseAttributesMap.get(className);
            boolean attributesCountMatch = ontologyAttributes.size() == databaseAttributes.size();
            if (attributesCountMatch) {
                System.out.printf("DB matches %s class attributes count with Ontology.\n", className);
            } else {
                System.out.printf("DB DOES NOT match %1$s class attributes count with Ontology. DB %1$s class has %2$d and Ontology has %3$d attributes.\n",
                        className, databaseAttributes.size(), ontologyAttributes.size());
                System.out.println("****************************************");
            }

            assertTrue(attributesCountMatch);
        });
    }

    @Test
    void testDatabaseDoesNotMissAttributesFromOntology() {
        Map<String, List<String>> ontologyAttributesMap = ontologyStructure.getAttributesMap();
        Map<String, List<String>> databaseAttributesMap = requester.getAllNodesAttributes();

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
            System.out.println("****************************************");
        }

        assertTrue(containsAllAttributes.get());
    }

    @Test
    void testDatabaseHasNoExtraAttributes() {
        Map<String, List<String>> ontologyAttributesMap = ontologyStructure.getAttributesMap();
        Map<String, List<String>> databaseAttributesMap = requester.getAllNodesAttributes();

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
            System.out.println("****************************************");
        } else {
            System.out.println("DB has no extra attributes in comparison with Ontology");
        }

        assertFalse(gotExtraAttributes.get());
    }
}
