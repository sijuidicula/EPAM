package com.yara.odx.requestor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requester implements AutoCloseable {

//    private static final String URI = "bolt+s://odx-storage.yara.com:7687";
//    private static final String USER = "neo4j";

//    Use valid pass for graph DB
//    private static final String PASSWORD = "XXXXXXXX";

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "1234";

    private final Driver driver;

    public Requester() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    @Override
    public void close() {
        driver.close();
    }

    public int getNodesCount(String className) {
        int nodesCount = 0;
        String commandFormat = "MATCH (n:%s) RETURN COUNT(n)";
        String command = String.format(commandFormat, className);
        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> getResultAsInteger(command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    private Integer getResultAsInteger(String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        Record record = list.get(0);
        Value value = record.get(0);
        return value.asInt();
    }

    public int getEmptyUuidsCount(String className) {
        int nodesCount = 0;
        String singleItemName = getSingleItemName(className);
        String commandFormat = "MATCH (n:%1$s) " +
                "WHERE n.ODX_%2$s_UUId = \"\" " +
                "OR " +
                "n.ODX_%2$s_UUId IS NULL " +
                "RETURN COUNT(n)";
        String command = String.format(commandFormat, className, singleItemName);

        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> getResultAsInteger(command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    private String getSingleItemName(String className) {
        if (className.endsWith("s") &&
                !className.endsWith("ss") &&
                !className.equals("Units")) {
            int lastIndex = className.lastIndexOf("s");

            return className.substring(0, lastIndex);
        }
        return className;
    }

    public int getEmptyUrisCount(String className) {
        int nodesCount = 0;
        String singleItemName = getSingleItemName(className);
        String commandFormat = "MATCH (n:%1$s) " +
                "WHERE n.ODX_%2$s_Uri = \"\" " +
                "OR " +
                "n.ODX_%2$s_Uri IS NULL " +
                "RETURN COUNT(n)";
        String command = String.format(commandFormat, className, singleItemName);
        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> getResultAsInteger(command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    public int getEmptyLabelsCount(String className) {
        int nodesCount = 0;
        String singleItemName = getSingleItemName(className);

//            Fertilizers name property is called "ProdName" so single item will be "Prod"
        if (singleItemName.equals("Fertilizer")) singleItemName = "Prod";

        String commandFormat = "MATCH (n:%1$s) " +
                "WHERE n.%2$sName = \"\" " +
                "OR " +
                "n.%2$sName IS NULL " +
                "RETURN COUNT(n)";
        String command = String.format(commandFormat, className, singleItemName);

        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> getResultAsInteger(command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    public List<Record> request(String useCase) {
        List<Record> records = new ArrayList<>();
        try (Session session = driver.session()) {
            records = session.readTransaction(tx -> {

//                System.out.println("****************************************");
//                System.out.println(useCase);
//                System.out.println("****************************************");

                Result result = tx.run(useCase);
                List<Record> list = result.list();

//                System.out.println("****************************************");
//                System.out.println(list);
//                System.out.println("****************************************");

                return list;
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return records;
    }

    public List<String> getAllClassNames() {
        List<String> classNames = new ArrayList<>();
        String command = "MATCH (n) RETURN distinct labels(n)";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNamesList(classNames, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return classNames;
    }

    private Object updateNamesList(List<String> classNames, String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        for (Record record : list) {
            Map<String, String> map = record.asMap(v -> String.valueOf(v)
                    .replaceAll("\"", "")
                    .replaceAll("\\[", "")
                    .replaceAll("\\]", ""));
            map.forEach((k, v) -> classNames.add(v));
        }
        return null;
    }

    public List<String> getAllRelationshipNames() {
        List<String> relationshipNames = new ArrayList<>();
        String command = "MATCH (n)-[r]-(m) RETURN DISTINCT type(r)\n";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNamesList(relationshipNames, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationshipNames;
    }

    public Map<String, List<String>> getAllNodesAttributes() {
        Map<String, List<String>> attributeMap = new HashMap<>();
        String command = "MATCH(n) \n" +
                "WITH LABELS(n) AS labels , KEYS(n) AS keys\n" +
                "UNWIND labels AS label\n" +
                "UNWIND keys AS key\n" +
                "RETURN DISTINCT label AS className, COLLECT(DISTINCT key) AS attributes\n" +
                "ORDER BY label\n";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateAttributesMap(attributeMap, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return attributeMap;
    }

    private Object updateAttributesMap(Map<String, List<String>> attributeMap, String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        for (Record record : list) {
            Map<String, Object> map = record.asMap();
            String className = (String) map.get("className");
            List<String> attributes = (List<String>) map.get("attributes");
            attributeMap.put(className, attributes);
        }
        return null;
    }

    public Map<String, String> getRelationNodesMap(String relation) {
        Map<String, String> nodesMap = new HashMap<>();
        String command = String.format("MATCH (s)-[:%s]->(o)\n" +
                "RETURN DISTINCT labels(s) AS subject, labels(o) AS object", relation);
        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNodeNamesMap(nodesMap, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesMap;
    }

    private Object updateNodeNamesMap(Map<String, String> nodesMap, String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        for (Record record : list) {
            Map<String, Object> map = record.asMap();
            List<String> subjectList = (List<String>) map.get("subject");
            String subject = subjectList.get(0);
            List<String> objectList = (List<String>) map.get("object");
            String object = objectList.get(0);
            nodesMap.put(subject, object);
        }
        return null;
    }

    public Map<Node, Node> getIncorrectRelationNodesMap(String subject, String relationship, String object) {
        Map<Node, Node> nodesMap = new HashMap<>();
        String command = String.format(
                "MATCH (subject:%1$s)-[:%2$s]->(object:%3$s)\n" +
                        "WHERE subject.%1$sId <> object.%3$s_%1$sId_Ref\n" +
                        "OR subject.%1$sId <> object.%1$sId_Ref\n" +
                        "OR subject.%1$sId <> object.%4$s_%1$sId_Ref\n" +
                        "RETURN subject, object",
                subject, relationship, object, replaceWithCapital(object));

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNodesMap(nodesMap, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesMap;
    }

    public Map<Node, Node> getCorrectRelationNodesMap(String subject, String relationship, String object) {
        Map<Node, Node> nodesMap = new HashMap<>();
        String command = String.format(
                "MATCH (subject:%1$s)-[:%2$s]->(object:%3$s)\n" +
                        "WHERE subject.%1$sId = object.%3$s_%1$sId_Ref\n" +
                        "OR subject.%1$sId = object.%1$sId_Ref\n" +
                        "OR subject.%1$sId = object.%4$s_%1$sId_Ref\n" +
                        "RETURN subject, object",
                subject, relationship, object, replaceWithCapital(object));

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNodesMap(nodesMap, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesMap;
    }

    private Object updateNodesMap(Map<Node, Node> nodesMap, String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        for (Record record : list) {
            Node subject = record.get("subject").asNode();
            Node object = record.get("object").asNode();
            nodesMap.put(subject, object);
        }
        return null;
    }

    private String replaceWithCapital(String object) {
        StringBuilder builder = new StringBuilder();
        for (String letter : object.split("")) {
            if (Character.isUpperCase(letter.charAt(0))) {
                builder.append(letter);
            }
        }
        return builder.toString();
    }

    public Map<Node, Node> getCorrectRelationNodesMapForUnits(String subject, String relationship, String object) {
        Map<Node, Node> nodesMap = new HashMap<>();
        String command = String.format(
                "MATCH (subject:%1$s)-[:%2$s]->(object:%3$s)\n" +
                        "WHERE subject.UnitsId = object.%3$s_UnitId_Ref\n" +
                        "OR subject.UnitsId = object.UnitId_Ref\n" +
                        "OR subject.UnitsId = object.%4$s_UnitId_Ref\n" +
                        "RETURN subject, object",
                subject, relationship, object, replaceWithCapital(object));

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateNodesMap(nodesMap, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesMap;
    }

    public List<Relationship> getIncorrectVarietyToDescriptionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command = "MATCH (subject:CropVariety)-[relationship:hasCropDescription]->(object:CropDescription)\n" +
                "WHERE relationship.CV_CropDescriptionId_Ref <> object.CropDescriptionId\n" +
                "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    private Object updateRelationsList(List<Relationship> relationsList, String command, Transaction tx) {
        Result result = tx.run(command);
        List<Record> list = result.list();
        for (Record record : list) {
            if (record.get("relationship").isNull()) continue;

//            System.out.println(record.get("relationship"));

            Relationship relationship = record.get("relationship").asRelationship();
            relationsList.add(relationship);
        }
        return null;
    }

    public List<Relationship> getCorrectVarietyToDescriptionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command = "MATCH (subject:CropVariety)-[relationship:hasCropDescription]->(object:CropDescription)\n" +
                "WHERE relationship.CV_CropDescriptionId_Ref = object.CropDescriptionId\n" +
                "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    public List<Relationship> getIncorrectDescriptionToRegionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command =
                "MATCH (country:Country)-[:hasRegion]->(region:Region)" +
                        "<-[relationship:isAvailableIn]-(subject:CropDescription)\n" +
                        "WHERE relationship.CD_CountryIdRef <> country.CountryId\n" +
                        "OR relationship.CD_RegionIdRef <> region.RegionId\n" +
                        "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    public List<Relationship> getCorrectDescriptionToRegionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command =
                "MATCH (country:Country)-[:hasRegion]->(region:Region)<-[relationship:isAvailableIn]-" +
                        "(subject:CropDescription)-[:hasGrowthScale]->(object:GrowthScale)\n" +
                        "WHERE relationship.CD_CountryIdRef = country.CountryId\n" +
                        "OR relationship.CD_GrowthScaleId_Ref = object.GrowthScaleId\n" +
                        "OR relationship.CD_RegionIdRef = region.RegionId\n" +
                        "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    public List<Relationship> getIncorrectFertilizersToRegionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command =
                "MATCH (country:Country)-[:hasRegion]->(region:Region)" +
                        "<-[relationship:isAvailableIn]-(fertilizer:Fertilizers)\n" +
                        "WHERE relationship.Prod_CountryId_Ref <> country.CountryId\n" +
                        "OR relationship.Prod_RegionIdRef <> region.RegionId\n" +
                        "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    public List<Relationship> getCorrectFertilizersToRegionRelationsList() {
        List<Relationship> relationsList = new ArrayList<>();
        String command =
                "MATCH (country:Country)-[:hasRegion]->(region:Region)" +
                        "<-[relationship:isAvailableIn]-(fertilizer:Fertilizers)\n" +
                        "WHERE relationship.Prod_CountryId_Ref = country.CountryId\n" +
                        "OR relationship.Prod_RegionIdRef = region.RegionId\n" +
                        "RETURN relationship";

//        System.out.println(command);

        try (Session session = driver.session()) {
            session.readTransaction(tx -> updateRelationsList(relationsList, command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return relationsList;
    }

    public int getEmptyLabelsCountForGss() {
        int nodesCount = 0;

        String command = "MATCH (n:GrowthScaleStages) " +
                "WHERE n.GrowthScaleStageDescription = \"\" " +
                "OR " +
                "n.GrowthScaleStageDescription IS NULL " +
                "RETURN COUNT(n)";

        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> getResultAsInteger(command, tx));
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;


    }
}
