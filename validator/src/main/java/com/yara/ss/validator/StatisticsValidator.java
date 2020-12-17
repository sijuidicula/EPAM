package com.yara.ss.validator;

import com.yara.ss.requestor.Requestor;

import java.util.Map;

public class StatisticsValidator {

    public void validateNodesCount(Requestor requestor, Map<String, Integer> statistics) {
        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int classStat = entry.getValue();
            int nodesCount = requestor.getNodesCount(className);

            if (classStat == nodesCount) {
//                System.out.println(String.format("Class %s stats are equal to numbers in DB", className));
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
//                System.out.println(String.format("Class %s has no nodes with empty UUIds", className));
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
//                System.out.println(String.format("Class %s has no nodes with empty URIs", className));
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
//                System.out.println(String.format("Class %s has no nodes with empty labels", className));
            } else {
                System.out.println(String.format("Class %s has %d nodes with empty labels", className, nodesWithEmptyLabels));
                System.out.println("****************************************");
            }
        }

    }
}
