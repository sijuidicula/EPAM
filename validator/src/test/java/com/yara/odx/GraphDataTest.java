package com.yara.odx;

import com.yara.odx.reader.StatisticsReader;
import com.yara.odx.requestor.Requestor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GraphDataTest {


    private Requestor requestor = new Requestor();
    private StatisticsReader statisticsReader = new StatisticsReader();

    private Map<String, Integer> statistics = statisticsReader.getStatistics();

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

    @Test
    void testDatabaseHasNoBlankNodes() {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();

//            TODO
//             Class GrowthScaleStages does not have "GrowthScaleStageName" property
            if (className == "GrowthScaleStages") {
                System.out.println(className);
                continue;
            }

            int actualBlankNodeCount = requestor.getEmptyLabelsCount(className);

            if (actualBlankNodeCount == 0) {
                System.out.println(String.format("Class %s has no nodes with empty labels", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty labels", className, actualBlankNodeCount));
                System.out.println("****************************************");
            }

            assertEquals(0, actualBlankNodeCount);
        }
    }

    @Test
    void testDatabaseHasNoEmptyUri() {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int nodesWithEmptyUriCount = requestor.getEmptyUrisCount(className);

            if (nodesWithEmptyUriCount == 0) {
                System.out.println(String.format("Class %s has no nodes with empty URIs", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty URIs", className, nodesWithEmptyUriCount));
                System.out.println("****************************************");
            }

            assertEquals(0, nodesWithEmptyUriCount);
        }
    }

    @Test
    void testDatabaseHasNoEmptyUUId() {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int nodesWithEmptyUuidCount = requestor.getEmptyUuidsCount(className);

            if (nodesWithEmptyUuidCount == 0) {
                System.out.println(String.format("Class %s has no nodes with empty UUIds", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty UUIds", className, nodesWithEmptyUuidCount));
                System.out.println("****************************************");
            }

            assertEquals(0, nodesWithEmptyUuidCount);
        }
    }
}
