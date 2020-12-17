package com.yara.ss;

import com.yara.ss.reader.StatisticsReader;
import com.yara.ss.requestor.Requestor;
import com.yara.ss.validator.StatisticsValidator;

import java.util.Map;

public class ValidatorMain {
    public static void main(String[] args) {

        Requestor requestor = new Requestor();
        StatisticsReader reader = new StatisticsReader();
        StatisticsValidator validator = new StatisticsValidator();

        Map<String, Integer> statistics = reader.getStatistics();
        validator.validateNodesCount(requestor, statistics);
        validator.validateNonEmptyUuids(requestor, statistics);
        validator.validateNonEmptyURIs(requestor, statistics);
        validator.validateNonEmptyLabels(requestor, statistics);

        requestor.close();
    }
}
