package com.yara.odx;

import com.yara.odx.domain.Info;
import com.yara.odx.domain.UseCaseAnswer;
import com.yara.odx.reader.AnswerReader;
import com.yara.odx.reader.UseCaseReader;
import com.yara.odx.requestor.Requester;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

//TODO prepare correct use cases after changing classes properties
// 1 Country
public class UseCaseTest {

    private Requester requester = new Requester();
    private UseCaseReader useCaseReader = new UseCaseReader();
    private AnswerReader answerReader = new AnswerReader();

    private List<String> useCases = useCaseReader.readUseCases();
    private List<UseCaseAnswer> validAnswers = answerReader.readValidAnswers();

    @Test
    void testUseCaseAnswersNotEmpty() {
        for (int i = 0; i < useCases.size(); i++) {
            int count = i + 1;
            String useCase = useCases.get(i);
            List<Record> answer = requester.request(useCase);

            if (answer.isEmpty()) {
                System.out.printf("UseCase # %d result IS EMPTY\n", count);
                System.out.println("****************************************");
            } else {
                System.out.printf("UseCase # %d result is not empty\n", count);
            }

            assertFalse(answer.isEmpty());
        }
    }

    @Test
    void testUseCaseAnswersEqualExpectedAnswers() {
        for (int i = 0; i < useCases.size(); i++) {
            int count = i + 1;

            String useCase = useCases.get(i);
            Record record = requester.request(useCase).get(0);
            List<String> keys = record.keys();

            UseCaseAnswer expectedAnswer = validAnswers.get(i);
            Map<String, Info> infoMap = expectedAnswer.getInfoMap();

            for (int j = 0; j < record.size(); j++) {
                String key = keys.get(j);
                Value value = record.get(key);
                Info info = infoMap.get(key);

                if (value.type().name() == "NODE") {
                    validateNode(count, key, value, info);
                } else if (value.type().name() == "RELATIONSHIP") {
                    validateRelationship(count, key, value, info);
                } else {
                    System.out.println("Unknown value type");
                    System.out.println("****************************************");
                    fail();
                }
            }
        }
    }

    private void validateNode(int count, String key, Value value, Info info) {
        Node node = value.asNode();
        Iterable<String> actualLabels = node.labels();
        Map<String, String> actualProperties = node.asMap(v -> String.valueOf(v).replace("\"", ""));

        List<String> expectedLabels = info.getLabels();
        Map<String, String> expectedProperties = info.getProperties();

        if (actualLabels.equals(expectedLabels)) {
            System.out.printf("UseCase # %d %s value labels are equal to expected labels.\n", count, key);
        } else {
            System.out.printf("UseCase # %d %s value labels are not equal to expected labels.\n", count, key);
            System.out.println("****************************************");
        }
        if (actualProperties.equals(expectedProperties)) {
            System.out.printf("UseCase # %d %s value properties are equal to expected properties.\n", count, key);
        } else {
            System.out.printf("UseCase # %d %s value properties ARE NOT equal to expected properties.\n", count, key);
            System.out.println("****************************************");
        }

        assertEquals(expectedLabels, actualLabels);
        assertEquals(expectedProperties, actualProperties);
    }

    private void validateRelationship(int count, String key, Value value, Info info) {
        Relationship relationship = value.asRelationship();
        String actualType = relationship.type();
        Map<String, String> actualProperties = relationship.asMap(v -> String.valueOf(v).replace("\"", ""));

        String expectedType = info.getType();
        Map<String, String> expectedProperties = info.getProperties();

        if (actualType.equals(expectedType)) {
            System.out.printf("UseCase # %d %s type is equal to expected type.\n", count, key);
        } else {
            System.out.printf("UseCase # %d %s type IS NOT equal to expected type.\n", count, key);
            System.out.println("***************************************************");
        }
        if (actualProperties.equals(expectedProperties)) {
            System.out.printf("UseCase # %d %s value properties are equal expected properties.\n", count, key);
        } else {
            System.out.printf("UseCase # %d %s value properties are not equal expected properties.\n", count, key);
            System.out.println("***************************************************");
        }

        assertEquals(expectedType, actualType);
        assertEquals(expectedProperties, actualProperties);

    }
}
