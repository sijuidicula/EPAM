package com.yara.odx;

import com.yara.odx.requestor.Requester;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelationsTest {

    private Requester requester = new Requester();

    @Test
    void testCorrectSubjectAnsObjectForHasCountryRelation() {
        String relation = "hasRegion";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("Country", "Region");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForIsAvailableInRelation() {
        String relation = "isAvailableIn";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropDescription", "Region");
            put("Fertilizers", "Region");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasCropClassRelation() {
        String relation = "hasCropClass";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropGroup", "CropClass");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasCropSubClassRelation() {
        String relation = "hasCropSubClass";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropClass", "CropSubClass");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasCropDescriptionRelation() {
        String relation = "hasCropDescription";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropVariety", "CropDescription");
            put("CropSubClass", "CropDescription");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasCropVarietyRelation() {
        String relation = "hasCropVariety";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropSubClass", "CropVariety");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasProdNutrientRelation() {
        String relation = "hasProdNutrient";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("Fertilizers", "Nutrient");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasGrowthScaleRelation() {
        String relation = "hasGrowthScale";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("CropDescription", "GrowthScale");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasGrowthScaleStagesRelation() {
        String relation = "hasGrowthScaleStages";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("GrowthScale", "GrowthScaleStages");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasNutrientUnitRelation() {
        String relation = "hasNutrientUnit";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("Nutrient", "Units");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }

    @Test
    void testCorrectSubjectAnsObjectForHasUnitConversionRelation() {
        String relation = "hasUnitConversion";
        Map<String, String> expectedNodesMap = new HashMap() {{
            put("Units", "UnitConversion");
        }};

        Map<String, String> actualNodesMap = requester.getRelationNodesMap(relation);

        assertEquals(expectedNodesMap, actualNodesMap);
    }
}
