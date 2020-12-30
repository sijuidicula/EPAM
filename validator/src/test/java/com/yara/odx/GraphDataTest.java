package com.yara.odx;

import com.yara.odx.domain.OntologyStructure;
import com.yara.odx.domain.UseCaseAnswer;
import com.yara.odx.reader.AnswerReader;
import com.yara.odx.reader.OntologyReader;
import com.yara.odx.reader.StatisticsReader;
import com.yara.odx.reader.UseCaseReader;
import com.yara.odx.requestor.Requestor;
import com.yara.odx.validator.StatisticsValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphDataTest {

    private Requestor requestor = new Requestor();
    private StatisticsReader statisticsReader = new StatisticsReader();
    private StatisticsValidator validator = new StatisticsValidator();
    private UseCaseReader useCaseReader = new UseCaseReader();
    private OntologyReader ontologyReader = new OntologyReader();
    private AnswerReader answerReader = new AnswerReader();

    private Map<String, Integer> statistics = statisticsReader.getStatistics();
    private OntologyStructure ontologyStructure = ontologyReader.getOntologyStructure();
    private List<String> useCases = useCaseReader.readUseCases();
    private List<UseCaseAnswer> validAnswers = answerReader.readValidAnswers();

    @Test
    void testDatabaseNodesCountMatchPolarisSheet() {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int expectedNodeCount = entry.getValue();
            int actualNodeCount = requestor.getNodesCount(className);

            if (expectedNodeCount == actualNodeCount) {
                System.out.println(String.format("Class %s stats are equal to numbers in DB", className));
            } else {
                System.out.println(String.format("Class %s stats NOT equal to numbers in DB", className));
                System.out.println(String.format("%s in stats: %d; in DB: %d", className, expectedNodeCount, actualNodeCount));
                System.out.println("****************************************");
            }

            assertEquals(expectedNodeCount, actualNodeCount);
        }
    }
}
