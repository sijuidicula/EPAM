package com.yara.odx;

import com.yara.odx.domain.OntologyStructure;
import com.yara.odx.domain.UseCaseAnswer;
import com.yara.odx.reader.AnswerReader;
import com.yara.odx.reader.OntologyReader;
import com.yara.odx.reader.StatisticsReader;
import com.yara.odx.reader.UseCaseReader;
import com.yara.odx.requestor.Requestor;
import com.yara.odx.validator.StatisticsValidator;

import java.util.List;
import java.util.Map;

public class ValidatorMain {
    public static void main(String[] args) {

        Requestor requestor = new Requestor();
        StatisticsReader statisticsReader = new StatisticsReader();
        StatisticsValidator validator = new StatisticsValidator();
        UseCaseReader useCaseReader = new UseCaseReader();
        OntologyReader ontologyReader = new OntologyReader();
        AnswerReader answerReader = new AnswerReader();

        Map<String, Integer> statistics = statisticsReader.getStatistics();
        OntologyStructure ontologyStructure = ontologyReader.getOntologyStructure();
        List<String> useCases = useCaseReader.readUseCases();
        List<UseCaseAnswer> validAnswers = answerReader.readValidAnswers();

        validator.validateUseCases(requestor, useCases, validAnswers);
        validator.validateSchema(requestor, ontologyStructure);
        validator.validateNodesCount(requestor, statistics);
        validator.validateNonEmptyLabels(requestor, statistics);
        validator.validateNonEmptyUuids(requestor, statistics);
        validator.validateNonEmptyURIs(requestor, statistics);

        requestor.close();
    }
}
