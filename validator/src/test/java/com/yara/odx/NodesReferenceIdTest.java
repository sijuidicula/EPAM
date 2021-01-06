package com.yara.odx;

import com.yara.odx.requestor.Requester;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.types.Node;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NodesReferenceIdTest {

    private final Requester requester = new Requester();

    @Test
    void testRegionHasCorrectCountryReferenceIds() {
        String subject = "Country";
        String predicate = "hasRegion";
        String object = "Region";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testCropClassHasCorrectCropGroupReferenceIds() {
        String subject = "CropGroup";
        String predicate = "hasCropClass";
        String object = "CropClass";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testCropSubClassHasCorrectCropClassReferenceIds() {
        String subject = "CropClass";
        String predicate = "hasCropSubClass";
        String object = "CropSubClass";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testCropVarietyHasCorrectCropSubClassReferenceIds() {
        String subject = "CropSubClass";
        String predicate = "hasCropVariety";
        String object = "CropVariety";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testCropDescriptionHasCorrectCropSubClassReferenceIds() {
        String subject = "CropSubClass";
        String predicate = "hasCropDescription";
        String object = "CropDescription";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testGrowthScaleStageHasCorrectGrowthScaleReferenceIds() {
        String subject = "GrowthScale";
        String predicate = "hasGrowthScaleStages";
        String object = "GrowthScaleStages";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMap(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }

    @Test
    void testUnitConversionHasCorrectUnitsReferenceIds() {
        String subject = "Units";
        String predicate = "hasUnitConversion";
        String object = "UnitConversion";

        Map<Node, Node> incorrectNodesMap = requester.getIncorrectRelationNodesMap(subject, predicate, object);

//        TODO
//         In Units class we use
//         UnitsId (with "s")
//         property notation, but in UnitConversion class we use
//         UnitId_Ref (without "s")
//         property notation. Need to remove this inconsistency
        Map<Node, Node> actualNodesMap = requester.getCorrectRelationNodesMapForUnits(subject, predicate, object);

        assertTrue(incorrectNodesMap.isEmpty());
        assertFalse(actualNodesMap.isEmpty());
    }
}
