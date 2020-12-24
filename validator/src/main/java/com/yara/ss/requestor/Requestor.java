package com.yara.ss.requestor;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requestor implements AutoCloseable {

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "1234";

    private final Driver driver;

    public Requestor() {
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
            nodesCount = session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                Record record = list.get(0);
                Value value = record.get(0);
                return value.asInt();
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
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

//        System.out.println(command);

        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                Record record = list.get(0);
                Value value = record.get(0);
                return value.asInt();
            });
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
            nodesCount = session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                Record record = list.get(0);
                Value value = record.get(0);
                return value.asInt();
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    public int getEmptyLabelsCount(String className) {
        int nodesCount = 0;
        String singleItemName = getSingleItemName(className);
        String commandFormat = "MATCH (n:%1$s) " +
                "WHERE n.%2$sName = \"\" " +
                "OR " +
                "n.%2$sName IS NULL " +
                "RETURN COUNT(n)";
        String command = String.format(commandFormat, className, singleItemName);

        System.out.println(command);

        try (Session session = driver.session()) {
            nodesCount = session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                Record record = list.get(0);
                Value value = record.get(0);
                return value.asInt();
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return nodesCount;
    }

    public List<Record> requestUseCase(String useCase) {
        List<Record> records = new ArrayList<>();
        try (Session session = driver.session()) {
            records = session.readTransaction(tx -> {
                Result result = tx.run(useCase);
                return result.list();
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
            session.readTransaction(tx -> {
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
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return classNames;
    }

    public List<String> getAllRelationshipNames() {
        List<String> relationshipNames = new ArrayList<>();
        String command = "MATCH (n)-[r]-(m) RETURN DISTINCT type(r)\n";
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                for (Record record : list) {
                    Map<String, String> map = record.asMap(v -> String.valueOf(v)
                            .replaceAll("\"", "")
                            .replaceAll("\\[", "")
                            .replaceAll("\\]", ""));
                    map.forEach((k, v) -> relationshipNames.add(v));
                }
                return null;
            });
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
            session.readTransaction(tx -> {
                Result result = tx.run(command);
                List<Record> list = result.list();
                for (Record record : list) {

                    Map<String, Object> map = record.asMap();
                    String className = (String) map.get("className");
                    List<String> attributes = (List<String>) map.get("attributes");
                    attributeMap.put(className, attributes);
                }
                return null;
            });
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return attributeMap;
    }
}
