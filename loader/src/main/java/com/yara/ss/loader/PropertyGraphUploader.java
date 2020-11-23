package com.yara.ss.loader;

import com.yara.ss.domain.*;
import com.yara.ss.reader.ExcelWorkbookReader;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyGraphUploader implements AutoCloseable {

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "1234";

    private final Driver driver;

    public PropertyGraphUploader() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void uploadConcept(final String fileName) {
        try (Session session = driver.session()) {
            session.run("CALL n10s.rdf.import.fetch(\"file:///home/" + fileName + "\", \"Turtle\");");
        }
    }

    public void uploadCountries(List<Country> countries) {
        String commandFormat = "CREATE (%s:Country{name: \"%s\", id: \"%s\"})";
        try (Session session = driver.session()) {
            countries.forEach(country -> session.writeTransaction(tx -> tx.run(String.format(commandFormat,
                    createNewName(country.getName()), country.getName(), country.getId()))));
        }
        System.out.println("Country uploading completed");
    }

    public void uploadRegions(List<Region> regions) {
        String createRegion = "CREATE (%s:Region{id: \"%s\", countryId: \"%s\", name: \"%s\"})\n";
        try (Session session = driver.session()) {
            regions.forEach(region -> session.writeTransaction(tx -> {
                String newRegionName = createNewName(region.getName());
                return tx.run(String.format(createRegion,
                        newRegionName, region.getId(), region.getCountryId(), region.getName()));
            }));
        }
        System.out.println("Region uploading completed");
    }

    public void uploadCropGroups(List<CropGroup> cropGroups) {
        String createGroup = "CREATE (%s:CropGroup{id: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropGroups.forEach(group -> session.writeTransaction(tx -> {
                String newGroupName = createNewName(group.getName());
                return tx.run(String.format(createGroup,
                        newGroupName, group.getId(), group.getFaoId(), group.getMediaUri(), group.getName()));
            }));
        }
        System.out.println("CropGroup uploading completed");
    }

    public void uploadCropClasses(List<CropClass> cropClasses) {
        String createClass = "CREATE (%s:CropClass{id: \"%s\", groupId: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropClasses.forEach(clazz -> session.writeTransaction(tx -> {
                String newClassName = createNewName(clazz.getName());
                return tx.run(String.format(createClass,
                        newClassName, clazz.getId(), clazz.getGroupId(), clazz.getFaoId(), clazz.getMediaUri(), clazz.getName()));
            }));
        }
        System.out.println("CropClass uploading completed");
    }

    public void uploadCropSubClasses(List<CropSubClass> cropSubClasses) {
        String createSubClass = "CREATE (%s:CropSubClass{id: \"%s\", classId: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropSubClasses.forEach(subClass -> session.writeTransaction(tx -> {
                String newSubClassName = createNewName(subClass.getName());
                return tx.run(String.format(createSubClass,
                        newSubClassName, subClass.getId(), subClass.getClassId(), subClass.getFaoId(), subClass.getMediaUri(), subClass.getName()));
            }));
        }
        System.out.println("CropSubClass uploading completed");
    }

    public void uploadCropVarieties(List<CropVariety> cropVarieties) {
        String createVariety = "CREATE (%s:CropVariety{id: \"%s\", subClassId: \"%s\", name: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropVarieties.forEach(variety -> session.writeTransaction(tx -> {
                String newVarietyName = createNewName(variety.getName());
                return tx.run(String.format(createVariety,
                        newVarietyName, variety.getId(), variety.getSubClassId(), variety.getName()));
            }));
        }
        System.out.println("CropVariety uploading completed");
    }

    public void createCropClassToSubClassRelations(List<CropClass> ancestors, List<CropSubClass> children) {
        Map<CropClass, List<CropSubClass>> map = getAncestorSubClassMap(ancestors, children);
        for (Map.Entry<CropClass, List<CropSubClass>> entry : map.entrySet()) {
            CropClass ancestor = entry.getKey();
            List<CropSubClass> relatedChildren = entry.getValue();
            relatedChildren.forEach(child -> createClassSubClassRelation(ancestor, child));
        }
        System.out.println("Class-SubClass relation uploading completed");
    }

    public void createCountryToRegionRelations(List<Country> countries, List<Region> regions) {
        Map<Country, List<Region>> map = getCountryRegionMap(countries, regions);
        for (Map.Entry<Country, List<Region>> entry : map.entrySet()) {
            Country country = entry.getKey();
            List<Region> countryRegions = entry.getValue();
            countryRegions.forEach(region -> createCountryRegionRelation(country, region));
        }
        System.out.println("Country-Region relation uploading completed");
    }

    public void createCropGroupToClassRelations(List<CropGroup> groups, List<CropClass> classes) {
        Map<CropGroup, List<CropClass>> map = getCropGroupClassMap(groups, classes);
        for (Map.Entry<CropGroup, List<CropClass>> entry : map.entrySet()) {
            CropGroup group = entry.getKey();
            List<CropClass> groupClasses = entry.getValue();
            groupClasses.forEach(clazz -> createGroupClassRelation(group, clazz));
        }
        System.out.println("Group-Class relation uploading completed");
    }

    public void createCropSubClassToVarietyRelations(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        Map<CropSubClass, List<CropVariety>> map = getSubClassesVarietiesMap(subClasses, varieties);
        for (Map.Entry<CropSubClass, List<CropVariety>> entry : map.entrySet()) {
            CropSubClass subClass = entry.getKey();
            List<CropVariety> relatedVarieties = entry.getValue();
            relatedVarieties.forEach(variety -> createSubClassVarietyRelation(subClass, variety));
        }
        System.out.println("CropSubClass-CropVariety relation uploading completed");

    }

    public void uploadCountriesFromCsvByNeo4j(String countryCsvFileName) {
        String commandFormat = "LOAD CSV WITH HEADERS FROM  \"file:///%s\" AS country\n" +
                "\n" +
                "CREATE (p:Resource :NamedIndividual :Country\n" +
                "{\n" +
                "CountryId: country.CountryId,\n" +
                "CountryName: country.CountryName,\n" +
                "ProductSetCode: country.ProductSetCode,\n" +
                "rdfs__label: country.CountryName\n" +
                "})";
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(String.format(commandFormat, countryCsvFileName)));
        }
        System.out.println("Country uploading completed");
    }

    public void uploadShacl(String shaclFileName) {
        String commandFormat = "CALL n10s.validation.shacl.import.fetch" +
                "(\"file:///%s\",\"Turtle\")";
        try (Session session = driver.session()) {
            session.run(String.format(commandFormat, shaclFileName));
        }
        System.out.println("Shacl uploading completed");
    }

    public void createIncorrectCropSubClassRelation() {
        String commandFormat = "MATCH (rice:CropSubClass{name: 'Rice'})" +
                "MATCH (maize:CropSubClass{name: 'Maize'})" +
                "CREATE (rice)-[r:HAS_CROP_VARIETY]->(maize)";
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(commandFormat));
            System.out.println("Creating incorrect relation completed");
        } catch (ClientException e) {
            System.out.println("Creation of incorrect relation is impossible");
            System.out.println(e.getMessage());
        }
    }

    public void uploadCropClassAsRecord(CropClass cropClass) {
        if (cropClassExistInDatabase(cropClass.getId())) {
            return;
        }

        String createClassFormat = "CREATE (%s:CropClass{id: \"%s\", groupId: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
        String newClassName = createNewName(cropClass.getName());
        String createClassCommand = String.format(createClassFormat,
                newClassName, cropClass.getId(), cropClass.getGroupId(), cropClass.getFaoId(), cropClass.getMediaUri(), cropClass.getName());
        StringBuilder builder = new StringBuilder().append(createClassCommand);
        CropGroup group = createGroupFromExcel(cropClass.getGroupId());
        String newGroupName = createNewName(group.getName());

        if (!cropGroupExistInDatabase(group.getId())) {
            String createCropGroupCommand = composeCreateCropGroupCommand(group);
            builder.append(createCropGroupCommand);
        } else {
            String groupMatchFormat = "MATCH (%s:cropGroup{id: \"%s\"} RETURN %s)";
            builder.append(String.format(groupMatchFormat, newGroupName, group.getId(), newGroupName));
        }
        String createRelationFormat = "CREATE (%s)-[:HAS_CROP_CLASS]->(%s)";
        builder.append(String.format(createRelationFormat, newGroupName, newClassName));

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
        System.out.println("CropClass uploading completed");
    }

    public void activateShaclValidationOfTransactions() {
        String commandFormat = "CALL apoc.trigger.add('shacl-validate'," +
                "'call n10s.validation.shacl.validateTransaction($createdNodes,$createdRelationships, $assignedLabels, " +
                "$removedLabels, $assignedNodeProperties, $removedNodeProperties)', {phase:'before'})";
        try (Session session = driver.session()) {
            session.run(commandFormat);
        }
        System.out.println("Shacl validation for transactions activated");


    }

    public void uploadAnotherCropGroup() {
        String createGroup = "CREATE (cg:CropGroup{id: \"xxx\", faoId: \"xxx\", mediaUri: \"xxx\", name: \"xxx\"})\n";
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(createGroup));
        }
        System.out.println("Creation of XXX CropGroup is completed");
    }

    public void createIncorrectCropSubClassRelation2() {
        String commandFormat = "MATCH (cg1:CropGroup{name: 'Other crops'})" +
                "MATCH (cg2:CropGroup{name: 'xxx'})" +
                "CREATE (cg1)-[r:HAS_CROP_CLASS]->(cg2)";
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(commandFormat));
            System.out.println("Creating incorrect CropGroup relation completed");
        } catch (ClientException e) {
            System.out.println("Creation of incorrect CropGroup relation is impossible");
            System.out.println(e.getMessage());
        }
    }

    private void createCountryRegionRelation(Country country, Region region) {

        String matchCountry = String.format("MATCH (country:Country{id:\"%s\"})\n", country.getId());
        String matchRegion = String.format("MATCH (region:Region{id:\"%s\"})\n", region.getId());
        String createRelation = "CREATE (country)-[:HAS_REGION]->(region)";

        StringBuilder builder = new StringBuilder();
        builder.append(matchCountry).append(matchRegion).append(createRelation);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private void createGroupClassRelation(CropGroup group, CropClass clazz) {
        String matchGroup = String.format("MATCH (group:CropGroup{id:\"%s\"})\n", group.getId());
        String matchClass = String.format("MATCH (class:CropClass{id:\"%s\"})\n", clazz.getId());
        String createRelation = "CREATE (group)-[:HAS_CROP_CLASS]->(class)";

        StringBuilder builder = new StringBuilder();
        builder.append(matchGroup).append(matchClass).append(createRelation);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private void createClassSubClassRelation(CropClass ancestor, CropSubClass child) {
        String matchGroup = String.format("MATCH (ancestor:CropClass{id:\"%s\"})\n", ancestor.getId());
        String matchClass = String.format("MATCH (child:CropSubClass{id:\"%s\"})\n", child.getId());
        String createRelation = "CREATE (ancestor)-[:HAS_CROP_SUB_CLASS]->(child)";

        StringBuilder builder = new StringBuilder();
        builder.append(matchGroup).append(matchClass).append(createRelation);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private void createSubClassVarietyRelation(CropSubClass subClass, CropVariety variety) {
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{id:\"%s\"})\n", subClass.getId());
        String matchVariety = String.format("MATCH (variety:CropVariety{id:\"%s\"})\n", variety.getId());
        String createRelation = "CREATE (subClass)-[:HAS_CROP_VARIETY]->(variety)";

        StringBuilder builder = new StringBuilder();
        builder.append(matchSubClass).append(matchVariety).append(createRelation);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private Map<CropSubClass, List<CropVariety>> getSubClassesVarietiesMap(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        Map<CropSubClass, List<CropVariety>> map = new HashMap();
        varieties.forEach(variety -> {
            CropSubClass subClass = getSubClassById(subClasses, variety.getSubClassId());
            List<CropVariety> relatedVarieties = new ArrayList<>();
            if (map.containsKey(subClass)) {
                relatedVarieties = map.get(subClass);
            }
            relatedVarieties.add(variety);
            map.put(subClass, relatedVarieties);
        });
        return map;
    }

    private Map<Country, List<Region>> getCountryRegionMap(List<Country> countries, List<Region> regions) {
        Map<Country, List<Region>> map = new HashMap();
        regions.forEach(r -> {
            Country country = getCountryById(countries, r.getCountryId());
            List<Region> countryRegions = new ArrayList<>();
            if (map.containsKey(country)) {
                countryRegions = map.get(country);
            }
            countryRegions.add(r);
            map.put(country, countryRegions);
        });
        return map;
    }

    private Map<CropGroup, List<CropClass>> getCropGroupClassMap(List<CropGroup> groups, List<CropClass> classes) {
        Map<CropGroup, List<CropClass>> map = new HashMap();
        classes.forEach(cl -> {
            CropGroup group = getGroupById(groups, cl.getGroupId());
            List<CropClass> groupClasses = new ArrayList<>();
            if (map.containsKey(group)) {
                groupClasses = map.get(group);
            }
            groupClasses.add(cl);
            map.put(group, groupClasses);
        });
        return map;
    }

    private Map<CropClass, List<CropSubClass>> getAncestorSubClassMap(List<CropClass> ancestors, List<CropSubClass> subClasses) {
        Map<CropClass, List<CropSubClass>> map = new HashMap();
        subClasses.forEach(scl -> {
            CropClass ancestor = getAncestorById(ancestors, scl.getClassId());
            List<CropSubClass> relatedChildren = new ArrayList<>();
            if (map.containsKey(ancestor)) {
                relatedChildren = map.get(ancestor);
            }
            relatedChildren.add(scl);
            map.put(ancestor, relatedChildren);
        });
        return map;
    }

    private Country getCountryById(List<Country> countries, String id) {
        return countries.stream()
                .filter(country -> country.getId().equals(id))
                .findFirst()
                .orElse(new Country("xxx", "xxx"));
    }

    private CropGroup getGroupById(List<CropGroup> groups, String id) {
        return groups.stream()
                .filter(group -> group.getId().equals(id))
                .findFirst()
                .orElse(new CropGroup("xxx", "xxx", "xxx", "xxx"));
    }

    private CropClass getAncestorById(List<CropClass> classes, String id) {
        return classes.stream()
                .filter(cl -> cl.getId().equals(id))
                .findFirst()
                .orElse(new CropClass("xxx", "xxx", "xxx", "xxx", "xxx"));
    }

    private CropSubClass getSubClassById(List<CropSubClass> subClasses, String id) {
        return subClasses.stream()
                .filter(scl -> scl.getId().equals(id))
                .findFirst()
                .orElse(new CropSubClass("xxx", "xxx", "xxx", "xxx", "xxx"));
    }

    private String createNewName(String oldName) {
        return oldName.replace("[^A-Za-z0-9]", "_")
                .replace(" ", "_")
                .replace("-", "_")
                .replace(",", "_")
                .replace("'", "_")
                .replace("/", "_")
                .replace("(", "_")
                .replace(")", "_");
    }

    private boolean cropClassExistInDatabase(String classId) {
        boolean answer;
        String existCheckQueryFormat = "MATCH (cc:CropClass{id: \"%s\"})\n" +
                "RETURN count(cc)>0";
        String existCheckQuery = String.format(existCheckQueryFormat, classId);

        try (Session session = driver.session()) {
            answer = session.readTransaction(tx -> {
                List<Record> records = tx.run(existCheckQuery).list();
                return records.get(0).get(0).asBoolean();
            });
        }
        System.out.println("CropClass exists in DB: " + answer);
        return answer;
    }

    private boolean cropGroupExistInDatabase(String groupId) {
        boolean answer;
        String existCheckQueryFormat = "MATCH (cg:CropGroup{id: \"%s\"})\n" +
                "RETURN count(cg)>0";
        String existCheckQuery = String.format(existCheckQueryFormat, groupId);

        try (Session session = driver.session()) {
            answer = session.readTransaction(tx -> {
                List<Record> records = tx.run(existCheckQuery).list();
                return records.get(0).get(0).asBoolean();
            });
        }
        System.out.println("CropGroup exists in DB: " + answer);
        return answer;
    }

    private String composeCreateCropGroupCommand(CropGroup group) {
        String createGroup = "CREATE (%s:CropGroup{id: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
        String newGroupName = createNewName(group.getName());
        return String.format(createGroup, newGroupName, group.getId(), group.getFaoId(), group.getMediaUri(), group.getName());
    }

    private CropGroup createGroupFromExcel(String groupId) {
        //Need to pass this variable from outside
        String cropGroupFileName = "loader/src/main/resources/CropGroup.xlsx";

        //May be need to pass this from outside as well
        ExcelWorkbookReader excelWorkbookReader = new ExcelWorkbookReader();

        List<CropGroup> cropGroups = excelWorkbookReader.readCropGroupFromExcel(cropGroupFileName);

        return cropGroups.stream()
                .filter(group -> group.getId().equals(groupId))
                .findAny()
                .orElse(new CropGroup("dummy_group", "dummy_group", "dummy_group", "dummy_group"));
    }
}

