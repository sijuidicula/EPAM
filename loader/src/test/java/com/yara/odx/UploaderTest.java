package com.yara.odx;

import com.yara.odx.domain.*;
import com.yara.odx.loader.PropertyGraphUploader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UploaderTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();

    private PropertyGraphUploader uploader;

    private Neo4j embeddedDatabaseServer;

    @BeforeAll
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().build();
        this.uploader = new PropertyGraphUploader(embeddedDatabaseServer.boltURI(), driverConfig);
    }

    @Test
    void testUploadCountriesAsBatchCorrectly() {
        Country country1 = new Country("testSource", "Country", "testId1", "testName1", "");
        Country country2 = new Country("testSource", "Country", "testId2", "testName2", "");

        List<Country> countries = Arrays.asList(country1, country2);

        uploader.uploadCountriesAsBatch(countries);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:Country) RETURN n ORDER BY n.CountryId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CountryId", country1.getId(),
                                    "ODX_Country_UUId", country1.getUuId().toString(),
                                    "CountryName", country1.getName(),
                                    "ProductSetCode", country1.getProductSetCode(),
                                    "ODX_Country_Uri", "ODX/Country/".concat(country1.getUuId().toString())
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CountryId", country2.getId(),
                                    "ODX_Country_UUId", country2.getUuId().toString(),
                                    "CountryName", country2.getName(),
                                    "ProductSetCode", country2.getProductSetCode(),
                                    "ODX_Country_Uri", "ODX/Country/".concat(country2.getUuId().toString())
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadRegionsAsBatchCorrectly() {
        Country country1 = new Country("testSource", "Country", "testCountryId1", "testCountryName1", "");
        Country country2 = new Country("testSource", "Country", "testCountryId2", "testCountryName2", "");

        Region region1 = new Region("testSource", "Region", "testRegionId1", "testCountryId1", "testRegionName1");
        Region region2 = new Region("testSource", "Region", "testRegionId2", "testCountryId2", "testRegionName2");

        List<Region> regions = Arrays.asList(region1, region2);
        List<Country> countries = Arrays.asList(country1, country2);

        uploader.uploadRegionsAsBatch(regions, countries);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:Region) RETURN n ORDER BY n.RegionId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_Region_UUId", region1.getUuId().toString(),
                                    "ODX_Region_Uri", "ODX/Region/".concat(region1.getUuId().toString()),
                                    "RegionId", region1.getId(),
                                    "RegionName", region1.getName(),
                                    "Region_CountryId_Ref", country1.getId(),
                                    "Region_Country_UUId_Ref", country1.getUuId().toString()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_Region_UUId", region2.getUuId().toString(),
                                    "ODX_Region_Uri", "ODX/Region/".concat(region2.getUuId().toString()),
                                    "RegionId", region2.getId(),
                                    "RegionName", region2.getName(),
                                    "Region_CountryId_Ref", country2.getId(),
                                    "Region_Country_UUId_Ref", country2.getUuId().toString()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropGroupsAsBatchCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupMediaUri1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupMediaUri2", "testCropGroupName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);

        uploader.uploadCropGroupsAsBatch(cropGroups);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:CropGroup) RETURN n ORDER BY n.CropGroupId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_CropGroup_UUId", cropGroup1.getUuId().toString(),
                                    "ODX_CropGroup_Uri", "ODX/CropGroup/".concat(cropGroup1.getUuId().toString()),
                                    "CropGroupName", cropGroup1.getName(),
                                    "CropGroupId", cropGroup1.getId(),
                                    "CG_MediaUri", cropGroup1.getMediaUri(),
                                    "CG_FAOId", cropGroup1.getFaoId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropGroup_UUId", cropGroup2.getUuId().toString(),
                                    "ODX_CropGroup_Uri", "ODX/CropGroup/".concat(cropGroup2.getUuId().toString()),
                                    "CropGroupName", cropGroup2.getName(),
                                    "CropGroupId", cropGroup2.getId(),
                                    "CG_MediaUri", cropGroup2.getMediaUri(),
                                    "CG_FAOId", cropGroup2.getFaoId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropClassesAsBatchCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupMediaUri1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupMediaUri2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassMediaUri1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassMediaUri2", "testCropClassName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);

        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:CropClass) RETURN n ORDER BY n.CropClassId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_CropClass_UUId", cropClass1.getUuId().toString(),
                                    "ODX_CropClass_Uri", "ODX/CropClass/".concat(cropClass1.getUuId().toString()),
                                    "CropClassId", cropClass1.getId(),
                                    "CropGroupId_Ref", cropClass1.getGroupId(),
                                    "ODX_CG_UUId_Ref", cropGroup1.getUuId().toString(),
                                    "CC_FAOId", cropClass1.getFaoId(),
                                    "CC_MediaUri", cropClass1.getMediaUri(),
                                    "CropClassName", cropClass1.getName()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropClass_UUId", cropClass2.getUuId().toString(),
                                    "ODX_CropClass_Uri", "ODX/CropClass/".concat(cropClass2.getUuId().toString()),
                                    "CropClassId", cropClass2.getId(),
                                    "CropGroupId_Ref", cropClass2.getGroupId(),
                                    "ODX_CG_UUId_Ref", cropGroup2.getUuId().toString(),
                                    "CC_FAOId", cropClass2.getFaoId(),
                                    "CC_MediaUri", cropClass2.getMediaUri(),
                                    "CropClassName", cropClass2.getName()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropSubClassesAsBatchCorrectly() {
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", "testCropGroupId1",
                "testCropClassFaoId1", "testCropClassMediaUri1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", "testCropGroupId2",
                "testCropClassFaoId2", "testCropClassMediaUri2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassMediaUri1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassMediaUri2", "testCropSubClassName2");

        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);

        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:CropSubClass) RETURN n ORDER BY n.CropSubClassId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_CropSubClass_UUId", cropSubClass1.getUuId().toString(),
                                    "ODX_CropSubClass_Uri", "ODX/CropSubClass/".concat(cropSubClass1.getUuId().toString()),
                                    "CropSubClassId", cropSubClass1.getId(),
                                    "CropClassId_Ref", cropSubClass1.getClassId(),
                                    "ODX_CC_UUId_Ref", cropClass1.getUuId().toString(),
                                    "CSC_FAOId", cropSubClass1.getFaoId(),
                                    "CSC_MediaUri", cropSubClass1.getMediaUri(),
                                    "CropSubClassName", cropSubClass1.getName()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropSubClass_UUId", cropSubClass2.getUuId().toString(),
                                    "ODX_CropSubClass_Uri", "ODX/CropSubClass/".concat(cropSubClass2.getUuId().toString()),
                                    "CropSubClassId", cropSubClass2.getId(),
                                    "CropClassId_Ref", cropSubClass2.getClassId(),
                                    "ODX_CC_UUId_Ref", cropClass2.getUuId().toString(),
                                    "CSC_FAOId", cropSubClass2.getFaoId(),
                                    "CSC_MediaUri", cropSubClass2.getMediaUri(),
                                    "CropSubClassName", cropSubClass2.getName()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropVarietiesAsBatchCorrectly() {
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1",
                "testCropClassId1", "testCropSubClassFaoId1", "testCropSubClassMediaUri1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2",
                "testCropClassId2", "testCropSubClassFaoId2", "testCropSubClassMediaUri2", "testCropSubClassName2");
        CropVariety cropVariety1 = new CropVariety("testSource", "CropVariety", "testCropVarietyId1", cropSubClass1.getId(),
                "testCropVarietyName1");
        CropVariety cropVariety2 = new CropVariety("testSource", "CropVariety", "testCropVarietyId2", cropSubClass2.getId(),
                "testCropVarietyName2");

        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropVariety> cropVarieties = Arrays.asList(cropVariety1, cropVariety2);

        uploader.uploadCropVarietiesAsBatch(cropVarieties, cropSubClasses);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:CropVariety) RETURN n ORDER BY n.CropVarietyId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_CropVariety_UUId", cropVariety1.getUuId().toString(),
                                    "ODX_CropVariety_Uri", "ODX/CropVariety/".concat(cropVariety1.getUuId().toString()),
                                    "CV_CropSubClassId_Ref", cropVariety1.getSubClassId(),
                                    "CV_CSC_UUId_Ref", cropSubClass1.getUuId().toString(),
                                    "CropVarietyId", cropVariety1.getId(),
                                    "CropVarietyName", cropVariety1.getName()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropVariety_UUId", cropVariety2.getUuId().toString(),
                                    "ODX_CropVariety_Uri", "ODX/CropVariety/".concat(cropVariety2.getUuId().toString()),
                                    "CV_CropSubClassId_Ref", cropVariety2.getSubClassId(),
                                    "CV_CSC_UUId_Ref", cropSubClass2.getUuId().toString(),
                                    "CropVarietyId", cropVariety2.getId(),
                                    "CropVarietyName", cropVariety2.getName()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropDescriptionsAsBatchCorrectly() {
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1",
                "testCropClassId1", "testCropSubClassFaoId1", "testCropSubClassMediaUri1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2",
                "testCropClassId2", "testCropSubClassFaoId2", "testCropSubClassMediaUri2", "testCropSubClassName2");

        CropDescription cropDescription1 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId1",
                cropSubClass1.getId(), "testTrue", "testCropDescriptionMediaUri1", "testCropDescriptionName1");
        CropDescription cropDescription2 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId2",
                cropSubClass2.getId(), "testFalse", "testCropDescriptionMediaUri2", "testCropDescriptionName2");

        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropDescription> cropDescriptions = Arrays.asList(cropDescription1, cropDescription2);

        uploader.uploadCropDescriptionsAsBatch(cropDescriptions, cropSubClasses);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:CropDescription) RETURN n ORDER BY n.CropDescriptionId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_CropDescription_UUId", cropDescription1.getUuId().toString(),
                                    "ODX_CropDescription_Uri", "ODX/CropDescription/".concat(cropDescription1.getUuId().toString()),
                                    "CD_MediaUri", cropDescription1.getMediaUri(),
                                    "ChlorideSensitive", cropDescription1.isChlorideSensitive(),
                                    "CropDescriptionId", cropDescription1.getId(),
                                    "CropDescriptionName", cropDescription1.getName(),
                                    "CD_CropSubClassId_Ref", cropDescription1.getSubClassId(),
                                    "CD_CSC_UUId_Ref", cropSubClass1.getUuId().toString(),
                                    "ODX_CD_SourceSystem", cropDescription1.getSource()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropDescription_UUId", cropDescription2.getUuId().toString(),
                                    "ODX_CropDescription_Uri", "ODX/CropDescription/".concat(cropDescription2.getUuId().toString()),
                                    "CD_MediaUri", cropDescription2.getMediaUri(),
                                    "ChlorideSensitive", cropDescription2.isChlorideSensitive(),
                                    "CropDescriptionId", cropDescription2.getId(),
                                    "CropDescriptionName", cropDescription2.getName(),
                                    "CD_CropSubClassId_Ref", cropDescription2.getSubClassId(),
                                    "CD_CSC_UUId_Ref", cropSubClass2.getUuId().toString(),
                                    "ODX_CD_SourceSystem", cropDescription2.getSource()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadGrowthScalesAsBatchCorrectly() {
        GrowthScale growthScale1 = new GrowthScale("testSource", "GrowthScale", "testGrowthScaleId1",
                "testGrowthScaleName1");
        GrowthScale growthScale2 = new GrowthScale("testSource", "GrowthScale", "testGrowthScaleId2",
                "testGrowthScaleName2");

        List<GrowthScale> growthScales = Arrays.asList(growthScale1, growthScale2);

        uploader.uploadGrowthScalesAsBatch(growthScales);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:GrowthScale) RETURN n ORDER BY n.GrowthScaleId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_GrowthScale_UUId", growthScale1.getUuId().toString(),
                                    "ODX_GrowthScale_Uri", "ODX/GrowthScale/".concat(growthScale1.getUuId().toString()),
                                    "GrowthScaleId", growthScale1.getId(),
                                    "GrowthScaleName", growthScale1.getName()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_GrowthScale_UUId", growthScale2.getUuId().toString(),
                                    "ODX_GrowthScale_Uri", "ODX/GrowthScale/".concat(growthScale2.getUuId().toString()),
                                    "GrowthScaleId", growthScale2.getId(),
                                    "GrowthScaleName", growthScale2.getName()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadGrowthScaleStagesAsBatchCorrectly() {
        GrowthScale growthScale1 = new GrowthScale("testSource", "GrowthScale", "testGrowthScaleId1",
                "testGrowthScaleName1");
        GrowthScale growthScale2 = new GrowthScale("testSource", "GrowthScale", "testGrowthScaleId2",
                "testGrowthScaleName2");
        GrowthScaleStages scaleStage1 = new GrowthScaleStages("testSource", "GrowthScaleStages",
                "testGrowthScaleStagesId1", "testGrowthScaleStagesName1", growthScale1.getId(),
                "testGrowthScaleStagesDescription1", "testOrdinal1", "TestBaseOrdinal1");
        GrowthScaleStages scaleStage2 = new GrowthScaleStages("testSource", "GrowthScaleStages",
                "testGrowthScaleStagesId2", "testGrowthScaleStagesName2", growthScale2.getId(),
                "testGrowthScaleStagesDescription2", "testOrdinal2", "TestBaseOrdinal2");

        List<GrowthScale> growthScales = Arrays.asList(growthScale1, growthScale2);
        List<GrowthScaleStages> growthScaleStages = Arrays.asList(scaleStage1, scaleStage2);

        uploader.uploadGrowthScaleStagesAsBatch(growthScaleStages, growthScales);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:GrowthScaleStages) RETURN n ORDER BY n.GrowthScaleStageId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        System.out.println(node.asNode().asMap(String::valueOf));
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_GrowthScaleStage_UUId", scaleStage1.getUuId().toString(),
                                    "ODX_GrowthScaleStage_Uri", "ODX/GrowthScaleStages/".concat(scaleStage1.getUuId().toString()),
                                    "BaseOrdinal", scaleStage1.getBaseOrdinal(),
                                    "GrowthScaleId_Ref", scaleStage1.getGrowthScaleId(),
                                    "ODX_GS_UUId_Ref", growthScale1.getUuId().toString(),
                                    "GrowthScaleStageDescription", scaleStage1.getGrowthScaleStageDescription(),
                                    "GrowthScaleStageId", scaleStage1.getId(),
                                    "ODX_GSS_SourceSystem", scaleStage1.getSource(),
                                    "Ordinal", scaleStage1.getOrdinal()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_GrowthScaleStage_UUId", scaleStage2.getUuId().toString(),
                                    "ODX_GrowthScaleStage_Uri", "ODX/GrowthScaleStages/".concat(scaleStage2.getUuId().toString()),
                                    "BaseOrdinal", scaleStage2.getBaseOrdinal(),
                                    "GrowthScaleId_Ref", scaleStage2.getGrowthScaleId(),
                                    "ODX_GS_UUId_Ref", growthScale2.getUuId().toString(),
                                    "GrowthScaleStageDescription", scaleStage2.getGrowthScaleStageDescription(),
                                    "GrowthScaleStageId", scaleStage2.getId(),
                                    "ODX_GSS_SourceSystem", scaleStage2.getSource(),
                                    "Ordinal", scaleStage2.getOrdinal()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadNutrientsAsBatchCorrectly() {
        Nutrient nutrient1 = new Nutrient("testSource", "Nutrient", "testNutrientId1",
                "testNutrientName1", "testNutrientElementalName1", "testNutrientOrdinal1");
        Nutrient nutrient2 = new Nutrient("testSource", "Nutrient", "testNutrientId2",
                "testNutrientName2", "testNutrientElementalName2", "testNutrientOrdinal2");

        List<Nutrient> nutrients = Arrays.asList(nutrient1, nutrient2);

        uploader.uploadNutrientsAsBatch(nutrients);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:Nutrient) RETURN n ORDER BY n.NutrientId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        System.out.println(node.asNode().asMap(String::valueOf));
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_Nutrient_UUId", nutrient1.getUuId().toString(),
                                    "ODX_Nutrient_Uri", "ODX/Nutrient/".concat(nutrient1.getUuId().toString()),
                                    "NutrientId", nutrient1.getId(),
                                    "NutrientName", nutrient1.getName(),
                                    "ElementalName", nutrient1.getElementalName(),
                                    "Nutr_Ordinal", nutrient1.getNutrientOrdinal(),
                                    "ODX_Nutr_SourceSystem", nutrient1.getSource()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_Nutrient_UUId", nutrient2.getUuId().toString(),
                                    "ODX_Nutrient_Uri", "ODX/Nutrient/".concat(nutrient2.getUuId().toString()),
                                    "NutrientId", nutrient2.getId(),
                                    "NutrientName", nutrient2.getName(),
                                    "ElementalName", nutrient2.getElementalName(),
                                    "Nutr_Ordinal", nutrient2.getNutrientOrdinal(),
                                    "ODX_Nutr_SourceSystem", nutrient2.getSource()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadUnitsAsBatchCorrectly() {
        Units units1 = new Units("testSource", "Units", "testUnitsId1", "testUnitsName1", "testUnitsTag1");
        Units units2 = new Units("testSource", "Units", "testUnitsId2", "testUnitsName2", "testUnitsTag2");

        List<Units> units = Arrays.asList(units1, units2);

        uploader.uploadUnitsAsBatch(units);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:Units) RETURN n ORDER BY n.UnitsId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        System.out.println(node.asNode().asMap(String::valueOf));
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_Units_UUId", units1.getUuId().toString(),
                                    "ODX_Units_Uri", "ODX/Units/".concat(units1.getUuId().toString()),
                                    "UnitsId", units1.getId(),
                                    "UnitsName", units1.getName(),
                                    "UnitsTags", units1.getTag()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_Units_UUId", units2.getUuId().toString(),
                                    "ODX_Units_Uri", "ODX/Units/".concat(units2.getUuId().toString()),
                                    "UnitsId", units2.getId(),
                                    "UnitsName", units2.getName(),
                                    "UnitsTags", units2.getTag()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadUnitConversionsAsBatchCorrectly() {
        Units units1 = new Units("testSource", "Units", "testUnitsId1", "testUnitsName1", "testUnitsTag1");
        Units units2 = new Units("testSource", "Units", "testUnitsId2", "testUnitsName2", "testUnitsTag2");
        UnitConversion unitConversion1 = new UnitConversion("testSource", "UnitConversion", "testUnitConversionId1",
                units1.getId(), units2.getId(), "testMultiplier1", "testCountryIdRef1");
        UnitConversion unitConversion2 = new UnitConversion("testSource", "UnitConversion", "testUnitConversionId2",
                units2.getId(), units1.getId(), "testMultiplier2", "testCountryIdRef2");

        List<Units> units = Arrays.asList(units1, units2);
        List<UnitConversion> unitConversions = Arrays.asList(unitConversion1, unitConversion2);

        uploader.uploadUnitConversionsAsBatch(unitConversions, units);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:UnitConversion) RETURN n ORDER BY n.UnitConversionId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        System.out.println(node.asNode().asMap(String::valueOf));
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ODX_UnitConversion_UUId", unitConversion1.getUuId().toString(),
                                    "ODX_UnitConversion_Uri", "ODX/UnitConversion/".concat(unitConversion1.getUuId().toString()),
                                    "UnitConversionName", units2.getName(),
                                    "ConvertToUnitId", unitConversion1.getConvertToUnitId(),
                                    "CountryId_Ref", unitConversion1.getCountryIdRef(),
                                    "Multiplier", unitConversion1.getMultiplier(),
                                    "UnitConversionId", unitConversion1.getId(),
                                    "UnitId_Ref", unitConversion1.getUnitIdRef(),
                                    "ODX_UC_SourceSystem", unitConversion1.getSource()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_UnitConversion_UUId", unitConversion2.getUuId().toString(),
                                    "ODX_UnitConversion_Uri", "ODX/UnitConversion/".concat(unitConversion2.getUuId().toString()),
                                    "UnitConversionName", units1.getName(),
                                    "ConvertToUnitId", unitConversion2.getConvertToUnitId(),
                                    "CountryId_Ref", unitConversion2.getCountryIdRef(),
                                    "Multiplier", unitConversion2.getMultiplier(),
                                    "UnitConversionId", unitConversion2.getId(),
                                    "UnitId_Ref", unitConversion2.getUnitIdRef(),
                                    "ODX_UC_SourceSystem", unitConversion2.getSource()
                            ).asMap(String::valueOf)
                    );
        }
    }
}
