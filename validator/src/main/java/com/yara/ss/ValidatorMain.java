package com.yara.ss;

import com.yara.ss.reader.StatisticsReader;
import com.yara.ss.reader.UseCaseReader;
import com.yara.ss.requestor.Requestor;
import com.yara.ss.validator.StatisticsValidator;

import java.util.List;
import java.util.Map;

public class ValidatorMain {
    public static void main(String[] args) {

        Requestor requestor = new Requestor();
        StatisticsReader statisticsReader = new StatisticsReader();
        StatisticsValidator validator = new StatisticsValidator();
        UseCaseReader useCaseReader = new UseCaseReader();
        useCaseReader.readUseCases();

        Map<String, Integer> statistics = statisticsReader.getStatistics();
        List<String> useCases = useCaseReader.getUseCases();

        validator.validateUseCases(requestor, useCases);
//        validator.validateNodesCount(requestor, statistics);
//        validator.validateNonEmptyUuids(requestor, statistics);
//        validator.validateNonEmptyURIs(requestor, statistics);
//        validator.validateNonEmptyLabels(requestor, statistics);

        requestor.close();
    }
}
