package com.yara.ss.requestor;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.List;

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

    public int getNumberOfNodesForClass(String className) {
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
}
