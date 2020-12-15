package com.yara.ss;

import com.yara.ss.reader.StatisticsReader;
import com.yara.ss.requestor.Requestor;

import java.util.Map;

public class ValidatorMain {
    public static void main(String[] args) {

        Requestor requestor = new Requestor();
        StatisticsReader reader = new StatisticsReader();

        Map<String, Integer> statistics = reader.getStatistics();

        for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
            String className = entry.getKey();
            int classStat = entry.getValue();
            int nodesCount = requestor.getNumberOfNodesForClass(className);

            if (classStat == nodesCount) {
                System.out.println(String.format("Class %s stats are equal to numbers in DB", className));
            } else {
                System.out.println(String.format("Class %s stats NOT equal to numbers in DB", className));
                System.out.println(String.format("%s in stats: %d; in DB: %d", className, classStat, nodesCount));
                System.out.println("****************************************");
            }
        }
//        int cropGroupNodesCount = requestor.getNumberOfNodesForClass("CropGroup");
//        System.out.println(cropGroupNodesCount);
//        int cropClassNodesCount = requestor.getNumberOfNodesForClass("CropClass");
//        System.out.println(cropClassNodesCount);
//        int cropSubClassNodesCount = requestor.getNumberOfNodesForClass("CropSubClass");
//        System.out.println(cropSubClassNodesCount);
//        int cropVarietyNodesCount = requestor.getNumberOfNodesForClass("CropVariety");
//        System.out.println(cropVarietyNodesCount);

        requestor.close();
    }
}
