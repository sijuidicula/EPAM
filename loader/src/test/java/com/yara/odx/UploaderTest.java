package com.yara.odx;

import com.yara.odx.domain.*;
import com.yara.odx.loader.PropertyGraphUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.*;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UploaderTest {

    private static final Config driverConfig = Config.builder().withoutEncryption().build();

    private PropertyGraphUploader uploader;

    private Neo4j embeddedDatabaseServer;

    @BeforeEach
    void initializeNeo4j() {
        this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder().build();
        this.uploader = new PropertyGraphUploader(embeddedDatabaseServer.boltURI(), driverConfig);
    }

    @Test
    void testUploadCountriesAsBatchCorrectly() {
        Country country1 = new Country("testSource", "Country", "testId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");

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
                                    "CountryName", country1.getName(),
                                    "FIPS", country1.getFips(),
                                    "ISO2Code", country1.getIso2Code(),
                                    "ISO3Code", country1.getIso3Code(),
                                    "M49Code", country1.getM49Code(),
                                    "ODX_Country_Uri", country1.getUri(),
                                    "ODX_Country_UUId", country1.getUuId().toString(),
                                    "ODX_CS_UUId_Ref", country1.getContinentalSectionUuidRef(),
                                    "ProductSetCode", country1.getProductSetCode(),
                                    "UN", country1.getUn()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CountryId", country2.getId(),
                                    "CountryName", country2.getName(),
                                    "FIPS", country2.getFips(),
                                    "ISO2Code", country2.getIso2Code(),
                                    "ISO3Code", country2.getIso3Code(),
                                    "M49Code", country2.getM49Code(),
                                    "ODX_Country_Uri", country2.getUri(),
                                    "ODX_Country_UUId", country2.getUuId().toString(),
                                    "ODX_CS_UUId_Ref", country2.getContinentalSectionUuidRef(),
                                    "ProductSetCode", country2.getProductSetCode(),
                                    "UN", country2.getUn()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadRegionsAsBatchCorrectly() {
        Country country1 = new Country("testSource", "Country", "testCountryId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testCountryId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");

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
                                    "ODX_Region_Uri", region1.getUri(),
                                    "ODX_Region_UUId", region1.getUuId().toString(),
                                    "Region_Country_UUId_Ref", country1.getUuId().toString(),
                                    "Region_CountryId_Ref", region1.getCountryId(),
                                    "RegionId", region1.getId(),
                                    "RegionName", region1.getName()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_Region_Uri", region2.getUri(),
                                    "ODX_Region_UUId", region2.getUuId().toString(),
                                    "Region_Country_UUId_Ref", country2.getUuId().toString(),
                                    "Region_CountryId_Ref", region2.getCountryId(),
                                    "RegionId", region2.getId(),
                                    "RegionName", region2.getName()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropGroupsAsBatchCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");

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
                                    "ODX_CropGroup_Uri", cropGroup1.getUri(),
                                    "CropGroupName", cropGroup1.getName(),
                                    "CropGroupId", cropGroup1.getId(),
                                    "CG_FAOId", cropGroup1.getFaoId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ODX_CropGroup_UUId", cropGroup2.getUuId().toString(),
                                    "ODX_CropGroup_Uri", cropGroup2.getUri(),
                                    "CropGroupName", cropGroup2.getName(),
                                    "CropGroupId", cropGroup2.getId(),
                                    "CG_FAOId", cropGroup2.getFaoId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropClassesAsBatchCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");

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
                                    "CC_FAOId", cropClass1.getFaoId(),
                                    "CropClassId", cropClass1.getId(),
                                    "CropClassName", cropClass1.getName(),
                                    "CropGroupId_Ref", cropClass1.getGroupId(),
                                    "ODX_CG_UUId_Ref", cropGroup1.getUuId().toString(),
                                    "ODX_CropClass_Uri", cropClass1.getUri(),
                                    "ODX_CropClass_UUId", cropClass1.getUuId().toString()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CC_FAOId", cropClass2.getFaoId(),
                                    "CropClassId", cropClass2.getId(),
                                    "CropClassName", cropClass2.getName(),
                                    "CropGroupId_Ref", cropClass2.getGroupId(),
                                    "ODX_CG_UUId_Ref", cropGroup2.getUuId().toString(),
                                    "ODX_CropClass_Uri", cropClass2.getUri(),
                                    "ODX_CropClass_UUId", cropClass2.getUuId().toString()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropSubClassesAsBatchCorrectly() {
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", "testCropGroupId1",
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", "testCropGroupId2",
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");

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
                                    "CropClassId_Ref", cropSubClass1.getClassId(),
                                    "CropSubClassId", cropSubClass1.getId(),
                                    "CropSubClassName", cropSubClass1.getName(),
                                    "CSC_FAOId", cropSubClass1.getFaoId(),
                                    "ODX_CC_UUId_Ref", cropClass1.getUuId().toString(),
                                    "ODX_CropSubClass_Uri", cropSubClass1.getUri(),
                                    "ODX_CropSubClass_UUId", cropSubClass1.getUuId().toString()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropClassId_Ref", cropSubClass2.getClassId(),
                                    "CropSubClassId", cropSubClass2.getId(),
                                    "CropSubClassName", cropSubClass2.getName(),
                                    "CSC_FAOId", cropSubClass2.getFaoId(),
                                    "ODX_CC_UUId_Ref", cropClass2.getUuId().toString(),
                                    "ODX_CropSubClass_Uri", cropSubClass2.getUri(),
                                    "ODX_CropSubClass_UUId", cropSubClass2.getUuId().toString()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropVarietiesAsBatchCorrectly() {
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1",
                "testCropClassId1", "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2",
                "testCropClassId2", "testCropSubClassFaoId2", "testCropSubClassName2");
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
                                    "CropVarietyId", cropVariety1.getId(),
                                    "CropVarietyName", cropVariety1.getName(),
                                    "CV_CropSubClassId_Ref", cropVariety1.getSubClassId(),
                                    "CV_CSC_UUId_Ref", cropSubClass1.getUuId().toString(),
                                    "ODX_CropVariety_Uri", cropVariety1.getUri(),
                                    "ODX_CropVariety_UUId", cropVariety1.getUuId().toString()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropVarietyId", cropVariety2.getId(),
                                    "CropVarietyName", cropVariety2.getName(),
                                    "CV_CropSubClassId_Ref", cropVariety2.getSubClassId(),
                                    "CV_CSC_UUId_Ref", cropSubClass2.getUuId().toString(),
                                    "ODX_CropVariety_Uri", cropVariety2.getUri(),
                                    "ODX_CropVariety_UUId", cropVariety2.getUuId().toString()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUploadCropDescriptionsAsBatchCorrectly() {
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1",
                "testCropClassId1", "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2",
                "testCropClassId2", "testCropSubClassFaoId2", "testCropSubClassName2");

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

    @Test
    void testUploadFertilizersAsBatchCorrectly() {
        Fertilizers fertilizer1 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId1",
                "testFertilizersName1", "testFertilizersFamily1", "testFertilizersType1", "testTrue",
                "testFertilizersDryMatter1", "testFertilizersSpreaderLoss", "testFertilizersDensity1")
                .n("testN1")
                .nUnitId("testNUnitId1")
                .p("testP1")
                .pUnitId("testPUnitId1")
                .k("testK1")
                .kUnitId("testKUnitId1")
                .mg("testMg1")
                .mgUnitId("testMgUnitId1")
                .s("testS1")
                .sUnitId("testSUnitId1")
                .ca("testCa1")
                .caUnitId("testCaUnitId1")
                .b("testB1")
                .bUnitId("testBUnitId1")
                .zn("testZn1")
                .znUnitId("testZnUnitId1")
                .mn("testMn1")
                .mnUnitId("testMnUnitId1")
                .cu("testCu1")
                .cuUnitId("testCuUnitId1")
                .fe("testFe1")
                .feUnitId("testFeUnitId1")
                .mo("testMo1")
                .moUnitId("testMoUnitId1")
                .na("testNa1")
                .naUnitId("testNaUnitId1")
                .se("testSe1")
                .seUnitId("testSeUnitId1")
                .co("testCo1")
                .coUnitId("testCoUnitId1")
                .no3("testNO3_1")
                .nh4("testNH4_1")
                .urea("testUrea1")
                .utilizationN("testUtilizationN1")
                .utilizationNh4("testUtilizationNH4_1")
                .tank("testTank1")
                .electricalConductivity("testElectricalConductivity1")
                .pH("testPh1")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode1")
                .syncId("testSyncId1")
                .syncSource("testSyncSource1")
                .lastSync("testLastSync1")
                .build();
        Fertilizers fertilizer2 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId2",
                "testFertilizersName2", "testFertilizersFamily2", "testFertilizersType2", "testTrue",
                "testFertilizersDryMatter2", "testFertilizersSpreaderLoss", "testFertilizersDensity2")
                .n("testN2")
                .nUnitId("testNUnitId2")
                .p("testP2")
                .pUnitId("testPUnitId2")
                .k("testK2")
                .kUnitId("testKUnitId2")
                .mg("testMg2")
                .mgUnitId("testMgUnitId2")
                .s("testS2")
                .sUnitId("testSUnitId2")
                .ca("testCa2")
                .caUnitId("testCaUnitId2")
                .b("testB2")
                .bUnitId("testBUnitId2")
                .zn("testZn2")
                .znUnitId("testZnUnitId2")
                .mn("testMn2")
                .mnUnitId("testMnUnitId2")
                .cu("testCu2")
                .cuUnitId("testCuUnitId2")
                .fe("testFe2")
                .feUnitId("testFeUnitId2")
                .mo("testMo2")
                .moUnitId("testMoUnitId2")
                .na("testNa2")
                .naUnitId("testNaUnitId2")
                .se("testSe2")
                .seUnitId("testSeUnitId2")
                .co("testCo2")
                .coUnitId("testCoUnitId2")
                .no3("testNO3_2")
                .nh4("testNH4_2")
                .urea("testUrea2")
                .utilizationN("testUtilizationN2")
                .utilizationNh4("testUtilizationNH4_2")
                .tank("testTank2")
                .electricalConductivity("testElectricalConductivity2")
                .pH("testPh2")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode2")
                .syncId("testSyncId2")
                .syncSource("testSyncSource2")
                .lastSync("testLastSync2")
                .build();

        List<Fertilizers> fertilizers = Arrays.asList(fertilizer1, fertilizer2);

        uploader.uploadFertilizersAsBatch(fertilizers);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (n:Fertilizers) RETURN n ORDER BY n.FertilizersId");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Value node = record.get("n");
                        return node.asNode().asMap(String::valueOf);
                    })
                    .containsExactly(
                            Values.parameters(
                                    "B", fertilizer1.getB(),
                                    "BUnitId", fertilizer1.getBUnitId(),
                                    "Ca", fertilizer1.getCa(),
                                    "CaUnitId", fertilizer1.getCaUnitId(),
                                    "Co", fertilizer1.getCo(),
                                    "CoUnitId", fertilizer1.getCoUnitId(),
                                    "Cu", fertilizer1.getCu(),
                                    "CuUnitId", fertilizer1.getCuUnitId(),
                                    "Density", fertilizer1.getDensity(),
                                    "DhCode", fertilizer1.getDhCode(),
                                    "DryMatter", fertilizer1.getDryMatter(),
                                    "ElectricalConductivity", fertilizer1.getElectricalConductivity(),
                                    "Fe", fertilizer1.getFe(),
                                    "FeUnitId", fertilizer1.getFeUnitId(),
                                    "K", fertilizer1.getK(),
                                    "KUnitId", fertilizer1.getKUnitId(),
                                    "LastSync", fertilizer1.getLastSync(),
                                    "LowChloride", fertilizer1.getLowChloride(),
                                    "Mg", fertilizer1.getMg(),
                                    "MgUnitId", fertilizer1.getMgUnitId(),
                                    "Mn", fertilizer1.getMn(),
                                    "MnUnitId", fertilizer1.getMnUnitId(),
                                    "Mo", fertilizer1.getMo(),
                                    "MoUnitId", fertilizer1.getMoUnitId(),
                                    "N", fertilizer1.getN(),
                                    "NUnitId", fertilizer1.getNUnitId(),
                                    "Na", fertilizer1.getNa(),
                                    "NaUnitId", fertilizer1.getNaUnitId(),
                                    "NH4", fertilizer1.getNh4(),
                                    "NO3", fertilizer1.getNo3(),
                                    "ODX_Fert_SourceSystem", fertilizer1.getSource(),
                                    "ODX_Fertilizer_Uri", "ODX/Fertilizers/".concat(fertilizer1.getUuId().toString()),
                                    "ODX_Fertilizer_UUId", fertilizer1.getUuId().toString(),
                                    "P", fertilizer1.getP(),
                                    "PUnitId", fertilizer1.getPUnitId(),
                                    "Ph", fertilizer1.getPh(),
                                    "ProdFamily", fertilizer1.getFamily(),
                                    "ProdName", fertilizer1.getName(),
                                    "ProductId", fertilizer1.getId(),
                                    "ProductType", fertilizer1.getType(),
                                    "S", fertilizer1.getS(),
                                    "SUnitId", fertilizer1.getSUnitId(),
                                    "Se", fertilizer1.getSe(),
                                    "SeUnitId", fertilizer1.getSeUnitId(),
                                    "Solubility20C", fertilizer1.getSolubility20C(),
                                    "Solubility5C", fertilizer1.getSolubility5C(),
                                    "SpreaderLoss", fertilizer1.getSpreaderLoss(),
                                    "SyncId", fertilizer1.getSyncId(),
                                    "SyncSource", fertilizer1.getSyncSource(),
                                    "Tank", fertilizer1.getTank(),
                                    "Urea", fertilizer1.getUrea(),
                                    "UtilizationN", fertilizer1.getUtilizationN(),
                                    "UtilizationNH4", fertilizer1.getUtilizationNh4(),
                                    "Zn", fertilizer1.getZn(),
                                    "ZnUnitId", fertilizer1.getZnUnitId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "B", fertilizer2.getB(),
                                    "BUnitId", fertilizer2.getBUnitId(),
                                    "Ca", fertilizer2.getCa(),
                                    "CaUnitId", fertilizer2.getCaUnitId(),
                                    "Co", fertilizer2.getCo(),
                                    "CoUnitId", fertilizer2.getCoUnitId(),
                                    "Cu", fertilizer2.getCu(),
                                    "CuUnitId", fertilizer2.getCuUnitId(),
                                    "Density", fertilizer2.getDensity(),
                                    "DhCode", fertilizer2.getDhCode(),
                                    "DryMatter", fertilizer2.getDryMatter(),
                                    "ElectricalConductivity", fertilizer2.getElectricalConductivity(),
                                    "Fe", fertilizer2.getFe(),
                                    "FeUnitId", fertilizer2.getFeUnitId(),
                                    "K", fertilizer2.getK(),
                                    "KUnitId", fertilizer2.getKUnitId(),
                                    "LastSync", fertilizer2.getLastSync(),
                                    "LowChloride", fertilizer2.getLowChloride(),
                                    "Mg", fertilizer2.getMg(),
                                    "MgUnitId", fertilizer2.getMgUnitId(),
                                    "Mn", fertilizer2.getMn(),
                                    "MnUnitId", fertilizer2.getMnUnitId(),
                                    "Mo", fertilizer2.getMo(),
                                    "MoUnitId", fertilizer2.getMoUnitId(),
                                    "N", fertilizer2.getN(),
                                    "NUnitId", fertilizer2.getNUnitId(),
                                    "Na", fertilizer2.getNa(),
                                    "NaUnitId", fertilizer2.getNaUnitId(),
                                    "NH4", fertilizer2.getNh4(),
                                    "NO3", fertilizer2.getNo3(),
                                    "ODX_Fert_SourceSystem", fertilizer2.getSource(),
                                    "ODX_Fertilizer_Uri", "ODX/Fertilizers/".concat(fertilizer2.getUuId().toString()),
                                    "ODX_Fertilizer_UUId", fertilizer2.getUuId().toString(),
                                    "P", fertilizer2.getP(),
                                    "PUnitId", fertilizer2.getPUnitId(),
                                    "Ph", fertilizer2.getPh(),
                                    "ProdFamily", fertilizer2.getFamily(),
                                    "ProdName", fertilizer2.getName(),
                                    "ProductId", fertilizer2.getId(),
                                    "ProductType", fertilizer2.getType(),
                                    "S", fertilizer2.getS(),
                                    "SUnitId", fertilizer2.getSUnitId(),
                                    "Se", fertilizer2.getSe(),
                                    "SeUnitId", fertilizer2.getSeUnitId(),
                                    "Solubility20C", fertilizer2.getSolubility20C(),
                                    "Solubility5C", fertilizer2.getSolubility5C(),
                                    "SpreaderLoss", fertilizer2.getSpreaderLoss(),
                                    "SyncId", fertilizer2.getSyncId(),
                                    "SyncSource", fertilizer2.getSyncSource(),
                                    "Tank", fertilizer2.getTank(),
                                    "Urea", fertilizer2.getUrea(),
                                    "UtilizationN", fertilizer2.getUtilizationN(),
                                    "UtilizationNH4", fertilizer2.getUtilizationNh4(),
                                    "Zn", fertilizer2.getZn(),
                                    "ZnUnitId", fertilizer2.getZnUnitId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testCountryRegionRelationsUploadCorrectly() {
        Country country1 = new Country("testSource", "Country", "testId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");

        Region region1 = new Region("testSource", "Region", "testRegionId1", country1.getId(), "testRegionName1");
        Region region2 = new Region("testSource", "Region", "testRegionId2", country2.getId(), "testRegionName2");

        List<Region> regions = Arrays.asList(region1, region2);
        List<Country> countries = Arrays.asList(country1, country2);

        uploader.uploadCountriesAsBatch(countries);
        uploader.uploadRegionsAsBatch(regions, countries);
        uploader.createCountryToRegionRelations(countries, regions);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:Country)-[p:hasRegion]->(o:Region) RETURN s,p,o ORDER BY s.CountryId\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CountryId", subjectMap.get("CountryId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("RegionId", objectMap.get("RegionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CountryId", country1.getId(),
                                    "relationshipType", "hasRegion",
                                    "RegionId", region1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CountryId", country2.getId(),
                                    "relationshipType", "hasRegion",
                                    "RegionId", region2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testGroupClassRelationsUploadCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);

        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.createCropGroupToClassRelations(cropGroups, cropClasses);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropGroup)-[p:hasCropClass]->(o:CropClass) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropGroupId", subjectMap.get("CropGroupId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("CropClassId", objectMap.get("CropClassId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropGroupId", cropGroup1.getId(),
                                    "relationshipType", "hasCropClass",
                                    "CropClassId", cropClass1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropGroupId", cropGroup2.getId(),
                                    "relationshipType", "hasCropClass",
                                    "CropClassId", cropClass2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testClassSubClassRelationsUploadCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);

        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.createCropClassToSubClassRelations(cropClasses, cropSubClasses);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropClass)-[p:hasCropSubClass]->(o:CropSubClass) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropClassId", subjectMap.get("CropClassId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("CropSubClassId", objectMap.get("CropSubClassId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropClassId", cropClass1.getId(),
                                    "relationshipType", "hasCropSubClass",
                                    "CropSubClassId", cropSubClass1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropClassId", cropClass2.getId(),
                                    "relationshipType", "hasCropSubClass",
                                    "CropSubClassId", cropSubClass2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testSubClassVarietyRelationsUploadCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");
        CropVariety cropVariety1 = new CropVariety("testSource", "CropVariety", "testCropVarietyId1", cropSubClass1.getId(),
                "testCropVarietyName1");
        CropVariety cropVariety2 = new CropVariety("testSource", "CropVariety", "testCropVarietyId2", cropSubClass2.getId(),
                "testCropVarietyName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropVariety> cropVarieties = Arrays.asList(cropVariety1, cropVariety2);

        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.uploadCropVarietiesAsBatch(cropVarieties, cropSubClasses);
        uploader.createCropSubClassToVarietyRelations(cropSubClasses, cropVarieties);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropSubClass)-[p:hasCropVariety]->(o:CropVariety) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropSubClassId", subjectMap.get("CropSubClassId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("CropVarietyId", objectMap.get("CropVarietyId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropSubClassId", cropSubClass1.getId(),
                                    "relationshipType", "hasCropVariety",
                                    "CropVarietyId", cropVariety1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropSubClassId", cropSubClass2.getId(),
                                    "relationshipType", "hasCropVariety",
                                    "CropVarietyId", cropVariety2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testSubClassDescriptionRelationsUploadCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");
        CropDescription cropDescription1 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId1",
                cropSubClass1.getId(), "testTrue", "testCropDescriptionMediaUri1", "testCropDescriptionName1");
        CropDescription cropDescription2 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId2",
                cropSubClass2.getId(), "testFalse", "testCropDescriptionMediaUri2", "testCropDescriptionName2");

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropDescription> cropDescriptions = Arrays.asList(cropDescription1, cropDescription2);

        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.uploadCropDescriptionsAsBatch(cropDescriptions, cropSubClasses);
        uploader.createCropSubClassToDescriptionRelations(cropSubClasses, cropDescriptions);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropSubClass)-[p:hasCropDescription]->(o:CropDescription) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropSubClassId", subjectMap.get("CropSubClassId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("CropDescriptionId", objectMap.get("CropDescriptionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropSubClassId", cropSubClass1.getId(),
                                    "relationshipType", "hasCropDescription",
                                    "CropDescriptionId", cropDescription1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropSubClassId", cropSubClass2.getId(),
                                    "relationshipType", "hasCropDescription",
                                    "CropDescriptionId", cropDescription2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testVarietyDescriptionRelationsUploadCorrectly() {
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");
        CropVariety cropVariety1 = new CropVariety("testSource", "CropVariety", "testCropVarietyId1", cropSubClass1.getId(),
                "testCropVarietyName1");
        CropVariety cropVariety2 = new CropVariety("testSource", "CropVariety", "testCropVarietyId2", cropSubClass2.getId(),
                "testCropVarietyName2");
        CropDescription cropDescription1 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId1",
                cropSubClass1.getId(), "testTrue", "testCropDescriptionMediaUri1", "testCropDescriptionName1");
        CropDescription cropDescription2 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId2",
                cropSubClass2.getId(), "testFalse", "testCropDescriptionMediaUri2", "testCropDescriptionName2");
        CropDescriptionVariety cropDescVar1 = new CropDescriptionVariety("testCropDescVarId1", cropVariety1.getId(), cropDescription1.getId());
        CropDescriptionVariety cropDescVar2 = new CropDescriptionVariety("testCropDescVarId2", cropVariety2.getId(), cropDescription2.getId());

        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropVariety> cropVarieties = Arrays.asList(cropVariety1, cropVariety2);
        List<CropDescription> cropDescriptions = Arrays.asList(cropDescription1, cropDescription2);
        List<CropDescriptionVariety> cropDescVars = Arrays.asList(cropDescVar1, cropDescVar2);

        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.uploadCropVarietiesAsBatch(cropVarieties, cropSubClasses);
        uploader.uploadCropDescriptionsAsBatch(cropDescriptions, cropSubClasses);
        uploader.createCropVarietyToDescriptionRelations(cropVarieties, cropDescriptions, cropDescVars);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropVariety)-[p:hasCropDescription]->(o:CropDescription) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropVarietyId", subjectMap.get("CropVarietyId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("CropDescriptionId", objectMap.get("CropDescriptionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropVarietyId", cropVariety1.getId(),
                                    "relationshipType", "hasCropDescription",
                                    "CropDescriptionId", cropDescription1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropVarietyId", cropVariety2.getId(),
                                    "relationshipType", "hasCropDescription",
                                    "CropDescriptionId", cropDescription2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testScaleStagesRelationsUploadCorrectly() {
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

        uploader.uploadGrowthScales(growthScales);
        uploader.uploadGrowthScaleStagesAsBatch(growthScaleStages, growthScales);

        uploader.createGrowthScaleToStagesRelations(growthScales, growthScaleStages);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:GrowthScale)-[p:hasGrowthScaleStages]->(o:GrowthScaleStages) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("GrowthScaleId", subjectMap.get("GrowthScaleId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("GrowthScaleStageId", objectMap.get("GrowthScaleStageId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "GrowthScaleId", growthScale1.getId(),
                                    "relationshipType", "hasGrowthScaleStages",
                                    "GrowthScaleStageId", scaleStage1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "GrowthScaleId", growthScale2.getId(),
                                    "relationshipType", "hasGrowthScaleStages",
                                    "GrowthScaleStageId", scaleStage2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testDescriptionRegionsRelationsUploadCorrectly() {
        Country country1 = new Country("testSource", "Country", "testCountryId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testCountryId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");
        Region region1 = new Region("testSource", "Region", "testRegionId1", "testCountryId1", "testRegionName1");
        Region region2 = new Region("testSource", "Region", "testRegionId2", "testCountryId2", "testRegionName2");
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");
        CropVariety cropVariety1 = new CropVariety("testSource", "CropVariety", "testCropVarietyId1", cropSubClass1.getId(),
                "testCropVarietyName1");
        CropVariety cropVariety2 = new CropVariety("testSource", "CropVariety", "testCropVarietyId2", cropSubClass2.getId(),
                "testCropVarietyName2");
        CropDescription cropDescription1 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId1",
                cropSubClass1.getId(), "testTrue", "testCropDescriptionMediaUri1", "testCropDescriptionName1");
        CropDescription cropDescription2 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId2",
                cropSubClass2.getId(), "testFalse", "testCropDescriptionMediaUri2", "testCropDescriptionName2");
        CropDescriptionVariety cropDescVar1 = new CropDescriptionVariety("testCropDescVarId1", cropVariety1.getId(), cropDescription1.getId());
        CropDescriptionVariety cropDescVar2 = new CropDescriptionVariety("testCropDescVarId2", cropVariety2.getId(), cropDescription2.getId());
        CropRegion cropRegion1 = new CropRegion("testCropRegionId1", cropDescription1.getId(), country1.getId(), region1.getId(),
                "testGrowthScaleId1", "testDefaultSeedingDate1", "testDefaultHarvestDate1",
                "testDefaultYield1", "testYieldBaseUnitId1", "testDemandBaseUnitId1",
                "testAdditionalProperties1");
        CropRegion cropRegion2 = new CropRegion("testCropRegionId2", cropDescription2.getId(), country2.getId(), region2.getId(),
                "testGrowthScaleId2", "testDefaultSeedingDate2", "testDefaultHarvestDate2",
                "testDefaultYield2", "testYieldBaseUnitId2", "testDemandBaseUnitId2",
                "testAdditionalProperties2");

        List<Region> regions = Arrays.asList(region1, region2);
        List<Country> countries = Arrays.asList(country1, country2);
        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropVariety> cropVarieties = Arrays.asList(cropVariety1, cropVariety2);
        List<CropDescription> cropDescriptions = Arrays.asList(cropDescription1, cropDescription2);
        List<CropDescriptionVariety> cropDescVars = Arrays.asList(cropDescVar1, cropDescVar2);
        List<CropRegion> cropRegions = Arrays.asList(cropRegion1, cropRegion2);

        uploader.uploadCountriesAsBatch(countries);
        uploader.uploadRegionsAsBatch(regions, countries);
        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.uploadCropVarietiesAsBatch(cropVarieties, cropSubClasses);
        uploader.uploadCropDescriptionsAsBatch(cropDescriptions, cropSubClasses);

        uploader.createCountryToRegionRelations(countries, regions);
        uploader.createCropGroupToClassRelations(cropGroups, cropClasses);
        uploader.createCropClassToSubClassRelations(cropClasses, cropSubClasses);
        uploader.createCropSubClassToVarietyRelations(cropSubClasses, cropVarieties);
        uploader.createCropSubClassToDescriptionRelations(cropSubClasses, cropDescriptions);
        uploader.createCropVarietyToDescriptionRelations(cropVarieties, cropDescriptions, cropDescVars);
        uploader.createCropDescriptionsToRegionsRelations(cropDescriptions, regions, cropRegions);

        Map<String, String> relProp1 = new HashMap() {{
            put("CD_GrowthScaleId_Ref", cropRegion1.getGrowthScaleIdRef());
            put("CD_CountryIdRef", cropRegion1.getCountryIdRef());
            put("DefaultSeedingDate", cropRegion1.getDefaultSeedingDate());
            put("YieldBaseUnitId", cropRegion1.getYieldBaseUnitId());
            put("DefaultYield", cropRegion1.getDefaultYield());
            put("CD_RegionIdRef", cropRegion1.getRegionIdRef());
            put("AdditionalProperties", cropRegion1.getAdditionalProperties());
            put("DefaultHarvestDate", cropRegion1.getDefaultHarvestDate());
            put("DemandBaseUnitId", cropRegion1.getDemandBaseUnitId());
        }};
        Map<String, String> relProp2 = new HashMap() {{
            put("CD_GrowthScaleId_Ref", cropRegion2.getGrowthScaleIdRef());
            put("CD_CountryIdRef", cropRegion2.getCountryIdRef());
            put("DefaultSeedingDate", cropRegion2.getDefaultSeedingDate());
            put("YieldBaseUnitId", cropRegion2.getYieldBaseUnitId());
            put("DefaultYield", cropRegion2.getDefaultYield());
            put("CD_RegionIdRef", cropRegion2.getRegionIdRef());
            put("AdditionalProperties", cropRegion2.getAdditionalProperties());
            put("DefaultHarvestDate", cropRegion2.getDefaultHarvestDate());
            put("DemandBaseUnitId", cropRegion2.getDemandBaseUnitId());
        }};

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropDescription)-[p:isAvailableIn]->(o:Region) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropDescriptionId", subjectMap.get("CropDescriptionId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("relationshipProperties", "\"".concat(
                                predicate.asRelationship()
                                        .asMap(v -> String.valueOf(v)
                                                .replace("\"", ""))
                                        .toString())
                                .concat("\""));
                        map.put("RegionId", objectMap.get("RegionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropDescriptionId", cropDescription1.getId(),
                                    "relationshipType", "isAvailableIn",
                                    "relationshipProperties", relProp1.toString(),
                                    "RegionId", region1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropDescriptionId", cropDescription2.getId(),
                                    "relationshipType", "isAvailableIn",
                                    "relationshipProperties", relProp2.toString(),
                                    "RegionId", region2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testDescriptionGrowthScalesRelationsUploadCorrectly() {
        Country country1 = new Country("testSource", "Country", "testCountryId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testCountryId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");
        Region region1 = new Region("testSource", "Region", "testRegionId1", "testCountryId1", "testRegionName1");
        Region region2 = new Region("testSource", "Region", "testRegionId2", "testCountryId2", "testRegionName2");
        CropGroup cropGroup1 = new CropGroup("testSource", "CropGroup", "testCropGroupId1",
                "testCropGroupFaoId1", "testCropGroupName1");
        CropGroup cropGroup2 = new CropGroup("testSource", "CropGroup", "testCropGroupId2",
                "testCropGroupFaoId2", "testCropGroupName2");
        CropClass cropClass1 = new CropClass("testSource", "CropClass", "testCropClassId1", cropGroup1.getId(),
                "testCropClassFaoId1", "testCropClassName1");
        CropClass cropClass2 = new CropClass("testSource", "CropClass", "testCropClassId2", cropGroup2.getId(),
                "testCropClassFaoId2", "testCropClassName2");
        CropSubClass cropSubClass1 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId1", cropClass1.getId(),
                "testCropSubClassFaoId1", "testCropSubClassName1");
        CropSubClass cropSubClass2 = new CropSubClass("testSource", "CropSubClass", "testCropSubClassId2", cropClass2.getId(),
                "testCropSubClassFaoId2", "testCropSubClassName2");
        CropVariety cropVariety1 = new CropVariety("testSource", "CropVariety", "testCropVarietyId1", cropSubClass1.getId(),
                "testCropVarietyName1");
        CropVariety cropVariety2 = new CropVariety("testSource", "CropVariety", "testCropVarietyId2", cropSubClass2.getId(),
                "testCropVarietyName2");
        CropDescription cropDescription1 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId1",
                cropSubClass1.getId(), "testTrue", "testCropDescriptionMediaUri1", "testCropDescriptionName1");
        CropDescription cropDescription2 = new CropDescription("testSource", "CropDescription", "testCropDescriptionId2",
                cropSubClass2.getId(), "testFalse", "testCropDescriptionMediaUri2", "testCropDescriptionName2");
        CropDescriptionVariety cropDescVar1 = new CropDescriptionVariety("testCropDescVarId1", cropVariety1.getId(), cropDescription1.getId());
        CropDescriptionVariety cropDescVar2 = new CropDescriptionVariety("testCropDescVarId2", cropVariety2.getId(), cropDescription2.getId());
        CropRegion cropRegion1 = new CropRegion("testCropRegionId1", cropDescription1.getId(), country1.getId(), region1.getId(),
                "testGrowthScaleId1", "testDefaultSeedingDate1", "testDefaultHarvestDate1",
                "testDefaultYield1", "testYieldBaseUnitId1", "testDemandBaseUnitId1",
                "testAdditionalProperties1");
        CropRegion cropRegion2 = new CropRegion("testCropRegionId2", cropDescription2.getId(), country2.getId(), region2.getId(),
                "testGrowthScaleId2", "testDefaultSeedingDate2", "testDefaultHarvestDate2",
                "testDefaultYield2", "testYieldBaseUnitId2", "testDemandBaseUnitId2",
                "testAdditionalProperties2");
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

        List<Region> regions = Arrays.asList(region1, region2);
        List<Country> countries = Arrays.asList(country1, country2);
        List<CropGroup> cropGroups = Arrays.asList(cropGroup1, cropGroup2);
        List<CropClass> cropClasses = Arrays.asList(cropClass1, cropClass2);
        List<CropSubClass> cropSubClasses = Arrays.asList(cropSubClass1, cropSubClass2);
        List<CropVariety> cropVarieties = Arrays.asList(cropVariety1, cropVariety2);
        List<CropDescription> cropDescriptions = Arrays.asList(cropDescription1, cropDescription2);
        List<CropDescriptionVariety> cropDescVars = Arrays.asList(cropDescVar1, cropDescVar2);
        List<CropRegion> cropRegions = Arrays.asList(cropRegion1, cropRegion2);
        List<GrowthScale> growthScales = Arrays.asList(growthScale1, growthScale2);
        List<GrowthScaleStages> growthScaleStages = Arrays.asList(scaleStage1, scaleStage2);

        uploader.uploadCountriesAsBatch(countries);
        uploader.uploadRegionsAsBatch(regions, countries);
        uploader.uploadCropGroupsAsBatch(cropGroups);
        uploader.uploadCropClassAsBatch(cropClasses, cropGroups);
        uploader.uploadCropSubClassesAsBatch(cropSubClasses, cropClasses);
        uploader.uploadCropVarietiesAsBatch(cropVarieties, cropSubClasses);
        uploader.uploadCropDescriptionsAsBatch(cropDescriptions, cropSubClasses);
        uploader.uploadGrowthScales(growthScales);
        uploader.uploadGrowthScaleStagesAsBatch(growthScaleStages, growthScales);

        uploader.createCountryToRegionRelations(countries, regions);
        uploader.createCropGroupToClassRelations(cropGroups, cropClasses);
        uploader.createCropClassToSubClassRelations(cropClasses, cropSubClasses);
        uploader.createCropSubClassToVarietyRelations(cropSubClasses, cropVarieties);
        uploader.createCropSubClassToDescriptionRelations(cropSubClasses, cropDescriptions);
        uploader.createCropVarietyToDescriptionRelations(cropVarieties, cropDescriptions, cropDescVars);
        uploader.createCropDescriptionsToRegionsRelations(cropDescriptions, regions, cropRegions);
        uploader.createCropDescriptionsToGrowthScaleRelations(cropDescriptions, growthScales, cropRegions);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:CropDescription)-[p:hasGrowthScale]->(o:GrowthScale) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("CropDescriptionId", subjectMap.get("CropDescriptionId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("GrowthScaleId", objectMap.get("GrowthScaleId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "CropDescriptionId", cropDescription1.getId(),
                                    "relationshipType", "hasGrowthScale",
                                    "GrowthScaleId", growthScale1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "CropDescriptionId", cropDescription2.getId(),
                                    "relationshipType", "hasGrowthScale",
                                    "GrowthScaleId", growthScale2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testNutrientUnitsRelationsUploadCorrectly() {
        Units unit1 = new Units("testSource", "Units", "testUnitsId1", "testUnitsName1", "testUnitsTag1");
        Units unit2 = new Units("testSource", "Units", "testUnitsId2", "testUnitsName2", "testUnitsTag2");
        Nutrient nutrient1 = new Nutrient("testSource", "Nutrient", "testNutrientId1",
                "testNutrientName1", unit1.getName(), "testNutrientOrdinal1");
        Nutrient nutrient2 = new Nutrient("testSource", "Nutrient", "testNutrientId2",
                "testNutrientName2", unit2.getName(), "testNutrientOrdinal2");

        List<Nutrient> nutrients = Arrays.asList(nutrient1, nutrient2);
        List<Units> units = Arrays.asList(unit1, unit2);

        uploader.uploadNutrientsAsBatch(nutrients);
        uploader.uploadUnitsAsBatch(units);
        uploader.createNutrientsToUnitsRelations(nutrients, units);


        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:Nutrient)-[p:hasNutrientUnit]->(o:Units) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("NutrientId", subjectMap.get("NutrientId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("UnitsId", objectMap.get("UnitsId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "NutrientId", nutrient1.getId(),
                                    "relationshipType", "hasNutrientUnit",
                                    "UnitsId", unit1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "NutrientId", nutrient2.getId(),
                                    "relationshipType", "hasNutrientUnit",
                                    "UnitsId", unit2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testUnitConversionsRelationsUploadCorrectly() {
        Units units1 = new Units("testSource", "Units", "testUnitsId1", "testUnitsName1", "testUnitsTag1");
        Units units2 = new Units("testSource", "Units", "testUnitsId2", "testUnitsName2", "testUnitsTag2");
        UnitConversion unitConversion1 = new UnitConversion("testSource", "UnitConversion", "testUnitConversionId1",
                units1.getId(), units2.getId(), "testMultiplier1", "testCountryIdRef1");
        UnitConversion unitConversion2 = new UnitConversion("testSource", "UnitConversion", "testUnitConversionId2",
                units2.getId(), units1.getId(), "testMultiplier2", "testCountryIdRef2");

        List<Units> units = Arrays.asList(units1, units2);
        List<UnitConversion> unitConversions = Arrays.asList(unitConversion1, unitConversion2);

        uploader.uploadUnitsAsBatch(units);
        uploader.uploadUnitConversionsAsBatch(unitConversions, units);
        uploader.createUnitsToConversionsRelations(units, unitConversions);


        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:Units)-[p:hasUnitConversion]->(o:UnitConversion) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("UnitsId", subjectMap.get("UnitsId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("UnitConversionId", objectMap.get("UnitConversionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "UnitsId", units1.getId(),
                                    "relationshipType", "hasUnitConversion",
                                    "UnitConversionId", unitConversion1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "UnitsId", units2.getId(),
                                    "relationshipType", "hasUnitConversion",
                                    "UnitConversionId", unitConversion2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testFertilizersRegionsRelationsUploadCorrectly() {
        Country country1 = new Country("testSource", "Country", "testCountryId1", "testName1", "test_fips1",
                "test_iso2Code1", "test_iso3Code1", "test_m49Code1", "test_continentalSectionUuidRef1",
                "testProductSetCode1", "test_un1");
        Country country2 = new Country("testSource", "Country", "testCountryId2", "testName2", "test_fips2",
                "test_iso2Code2", "test_iso3Code2", "test_m49Code2", "test_continentalSectionUuidRef2",
                "testProductSetCode2", "test_un2");
        Region region1 = new Region("testSource", "Region", "testRegionId1", country1.getId(), "testRegionName1");
        Region region2 = new Region("testSource", "Region", "testRegionId2", country2.getId(), "testRegionName2");
        Fertilizers fertilizer1 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId1",
                "testFertilizersName1", "testFertilizersFamily1", "testFertilizersType1", "testTrue",
                "testFertilizersDryMatter1", "testFertilizersSpreaderLoss", "testFertilizersDensity1")
                .n("testN1")
                .nUnitId("testNUnitId1")
                .p("testP1")
                .pUnitId("testPUnitId1")
                .k("testK1")
                .kUnitId("testKUnitId1")
                .mg("testMg1")
                .mgUnitId("testMgUnitId1")
                .s("testS1")
                .sUnitId("testSUnitId1")
                .ca("testCa1")
                .caUnitId("testCaUnitId1")
                .b("testB1")
                .bUnitId("testBUnitId1")
                .zn("testZn1")
                .znUnitId("testZnUnitId1")
                .mn("testMn1")
                .mnUnitId("testMnUnitId1")
                .cu("testCu1")
                .cuUnitId("testCuUnitId1")
                .fe("testFe1")
                .feUnitId("testFeUnitId1")
                .mo("testMo1")
                .moUnitId("testMoUnitId1")
                .na("testNa1")
                .naUnitId("testNaUnitId1")
                .se("testSe1")
                .seUnitId("testSeUnitId1")
                .co("testCo1")
                .coUnitId("testCoUnitId1")
                .no3("testNO3_1")
                .nh4("testNH4_1")
                .urea("testUrea1")
                .utilizationN("testUtilizationN1")
                .utilizationNh4("testUtilizationNH4_1")
                .tank("testTank1")
                .electricalConductivity("testElectricalConductivity1")
                .pH("testPh1")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode1")
                .syncId("testSyncId1")
                .syncSource("testSyncSource1")
                .lastSync("testLastSync1")
                .build();
        Fertilizers fertilizer2 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId2",
                "testFertilizersName2", "testFertilizersFamily2", "testFertilizersType2", "testTrue",
                "testFertilizersDryMatter2", "testFertilizersSpreaderLoss", "testFertilizersDensity2")
                .n("testN2")
                .nUnitId("testNUnitId2")
                .p("testP2")
                .pUnitId("testPUnitId2")
                .k("testK2")
                .kUnitId("testKUnitId2")
                .mg("testMg2")
                .mgUnitId("testMgUnitId2")
                .s("testS2")
                .sUnitId("testSUnitId2")
                .ca("testCa2")
                .caUnitId("testCaUnitId2")
                .b("testB2")
                .bUnitId("testBUnitId2")
                .zn("testZn2")
                .znUnitId("testZnUnitId2")
                .mn("testMn2")
                .mnUnitId("testMnUnitId2")
                .cu("testCu2")
                .cuUnitId("testCuUnitId2")
                .fe("testFe2")
                .feUnitId("testFeUnitId2")
                .mo("testMo2")
                .moUnitId("testMoUnitId2")
                .na("testNa2")
                .naUnitId("testNaUnitId2")
                .se("testSe2")
                .seUnitId("testSeUnitId2")
                .co("testCo2")
                .coUnitId("testCoUnitId2")
                .no3("testNO3_2")
                .nh4("testNH4_2")
                .urea("testUrea2")
                .utilizationN("testUtilizationN2")
                .utilizationNh4("testUtilizationNH4_2")
                .tank("testTank2")
                .electricalConductivity("testElectricalConductivity2")
                .pH("testPh2")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode2")
                .syncId("testSyncId2")
                .syncSource("testSyncSource2")
                .lastSync("testLastSync2")
                .build();
        FertilizerRegion fertilizerRegion1 = new FertilizerRegion("testFertilizerRegionId1", country1.getId(),
                region1.getId(), "testLocalizedName1", fertilizer1.getId(), "testTrue", "testAppTags1");
        FertilizerRegion fertilizerRegion2 = new FertilizerRegion("testFertilizerRegionId2", country2.getId(),
                region2.getId(), "testLocalizedName2", fertilizer2.getId(), "testTrue", "testAppTags2");

        List<Region> regions = Arrays.asList(region1, region2);
        List<Country> countries = Arrays.asList(country1, country2);
        List<Fertilizers> fertilizers = Arrays.asList(fertilizer1, fertilizer2);
        List<FertilizerRegion> fertilizerRegions = Arrays.asList(fertilizerRegion1, fertilizerRegion2);

        uploader.uploadCountriesAsBatch(countries);
        uploader.uploadRegionsAsBatch(regions, countries);
        uploader.uploadFertilizersAsBatch(fertilizers);
        uploader.createCountryToRegionRelations(countries, regions);
        uploader.createFertilizersToRegionsRelations(fertilizers, countries, regions, fertilizerRegions);

        Map<String, String> relProp1 = new HashMap() {{
            put("ProdCountry_UUId_Ref", country1.getUuId());
            put("Prod_CountryId_Ref", fertilizerRegion1.getCountryId());
            put("ApplicationTags", fertilizerRegion1.getApplicationTags());
            put("ProdRegion_UUId_Ref", region1.getUuId());
            put("Prod_RegionId_Ref", fertilizerRegion1.getRegionId());
            put("LocalizedName", fertilizerRegion1.getLocalizedName());
            put("IsAvailable", fertilizerRegion1.getIsAvailable());
        }};
        Map<String, String> relProp2 = new HashMap() {{
            put("ProdCountry_UUId_Ref", country2.getUuId());
            put("Prod_CountryId_Ref", fertilizerRegion2.getCountryId());
            put("ApplicationTags", fertilizerRegion2.getApplicationTags());
            put("ProdRegion_UUId_Ref", region2.getUuId());
            put("Prod_RegionId_Ref", fertilizerRegion2.getRegionId());
            put("LocalizedName", fertilizerRegion2.getLocalizedName());
            put("IsAvailable", fertilizerRegion2.getIsAvailable());
        }};

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:Fertilizers)-[p:isAvailableIn]->(o:Region) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("ProductId", subjectMap.get("ProductId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("relationshipProperties", "\"".concat(
                                predicate.asRelationship()
                                        .asMap(v -> String.valueOf(v)
                                                .replace("\"", ""))
                                        .toString())
                                .concat("\""));
                        map.put("RegionId", objectMap.get("RegionId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ProductId", fertilizer1.getId(),
                                    "relationshipType", "isAvailableIn",
                                    "relationshipProperties", relProp1.toString(),
                                    "RegionId", region1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ProductId", fertilizer2.getId(),
                                    "relationshipType", "isAvailableIn",
                                    "relationshipProperties", relProp2.toString(),
                                    "RegionId", region2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }

    @Test
    void testFertilizersNutrientsRelationsUploadCorrectly() {
        Units unit1 = new Units("testSource", "Units", "testUnitsId1", "testUnitsName1", "testUnitsTag1");
        Units unit2 = new Units("testSource", "Units", "testUnitsId2", "testUnitsName2", "testUnitsTag2");
        Nutrient nutrient1 = new Nutrient("testSource", "Nutrient", "testNutrientId1",
                "testNutrientName1", unit1.getTag(), "testNutrientOrdinal1");
        Nutrient nutrient2 = new Nutrient("testSource", "Nutrient", "testNutrientId2",
                "testNutrientName2", unit2.getTag(), "testNutrientOrdinal2");
        Fertilizers fertilizer1 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId1",
                "testFertilizersName1", "testFertilizersFamily1", "testFertilizersType1", "testTrue",
                "testFertilizersDryMatter1", "testFertilizersSpreaderLoss", "testFertilizersDensity1")
                .setNutrientUnitsContent(unit1.getId(), "1")
                .n("testN1")
                .nUnitId("testNUnitId1")
                .p("testP1")
                .pUnitId("testPUnitId1")
                .k("testK1")
                .kUnitId("testKUnitId1")
                .mg("testMg1")
                .mgUnitId("testMgUnitId1")
                .s("testS1")
                .sUnitId("testSUnitId1")
                .ca("testCa1")
                .caUnitId("testCaUnitId1")
                .b("testB1")
                .bUnitId("testBUnitId1")
                .zn("testZn1")
                .znUnitId("testZnUnitId1")
                .mn("testMn1")
                .mnUnitId("testMnUnitId1")
                .cu("testCu1")
                .cuUnitId("testCuUnitId1")
                .fe("testFe1")
                .feUnitId("testFeUnitId1")
                .mo("testMo1")
                .moUnitId("testMoUnitId1")
                .na("testNa1")
                .naUnitId("testNaUnitId1")
                .se("testSe1")
                .seUnitId("testSeUnitId1")
                .co("testCo1")
                .coUnitId("testCoUnitId1")
                .no3("testNO3_1")
                .nh4("testNH4_1")
                .urea("testUrea1")
                .utilizationN("testUtilizationN1")
                .utilizationNh4("testUtilizationNH4_1")
                .tank("testTank1")
                .electricalConductivity("testElectricalConductivity1")
                .pH("testPh1")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode1")
                .syncId("testSyncId1")
                .syncSource("testSyncSource1")
                .lastSync("testLastSync1")
                .build();
        Fertilizers fertilizer2 = new Fertilizers.Builder("testSource", "Fertilizers", "testFertilizersId2",
                "testFertilizersName2", "testFertilizersFamily2", "testFertilizersType2", "testTrue",
                "testFertilizersDryMatter2", "testFertilizersSpreaderLoss", "testFertilizersDensity2")
                .setNutrientUnitsContent(unit2.getId(), "2")
                .n("testN2")
                .nUnitId("testNUnitId2")
                .p("testP2")
                .pUnitId("testPUnitId2")
                .k("testK2")
                .kUnitId("testKUnitId2")
                .mg("testMg2")
                .mgUnitId("testMgUnitId2")
                .s("testS2")
                .sUnitId("testSUnitId2")
                .ca("testCa2")
                .caUnitId("testCaUnitId2")
                .b("testB2")
                .bUnitId("testBUnitId2")
                .zn("testZn2")
                .znUnitId("testZnUnitId2")
                .mn("testMn2")
                .mnUnitId("testMnUnitId2")
                .cu("testCu2")
                .cuUnitId("testCuUnitId2")
                .fe("testFe2")
                .feUnitId("testFeUnitId2")
                .mo("testMo2")
                .moUnitId("testMoUnitId2")
                .na("testNa2")
                .naUnitId("testNaUnitId2")
                .se("testSe2")
                .seUnitId("testSeUnitId2")
                .co("testCo2")
                .coUnitId("testCoUnitId2")
                .no3("testNO3_2")
                .nh4("testNH4_2")
                .urea("testUrea2")
                .utilizationN("testUtilizationN2")
                .utilizationNh4("testUtilizationNH4_2")
                .tank("testTank2")
                .electricalConductivity("testElectricalConductivity2")
                .pH("testPh2")
                .solubility5C("testSolubility5C")
                .solubility20C("testSolubility20C")
                .dhCode("testDhCode2")
                .syncId("testSyncId2")
                .syncSource("testSyncSource2")
                .lastSync("testLastSync2")
                .build();

        List<Fertilizers> fertilizers = Arrays.asList(fertilizer1, fertilizer2);
        List<Nutrient> nutrients = Arrays.asList(nutrient1, nutrient2);
        List<Units> units = Arrays.asList(unit1, unit2);

        uploader.uploadFertilizersAsBatch(fertilizers);
        uploader.uploadNutrientsAsBatch(nutrients);
        uploader.uploadUnitsAsBatch(units);
        uploader.createFertilizersToNutrientsRelations(fertilizers, nutrients, units);

        try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
             Session session = driver.session()
        ) {
            Result result = session.run("MATCH (s:Fertilizers)-[p:hasProdNutrient]->(o:Nutrient) RETURN s,p,o\n");
            //noinspection unchecked
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record -> {
                        Map<String, String> map = new HashMap<>();
                        Value subject = record.get("s");
                        Value predicate = record.get("p");
                        Value object = record.get("o");
                        Map<String, String> subjectMap = subject.asNode().asMap(String::valueOf);
                        Map<String, String> objectMap = object.asNode().asMap(String::valueOf);
                        map.put("ProductId", subjectMap.get("ProductId"));
                        map.put("relationshipType", "\"".concat(predicate.asRelationship().type()).concat("\""));
                        map.put("NutrientId", objectMap.get("NutrientId"));
                        return map;
                    })
                    .containsExactly(
                            Values.parameters(
                                    "ProductId", fertilizer1.getId(),
                                    "relationshipType", "hasProdNutrient",
                                    "NutrientId", nutrient1.getId()
                            ).asMap(String::valueOf),
                            Values.parameters(
                                    "ProductId", fertilizer2.getId(),
                                    "relationshipType", "hasProdNutrient",
                                    "NutrientId", nutrient2.getId()
                            ).asMap(String::valueOf)
                    );
        }
    }
}
