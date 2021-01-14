package com.yara.odx.loader;

import com.yara.odx.domain.*;
import com.yara.odx.reader.ShaclRulesReader;
import org.neo4j.driver.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PropertyGraphUploader implements AutoCloseable {

//    private static final String URI = "bolt+s://odx-storage.yara.com:7687";
//    private static final String USER = "neo4j";

//    Use valid pass for graph DB
//    private static final String PASSWORD = "XXXXXXXX";

    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "1234";

    public static final int BUILDER_LENGTH_THRESHOLD = 300_000;
    public static final int NODES_BATCH_SIZE = 25;
    public static final int RELATION_BATCH_SIZE = 25;

    private final Driver driver;

    public PropertyGraphUploader(java.net.URI uri, Config config) {
        driver = GraphDatabase.driver(uri, config);
    }

    public PropertyGraphUploader() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void uploadCountries(List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        String createCountryFormat = "CREATE (%s:%s{" +
                "ODX_Country_UUId: \"%s\", " +
                "ODX_Country_Uri: \"%s\", " +
                "CountryId: \"%s\", " +
                "CountryName: \"%s\", " +
                "ProductSetCode: \"%s\"})\n";
        try (Session session = driver.session()) {
            countries.forEach(country -> {
                if (!existsInDatabase(country)) {
                    System.out.println("Uploading Country # " + count.incrementAndGet());
                    session.writeTransaction(tx -> tx.run(String.format(createCountryFormat,
                            createNodeName(country.getName()), country.getClassName(),
                            country.getUuId(),
                            createOdxUri(country),
                            country.getId(),
                            country.getName(),
                            country.getProductSetCode())));
                }
            });
        }
        System.out.println("Country uploading completed");
        System.out.println(count.get() + " Countries uploaded");
//        reporter.writeStatisticsToFile(countries);
    }

    public void uploadCountriesAsBatch(List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createCountryFormat = "CREATE (%s:%s{" +
                "ODX_Country_UUId: \"%s\", " +
                "ODX_Country_Uri: \"%s\", " +
                "CountryId: \"%s\", " +
                "CountryName: \"%s\", " +
                "ProductSetCode: \"%s\"})\n";

        countries.forEach(country -> {
            count.incrementAndGet();
            String countryNodeName = createUniqueNodeName(country.getName(), Integer.toString(count.get()));
            String createCountryCommand = String.format(createCountryFormat,
                    countryNodeName, country.getClassName(),
                    country.getUuId(),
                    createOdxUri(country),
                    country.getId(),
                    country.getName(),
                    country.getProductSetCode());
            builder.append(createCountryCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), country.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " Countries uploaded");
    }

    public void uploadRegions(List<Region> regions, List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        String createRegionFormat = "CREATE (%s:%s{" +
                "ODX_Region_UUId: \"%s\", " +
                "ODX_Region_Uri: \"%s\", " +
                "RegionId: \"%s\", " +
                "Region_CountryId_Ref: \"%s\", " +
                "Region_Country_UUId_Ref: \"%s\", " +
                "RegionName: \"%s\"})\n";
        try (Session session = driver.session()) {
            regions.forEach(region -> {
                if (!existsInDatabase(region)) {
                    System.out.println("Uploading Region # " + count.incrementAndGet());
                    Country country = (Country) getFromCollectionById(countries, region.getCountryId());
                    session.writeTransaction(tx -> {
                        String newRegionName = createNodeName(region.getName());
                        return tx.run(String.format(createRegionFormat,
                                newRegionName, region.getClassName(),
                                region.getUuId(),
                                createOdxUri(region),
                                region.getId(),
                                region.getCountryId(),
                                country.getUuId(),
                                region.getName()));
                    });
                }
            });
        }
        System.out.println("Region uploading completed");
        System.out.println(count.get() + " Regions uploaded");
    }

    public void uploadRegionsAsBatch(List<Region> regions, List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createRegionFormat = "CREATE (%s:%s{" +
                "ODX_Region_UUId: \"%s\", " +
                "ODX_Region_Uri: \"%s\", " +
                "RegionId: \"%s\", " +
                "Region_CountryId_Ref: \"%s\", " +
                "Region_Country_UUId_Ref: \"%s\", " +
                "RegionName: \"%s\"})\n";

        regions.forEach(region -> {
            count.incrementAndGet();
            Country country = (Country) getFromCollectionById(countries, region.getCountryId());
            String regionNodeName = createUniqueNodeName(region.getName(), Integer.toString(count.get()));
            String createRegionCommand = String.format(createRegionFormat,
                    regionNodeName, region.getClassName(),
                    region.getUuId(),
                    createOdxUri(region),
                    region.getId(),
                    region.getCountryId(),
                    country.getUuId(),
                    region.getName());
            builder.append(createRegionCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), region.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " Regions uploaded");
    }

    public void uploadCropGroups(List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String createGroupFormat = "CREATE (%s:%s{" +
                "CG_FAOId: \"%s\", " +
                "CG_MediaUri: \"%s\", " +
                "CropGroupId: \"%s\", " +
                "CropGroupName: \"%s\", " +
                "ODX_CropGroup_Uri: \"%s\", " +
                "ODX_CropGroup_UUId: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropGroups.forEach(group -> session.writeTransaction(tx -> {
                System.out.println("Uploading CropGroup # " + count.incrementAndGet());
                String newGroupName = createNodeName(group.getName());
                return tx.run(String.format(createGroupFormat,
                        newGroupName, group.getClassName(),
                        group.getFaoId(),
                        group.getMediaUri(),
                        group.getId(),
                        group.getName(),
                        createOdxUri(group),
                        group.getUuId()));
            }));
        }
        System.out.println("CropGroup uploading completed");
        System.out.println(count.get() + " CropGroups uploaded");
    }

    public void uploadCropGroupsAsBatch(List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createGroupFormat = "CREATE (%s:%s{" +
                "CG_FAOId: \"%s\", " +
                "CG_MediaUri: \"%s\", " +
                "CropGroupId: \"%s\", " +
                "CropGroupName: \"%s\", " +
                "ODX_CropGroup_Uri: \"%s\", " +
                "ODX_CropGroup_UUId: \"%s\"})\n";

        cropGroups.forEach(group -> {
            count.incrementAndGet();
            String groupNodeName = createUniqueNodeName(group.getName(), Integer.toString(count.get()));
            String createGroupCommand = String.format(createGroupFormat,
                    groupNodeName, group.getClassName(),
                    group.getFaoId(),
                    group.getMediaUri(),
                    group.getId(),
                    group.getName(),
                    createOdxUri(group),
                    group.getUuId());
            builder.append(createGroupCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), group.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropGroups uploaded");
    }

    public void uploadCropClasses(List<CropClass> cropClasses, List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String createClassFormat = "CREATE (%s:%s{" +
                "ODX_CropClass_UUId: \"%s\", " +
                "ODX_CropClass_Uri: \"%s\", " +
                "CropClassId: \"%s\", " +
                "CropGroupId_Ref: \"%s\", " +
                "ODX_CG_UUId_Ref: \"%s\", " +
                "CC_FAOId: \"%s\", " +
                "CC_MediaUri: \"%s\", " +
                "CropClassName: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropClasses.forEach(cropClass -> session.writeTransaction(tx -> {
                System.out.println("Uploading CropClass # " + count.incrementAndGet());
                CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
                String newClassName = createNodeName(cropClass.getName());
                return tx.run(String.format(createClassFormat,
                        newClassName, cropClass.getClassName(),
                        cropClass.getUuId(),
                        createOdxUri(cropClass),
                        cropClass.getId(),
                        cropClass.getGroupId(),
                        cropGroup.getUuId(),
                        cropClass.getFaoId(),
                        cropClass.getMediaUri(),
                        cropClass.getName()));
            }));
        }
        System.out.println("CropClass uploading completed");
        System.out.println(count.get() + " CropClasses uploaded");
    }

    public void uploadCropClassAsBatch(List<CropClass> cropClasses, List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createClassFormat = "CREATE (%s:%s{" +
                "ODX_CropClass_UUId: \"%s\", " +
                "ODX_CropClass_Uri: \"%s\", " +
                "CropClassId: \"%s\", " +
                "CropGroupId_Ref: \"%s\", " +
                "ODX_CG_UUId_Ref: \"%s\", " +
                "CC_FAOId: \"%s\", " +
                "CC_MediaUri: \"%s\", " +
                "CropClassName: \"%s\"})\n";

        cropClasses.forEach(cropClass -> {
            count.incrementAndGet();
            CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
            String classNodeName = createUniqueNodeName(cropClass.getName(), Integer.toString(count.get()));
            String createClassCommand = String.format(createClassFormat,
                    classNodeName, cropClass.getClassName(),
                    cropClass.getUuId(),
                    createOdxUri(cropClass),
                    cropClass.getId(),
                    cropClass.getGroupId(),
                    cropGroup.getUuId(),
                    cropClass.getFaoId(),
                    cropClass.getMediaUri(),
                    cropClass.getName());
            builder.append(createClassCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), cropClass.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropClasses uploaded");
    }

    public void uploadCropSubClasses(List<CropSubClass> cropSubClasses, List<CropClass> cropClasses) {
        String createSubClassFormat = "CREATE (%s:%s{" +
                "ODX_CropSubClass_UUId: \"%s\", " +
                "ODX_CropSubClass_Uri: \"%s\", " +
                "CropSubClassId: \"%s\", " +
                "CropClassId_Ref: \"%s\", " +
                "ODX_CC_UUId_Ref: \"%s\", " +
                "CSC_FAOId: \"%s\", " +
                "CSC_MediaUri: \"%s\", " +
                "CropSubClassName: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            cropSubClasses.forEach(subClass -> session.writeTransaction(tx -> {
                System.out.println("Uploading CSC # " + count.incrementAndGet());
                CropClass cropClass = (CropClass) getFromCollectionById(cropClasses, subClass.getClassId());
                String newSubClassName = createNodeName(subClass.getName());
                return tx.run(String.format(createSubClassFormat,
                        newSubClassName, subClass.getClassName(),
                        subClass.getUuId(),
                        createOdxUri(subClass),
                        subClass.getId(),
                        subClass.getClassId(),
                        cropClass.getUuId(),
                        subClass.getFaoId(),
                        subClass.getMediaUri(),
                        subClass.getName()));
            }));
        }
        System.out.println("CropSubClass uploading completed");
        System.out.println(count.get() + " CropSubClasses uploaded");
    }

    public void uploadCropSubClassesAsBatch(List<CropSubClass> cropSubClasses, List<CropClass> cropClasses) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createSubClassFormat = "CREATE (%s:%s{" +
                "ODX_CropSubClass_UUId: \"%s\", " +
                "ODX_CropSubClass_Uri: \"%s\", " +
                "CropSubClassId: \"%s\", " +
                "CropClassId_Ref: \"%s\", " +
                "ODX_CC_UUId_Ref: \"%s\", " +
                "CSC_FAOId: \"%s\", " +
                "CSC_MediaUri: \"%s\", " +
                "CropSubClassName: \"%s\"})\n";

        cropSubClasses.forEach(subClass -> {
            count.incrementAndGet();
            CropClass cropClass = (CropClass) getFromCollectionById(cropClasses, subClass.getClassId());
            String subClassNodeName = createUniqueNodeName(subClass.getName(), Integer.toString(count.get()));
            String createClassCommand = String.format(createSubClassFormat,
                    subClassNodeName, subClass.getClassName(),
                    subClass.getUuId(),
                    createOdxUri(subClass),
                    subClass.getId(),
                    subClass.getClassId(),
                    cropClass.getUuId(),
                    subClass.getFaoId(),
                    subClass.getMediaUri(),
                    subClass.getName());
            builder.append(createClassCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), subClass.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropSubClasses uploaded");
    }

    public void uploadCropVarieties(List<CropVariety> cropVarieties, List<CropSubClass> cropSubClasses) {
        AtomicInteger count = new AtomicInteger(0);
        String createVarietyFormat = "CREATE (%s:%s{" +
                "ODX_CropVariety_UUId: \"%s\", " +
                "ODX_CropVariety_Uri: \"%s\", " +
                "CV_CropSubClassId_Ref: \"%s\", " +
                "CV_CSC_UUId_Ref: \"%s\", " +
                "CropVarietyId: \"%s\", " +
                "CropVarietyName: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropVarieties.forEach(variety -> session.writeTransaction(tx -> {
                System.out.println("Uploading CV # " + count.incrementAndGet());
                CropSubClass subClass = (CropSubClass) getFromCollectionById(cropSubClasses, variety.getSubClassId());
                String newVarietyName = createNodeName(variety.getName());
                return tx.run(String.format(createVarietyFormat,
                        newVarietyName, variety.getClassName(),
                        variety.getUuId(),
                        createOdxUri(variety),
                        variety.getSubClassId(),
                        subClass.getUuId(),
                        variety.getId(),
                        variety.getName()));
            }));
        }
        System.out.println("CropVariety uploading completed");
        System.out.println(count.get() + " CropVariety uploaded");
    }

    public void uploadCropVarietiesAsBatch(List<CropVariety> cropVarieties, List<CropSubClass> cropSubClasses) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createVarietyFormat = "CREATE (%s:%s{" +
                "ODX_CropVariety_UUId: \"%s\", " +
                "ODX_CropVariety_Uri: \"%s\", " +
                "CV_CropSubClassId_Ref: \"%s\", " +
                "CV_CSC_UUId_Ref: \"%s\", " +
                "CropVarietyId: \"%s\", " +
                "CropVarietyName: \"%s\"})\n";

        cropVarieties.forEach(variety -> {
            count.incrementAndGet();
            CropSubClass subClass = (CropSubClass) getFromCollectionById(cropSubClasses, variety.getSubClassId());
            String varietyNodeName = createUniqueNodeName(variety.getName(), Integer.toString(count.get()));
            String createVarietyCommand = String.format(createVarietyFormat,
                    varietyNodeName, variety.getClassName(),
                    variety.getUuId(),
                    createOdxUri(variety),
                    variety.getSubClassId(),
                    subClass.getUuId(),
                    variety.getId(),
                    variety.getName());
            builder.append(createVarietyCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), variety.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropVarieties uploaded");
    }

    public void uploadCropDescriptions(List<CropDescription> cropDescriptions, List<CropSubClass> cropSubClasses) {
        String createDescriptionFormat = "CREATE (%s:%s{" +
                "ODX_CropDescription_UUId: \"%s\", " +
                "ODX_CropDescription_Uri: \"%s\", " +
                "CD_MediaUri: \"%s\", " +
                "ChlorideSensitive: \"%s\", " +
                "CropDescriptionId: \"%s\", " +
                "CropDescriptionName: \"%s\", " +
                "CD_CropSubClassId_Ref: \"%s\", " +
                "CD_CSC_UUId_Ref: \"%s\", " +
                "ODX_CD_SourceSystem: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            cropDescriptions.forEach(description -> session.writeTransaction(tx -> {
                System.out.println("Uploading CD # " + count.incrementAndGet());
                CropSubClass subClass = (CropSubClass) getFromCollectionById(cropSubClasses, description.getSubClassId());
                String descriptionNodeName = createNodeName(description.getName());
                return tx.run(String.format(createDescriptionFormat,
                        descriptionNodeName, description.getClassName(),
                        description.getUuId(),
                        createOdxUri(description),
                        description.getMediaUri(),
                        description.isChlorideSensitive(),
                        description.getId(),
                        description.getName(),
                        description.getSubClassId(),
                        subClass.getUuId(),
                        "dummy_Polaris"));
            }));
        }
        System.out.println("CropDescription uploading completed");
        System.out.println(count.get() + " CropDescriptions uploaded");
    }

    public void uploadCropDescriptionsAsBatch(List<CropDescription> cropDescriptions, List<CropSubClass> cropSubClasses) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createDescriptionFormat = "CREATE (%s:%s{" +
                "ODX_CropDescription_UUId: \"%s\", " +
                "ODX_CropDescription_Uri: \"%s\", " +
                "CD_MediaUri: \"%s\", " +
                "ChlorideSensitive: \"%s\", " +
                "CropDescriptionId: \"%s\", " +
                "CropDescriptionName: \"%s\", " +
                "CD_CropSubClassId_Ref: \"%s\", " +
                "CD_CSC_UUId_Ref: \"%s\", " +
                "ODX_CD_SourceSystem: \"%s\"})\n";

        cropDescriptions.forEach(description -> {
            count.incrementAndGet();
            CropSubClass subClass = (CropSubClass) getFromCollectionById(cropSubClasses, description.getSubClassId());
            String descriptionNodeName = createUniqueNodeName(description.getName(), Integer.toString(count.get()));
            String createDescriptionCommand = String.format(createDescriptionFormat,
                    descriptionNodeName, description.getClassName(),
                    description.getUuId(),
                    createOdxUri(description),
                    description.getMediaUri(),
                    description.isChlorideSensitive(),
                    description.getId(),
                    description.getName(),
                    description.getSubClassId(),
                    subClass.getUuId(),
                    description.getSource());
            builder.append(createDescriptionCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), description.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropDescriptions uploaded");
    }

    public void uploadGrowthScales(List<GrowthScale> growthScales) {
        String createGrowthScaleCommandFormat = "CREATE (%s:%s{" +
                "ODX_GrowthScale_UUId: \"%s\", " +
                "GrowthScaleId: \"%s\", " +
                "GrowthScaleName: \"%s\", " +
                "ODX_GrowthScale_Uri: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            growthScales.forEach(scale -> session.writeTransaction(tx -> {
                String scaleNodeName = createNodeName(scale.getName());
                System.out.println("Uploading GS # " + count.incrementAndGet());
                return tx.run(String.format(createGrowthScaleCommandFormat,
                        scaleNodeName, scale.getClassName(),
                        scale.getUuId(),
                        scale.getId(),
                        scale.getName(),
                        createOdxUri(scale)));
            }));
        }
        System.out.println("GrowthScale uploading completed");
        System.out.println(count.get() + " GrowthScales uploaded");
    }

    public void uploadGrowthScalesAsBatch(List<GrowthScale> growthScales) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createGrowthScaleCommandFormat = "CREATE (%s:%s{" +
                "ODX_GrowthScale_UUId: \"%s\", " +
                "GrowthScaleId: \"%s\", " +
                "GrowthScaleName: \"%s\", " +
                "ODX_GrowthScale_Uri: \"%s\"})\n";

        growthScales.forEach(scale -> {
            count.incrementAndGet();
            String scaleNodeName = createUniqueNodeName(scale.getName(), Integer.toString(count.get()));
            String createGrowthScaleCommand = String.format(createGrowthScaleCommandFormat,
                    scaleNodeName, scale.getClassName(),
                    scale.getUuId(),
                    scale.getId(),
                    scale.getName(),
                    createOdxUri(scale));
            builder.append(createGrowthScaleCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), scale.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " GrowthScales uploaded");
    }

    public void uploadGrowthScaleStages(List<GrowthScaleStages> growthScaleStages, List<GrowthScale> growthScales) {
        String createGrowthScaleStageCommandFormat = "CREATE (%s:%s{" +
                "ODX_GrowthScaleStage_UUId: \"%s\", " +
                "ODX_GrowthScaleStage_Uri: \"%s\", " +
                "BaseOrdinal: \"%s\", " +
                "GrowthScaleId_Ref: \"%s\", " +
                "ODX_GS_UUId_Ref: \"%s\", " +
                "GrowthScaleStageDescription: \"%s\", " +
                "GrowthScaleStageId: \"%s\", " +
                "ODX_GSS_SourceSystem: \"%s\", " +
                "Ordinal: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            growthScaleStages.forEach(stage -> session.writeTransaction(tx -> {
                System.out.println("Uploading GSS # " + count.incrementAndGet());
                GrowthScale growthScale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
                String stageNodeName = createNodeName("GSS_number_" + count.get());
                return tx.run(String.format(createGrowthScaleStageCommandFormat,
                        stageNodeName, stage.getClassName(),
                        stage.getUuId(),
                        createOdxUri(stage),
                        stage.getBaseOrdinal(),
                        stage.getGrowthScaleId(),
                        growthScale.getUuId(),
                        stage.getGrowthScaleStageDescription(),
                        stage.getId(),
                        stage.getSource(),
                        stage.getOrdinal()));
            }));
        }
        System.out.println("GrowthScaleStage uploading completed");
        System.out.println(count.get() + " GrowthScaleStage uploaded");
    }

    public void uploadGrowthScaleStagesAsBatch(List<GrowthScaleStages> growthScaleStages, List<GrowthScale> growthScales) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createGrowthScaleStageFormat = "CREATE (%s:%s{" +
                "ODX_GrowthScaleStage_UUId: \"%s\", " +
                "ODX_GrowthScaleStage_Uri: \"%s\", " +
                "BaseOrdinal: \"%s\", " +
                "GrowthScaleId_Ref: \"%s\", " +
                "ODX_GS_UUId_Ref: \"%s\", " +
                "GrowthScaleStageDescription: \"%s\", " +
                "GrowthScaleStageId: \"%s\", " +
                "ODX_GSS_SourceSystem: \"%s\", " +
                "Ordinal: \"%s\"})\n";

        growthScaleStages.forEach(stage -> {
            count.incrementAndGet();
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
            String stageNodeName = createNodeName("GSS_number_" + count.get());
            String createGrowthScaleCommand = String.format(createGrowthScaleStageFormat,
                    stageNodeName, stage.getClassName(),
                    stage.getUuId(),
                    createOdxUri(stage),
                    stage.getBaseOrdinal(),
                    stage.getGrowthScaleId(),
                    scale.getUuId(),
                    stage.getGrowthScaleStageDescription(),
                    stage.getId(),
                    stage.getSource(),
                    stage.getOrdinal());
            builder.append(createGrowthScaleCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), stage.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " GrowthScaleStages uploaded");
    }

    public void uploadNutrients(List<Nutrient> nutrients) {
        String createNutrientsCommandFormat = "CREATE (%s:%s{" +
                "ODX_Nutrient_UUId: \"%s\", " +
                "ODX_Nutrient_Uri: \"%s\", " +
                "NutrientId: \"%s\", " +
                "NutrientName: \"%s\", " +
                "ElementalName: \"%s\", " +
                "Nutr_Ordinal: \"%s\", " +
                "ODX_Nutr_SourceSystem: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            nutrients.forEach(nutrient -> session.writeTransaction(tx -> {
                System.out.println("Uploading Nutrient # " + count.incrementAndGet());
                String nutrientNodeName = createNodeName(nutrient.getName());
                return tx.run(String.format(createNutrientsCommandFormat,
                        nutrientNodeName, nutrient.getClassName(),
                        nutrient.getUuId(),
                        createOdxUri(nutrient),
                        nutrient.getId(),
                        nutrient.getName(),
                        nutrient.getElementalName(),
                        nutrient.getNutrientOrdinal(),
                        "dummy_Polaris"));
            }));
        }
        System.out.println("Nutrient uploading completed");
        System.out.println(count.get() + " Nutrients uploaded");
    }

    public void uploadNutrientsAsBatch(List<Nutrient> nutrients) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createNutrientFormat = "CREATE (%s:%s{" +
                "ODX_Nutrient_UUId: \"%s\", " +
                "ODX_Nutrient_Uri: \"%s\", " +
                "NutrientId: \"%s\", " +
                "NutrientName: \"%s\", " +
                "ElementalName: \"%s\", " +
                "Nutr_Ordinal: \"%s\", " +
                "ODX_Nutr_SourceSystem: \"%s\"})\n";

        nutrients.forEach(nutrient -> {
            count.incrementAndGet();
            String nutrientNodeName = createUniqueNodeName(nutrient.getName(), Integer.toString(count.get()));
            String createNutrientCommand = String.format(createNutrientFormat,
                    nutrientNodeName, nutrient.getClassName(),
                    nutrient.getUuId(),
                    createOdxUri(nutrient),
                    nutrient.getId(),
                    nutrient.getName(),
                    nutrient.getElementalName(),
                    nutrient.getNutrientOrdinal(),
                    nutrient.getSource());
            builder.append(createNutrientCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), nutrient.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " Nutrients uploaded");
    }

    public void uploadUnits(List<Units> units) {
        String createUnitCommandFormat = "CREATE (%s:%s{" +
                "ODX_Units_UUId: \"%s\", " +
                "ODX_Units_Uri: \"%s\", " +
                "UnitsId: \"%s\", " +
                "UnitsName: \"%s\", " +
                "UnitsTags: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            units.forEach(unit -> session.writeTransaction(tx -> {
                System.out.println("Uploading Unit # " + count.incrementAndGet());
                String unitNodeName = createNodeName(unit.getName());
                return tx.run(String.format(createUnitCommandFormat,
                        unitNodeName, unit.getClassName(),
                        unit.getUuId(),
                        createOdxUri(unit),
                        unit.getId(),
                        unit.getName(),
                        unit.getTag()));
            }));
        }
        System.out.println("Unit uploading completed");
        System.out.println(count.get() + " Units uploaded");
    }

    public void uploadUnitsAsBatch(List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createUnitFormat = "CREATE (%s:%s{" +
                "ODX_Units_UUId: \"%s\", " +
                "ODX_Units_Uri: \"%s\", " +
                "UnitsId: \"%s\", " +
                "UnitsName: \"%s\", " +
                "UnitsTags: \"%s\"})\n";

        units.forEach(unit -> {
            count.incrementAndGet();
            String unitNodeName = createUniqueNodeName(unit.getName(), Integer.toString(count.get()));
            String createUnitCommand = String.format(createUnitFormat,
                    unitNodeName, unit.getClassName(),
                    unit.getUuId(),
                    createOdxUri(unit),
                    unit.getId(),
                    unit.getName(),
                    unit.getTag());
            builder.append(createUnitCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), unit.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " Units uploaded");
    }

    public void uploadUnitConversions(List<UnitConversion> conversions, List<Units> units) {
        String createUnitConversionCommandFormat = "CREATE (%s:%s{" +
                "ODX_UnitConversion_UUId: \"%s\", " +
                "ODX_UnitConversion_Uri: \"%s\", " +
                "UnitConversionName: \"%s\", " +
                "ConvertToUnitId: \"%s\", " +
                "CountryId_Ref: \"%s\", " +
                "Multiplier: \"%s\", " +
                "UnitConversionId: \"%s\", " +
                "UnitId_Ref: \"%s\", " +
                "ODX_UC_SourceSystem: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            conversions.forEach(conversion -> session.writeTransaction(tx -> {
                System.out.println("Uploading UnitConversion # " + count.incrementAndGet());
                Units convertToUnit = (Units) getFromCollectionById(units, conversion.getConvertToUnitId());
                String conversionNodeName = createNodeName(conversion.getName());
                return tx.run(String.format(createUnitConversionCommandFormat,
                        conversionNodeName, conversion.getClassName(),
                        conversion.getUuId(),
                        createOdxUri(conversion),
                        convertToUnit.getName(),
                        conversion.getConvertToUnitId(),
                        conversion.getCountryIdRef(),
                        conversion.getMultiplier(),
                        conversion.getId(),
                        conversion.getUnitIdRef(),
                        "dummy_Polaris"));
            }));
        }
        System.out.println("UnitConversion uploading completed");
        System.out.println(count.get() + " UnitConversions uploaded");
    }

    public void uploadUnitConversionsAsBatch(List<UnitConversion> conversions, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createConversionFormat = "CREATE (%s:%s{" +
                "ODX_UnitConversion_UUId: \"%s\", " +
                "ODX_UnitConversion_Uri: \"%s\", " +
                "UnitConversionName: \"%s\", " +
                "ConvertToUnitId: \"%s\", " +
                "CountryId_Ref: \"%s\", " +
                "Multiplier: \"%s\", " +
                "UnitConversionId: \"%s\", " +
                "UnitId_Ref: \"%s\", " +
                "ODX_UC_SourceSystem: \"%s\"})\n";

        conversions.forEach(conversion -> {
            count.incrementAndGet();
            Units convertToUnit = (Units) getFromCollectionById(units, conversion.getConvertToUnitId());
            String conversionNodeName = createUniqueNodeName(conversion.getName(), Integer.toString(count.get()));
            String createConversionCommand = String.format(createConversionFormat,
                    conversionNodeName, conversion.getClassName(),
                    conversion.getUuId(),
                    createOdxUri(conversion),
                    convertToUnit.getName(),
                    conversion.getConvertToUnitId(),
                    conversion.getCountryIdRef(),
                    conversion.getMultiplier(),
                    conversion.getId(),
                    conversion.getUnitIdRef(),
                    conversion.getSource());
            builder.append(createConversionCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), conversion.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " UnitConversions uploaded");
    }

    public void uploadFertilizers(List<Fertilizers> fertilizers) {
        String createFertilizerCommandFormat = "CREATE (%s:%s{" +
                "B: \"%s\", " +
                "BUnitId: \"%s\", " +
                "Ca: \"%s\", " +
                "CaUnitId: \"%s\", " +
                "Co: \"%s\", " +
                "CoUnitId: \"%s\", " +
                "Cu: \"%s\", " +
                "CuUnitId: \"%s\", " +
                "Density: \"%s\", " +
                "DhCode: \"%s\", " +
                "DryMatter: \"%s\", " +
                "ElectricalConductivity: \"%s\", " +
                "Fe: \"%s\", " +
                "FeUnitId: \"%s\", " +
                "K: \"%s\", " +
                "KUnitId: \"%s\", " +
                "LastSync: \"%s\", " +
                "LowChloride: \"%s\", " +
                "Mg: \"%s\", " +
                "MgUnitId: \"%s\", " +
                "Mn: \"%s\", " +
                "MnUnitId: \"%s\", " +
                "Mo: \"%s\", " +
                "MoUnitId: \"%s\", " +
                "N: \"%s\", " +
                "NUnitId: \"%s\", " +
                "Na: \"%s\", " +
                "NaUnitId: \"%s\", " +
                "NH4: \"%s\", " +
                "NO3: \"%s\", " +
                "ODX_Fert_SourceSystem: \"%s\", " +
                "ODX_Fertilizer_Uri: \"%s\", " +
                "ODX_Fertilizer_UUId: \"%s\", " +
                "P: \"%s\", " +
                "PUnitId: \"%s\", " +
                "Ph: \"%s\", " +
                "ProdFamily: \"%s\", " +
                "ProdName: \"%s\", " +
                "ProductId: \"%s\", " +
                "ProductType: \"%s\", " +
                "S: \"%s\", " +
                "SUnitId: \"%s\", " +
                "Se: \"%s\", " +
                "SeUnitId: \"%s\", " +
                "Solubility20C: \"%s\", " +
                "Solubility5C: \"%s\", " +
                "SpreaderLoss: \"%s\", " +
                "SyncId: \"%s\", " +
                "SyncSource: \"%s\", " +
                "Tank: \"%s\", " +
                "Urea: \"%s\", " +
                "UtilizationN: \"%s\", " +
                "UtilizationNH4: \"%s\", " +
                "Zn: \"%s\", " +
                "ZnUnitId: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            fertilizers.forEach(fertilizer -> session.writeTransaction(tx -> {
                System.out.println("Uploading Fertilizer # " + count.incrementAndGet());
                String nodeName = createNodeName(fertilizer.getName());
                return tx.run(String.format(createFertilizerCommandFormat,
                        nodeName, fertilizer.getClassName(),
                        fertilizer.getB(),
                        fertilizer.getBUnitId(),
                        fertilizer.getCa(),
                        fertilizer.getCaUnitId(),
                        fertilizer.getCo(),
                        fertilizer.getCoUnitId(),
                        fertilizer.getCu(),
                        fertilizer.getCuUnitId(),
                        fertilizer.getDensity(),
                        fertilizer.getDhCode(),
                        fertilizer.getDryMatter(),
                        fertilizer.getElectricalConductivity(),
                        fertilizer.getFe(),
                        fertilizer.getFeUnitId(),
                        fertilizer.getK(),
                        fertilizer.getKUnitId(),
                        fertilizer.getLastSync(),
                        fertilizer.getLowChloride(),
                        fertilizer.getMg(),
                        fertilizer.getMgUnitId(),
                        fertilizer.getMn(),
                        fertilizer.getMnUnitId(),
                        fertilizer.getMo(),
                        fertilizer.getMoUnitId(),
                        fertilizer.getN(),
                        fertilizer.getNUnitId(),
                        fertilizer.getNa(),
                        fertilizer.getNaUnitId(),
                        fertilizer.getNh4(),
                        fertilizer.getNo3(),
                        "dummy_Polaris",
                        createOdxUri(fertilizer),
                        fertilizer.getUuId(),
                        fertilizer.getP(),
                        fertilizer.getPUnitId(),
                        fertilizer.getPh(),
                        fertilizer.getFamily(),
                        fertilizer.getName(),
                        fertilizer.getId(),
                        fertilizer.getType(),
                        fertilizer.getS(),
                        fertilizer.getSUnitId(),
                        fertilizer.getSe(),
                        fertilizer.getSeUnitId(),
                        fertilizer.getSolubility20C(),
                        fertilizer.getSolubility5C(),
                        fertilizer.getSpreaderLoss(),
                        fertilizer.getSyncId(),
                        fertilizer.getSyncSource(),
                        fertilizer.getTank(),
                        fertilizer.getUrea(),
                        fertilizer.getUtilizationN(),
                        fertilizer.getUtilizationNh4(),
                        fertilizer.getZn(),
                        fertilizer.getZnUnitId()));
            }));
        }
        System.out.println("Fertilizer uploading completed");
        System.out.println(count.get() + " Fertilizers uploaded");
    }

    public void uploadFertilizersAsBatch(List<Fertilizers> fertilizers) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createFertilizerFormat = "CREATE (%s:%s{" +
                "B: \"%s\", " +
                "BUnitId: \"%s\", " +
                "Ca: \"%s\", " +
                "CaUnitId: \"%s\", " +
                "Co: \"%s\", " +
                "CoUnitId: \"%s\", " +
                "Cu: \"%s\", " +
                "CuUnitId: \"%s\", " +
                "Density: \"%s\", " +
                "DhCode: \"%s\", " +
                "DryMatter: \"%s\", " +
                "ElectricalConductivity: \"%s\", " +
                "Fe: \"%s\", " +
                "FeUnitId: \"%s\", " +
                "K: \"%s\", " +
                "KUnitId: \"%s\", " +
                "LastSync: \"%s\", " +
                "LowChloride: \"%s\", " +
                "Mg: \"%s\", " +
                "MgUnitId: \"%s\", " +
                "Mn: \"%s\", " +
                "MnUnitId: \"%s\", " +
                "Mo: \"%s\", " +
                "MoUnitId: \"%s\", " +
                "N: \"%s\", " +
                "NUnitId: \"%s\", " +
                "Na: \"%s\", " +
                "NaUnitId: \"%s\", " +
                "NH4: \"%s\", " +
                "NO3: \"%s\", " +
                "ODX_Fert_SourceSystem: \"%s\", " +
                "ODX_Fertilizer_Uri: \"%s\", " +
                "ODX_Fertilizer_UUId: \"%s\", " +
                "P: \"%s\", " +
                "PUnitId: \"%s\", " +
                "Ph: \"%s\", " +
                "ProdFamily: \"%s\", " +
                "ProdName: \"%s\", " +
                "ProductId: \"%s\", " +
                "ProductType: \"%s\", " +
                "S: \"%s\", " +
                "SUnitId: \"%s\", " +
                "Se: \"%s\", " +
                "SeUnitId: \"%s\", " +
                "Solubility20C: \"%s\", " +
                "Solubility5C: \"%s\", " +
                "SpreaderLoss: \"%s\", " +
                "SyncId: \"%s\", " +
                "SyncSource: \"%s\", " +
                "Tank: \"%s\", " +
                "Urea: \"%s\", " +
                "UtilizationN: \"%s\", " +
                "UtilizationNH4: \"%s\", " +
                "Zn: \"%s\", " +
                "ZnUnitId: \"%s\"})\n";

        fertilizers.forEach(fertilizer -> {
            count.incrementAndGet();
            String fertilizerNodeName = createUniqueNodeName(fertilizer.getName(), Integer.toString(count.get()));
            String createConversionCommand = String.format(createFertilizerFormat,
                    fertilizerNodeName, fertilizer.getClassName(),
                    fertilizer.getB(),
                    fertilizer.getBUnitId(),
                    fertilizer.getCa(),
                    fertilizer.getCaUnitId(),
                    fertilizer.getCo(),
                    fertilizer.getCoUnitId(),
                    fertilizer.getCu(),
                    fertilizer.getCuUnitId(),
                    fertilizer.getDensity(),
                    fertilizer.getDhCode(),
                    fertilizer.getDryMatter(),
                    fertilizer.getElectricalConductivity(),
                    fertilizer.getFe(),
                    fertilizer.getFeUnitId(),
                    fertilizer.getK(),
                    fertilizer.getKUnitId(),
                    fertilizer.getLastSync(),
                    fertilizer.getLowChloride(),
                    fertilizer.getMg(),
                    fertilizer.getMgUnitId(),
                    fertilizer.getMn(),
                    fertilizer.getMnUnitId(),
                    fertilizer.getMo(),
                    fertilizer.getMoUnitId(),
                    fertilizer.getN(),
                    fertilizer.getNUnitId(),
                    fertilizer.getNa(),
                    fertilizer.getNaUnitId(),
                    fertilizer.getNh4(),
                    fertilizer.getNo3(),
                    "dummy_Polaris",
                    createOdxUri(fertilizer),
                    fertilizer.getUuId(),
                    fertilizer.getP(),
                    fertilizer.getPUnitId(),
                    fertilizer.getPh(),
                    fertilizer.getFamily(),
                    fertilizer.getName(),
                    fertilizer.getId(),
                    fertilizer.getType(),
                    fertilizer.getS(),
                    fertilizer.getSUnitId(),
                    fertilizer.getSe(),
                    fertilizer.getSeUnitId(),
                    fertilizer.getSolubility20C(),
                    fertilizer.getSolubility5C(),
                    fertilizer.getSpreaderLoss(),
                    fertilizer.getSyncId(),
                    fertilizer.getSyncSource(),
                    fertilizer.getTank(),
                    fertilizer.getUrea(),
                    fertilizer.getUtilizationN(),
                    fertilizer.getUtilizationNh4(),
                    fertilizer.getZn(),
                    fertilizer.getZnUnitId());
            builder.append(createConversionCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), fertilizer.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " Fertilizers uploaded");
    }

    public void createCountryToRegionRelations(List<Country> countries, List<Region> regions) {
        AtomicInteger count = new AtomicInteger(0);
        regions.forEach(region -> {
            Country country = (Country) getFromCollectionById(countries, region.getCountryId());
            createCountryRegionRelation(country, region);
            System.out.println(count.incrementAndGet() + " Country to Region relations created");
        });
        System.out.println("Country-Region relation uploading completed");
        System.out.println(count.get() + " Country-Region relations uploaded");
    }

    public void createCountryToRegionRelationsAsBatch(List<Country> countries, List<Region> regions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Country to Region relations");
        regions.forEach(region -> {
            count.incrementAndGet();
            Country country = (Country) getFromCollectionById(countries, region.getCountryId());
            appendCountryRegionRelation(country, region, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), country.getClassName(), region.getClassName());
            }
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Country-Region relations uploaded");
    }

    private void flushBuilders(StringBuilder matchBuilder, StringBuilder createBuilder) {
        if (matchBuilder.length() + createBuilder.length() > BUILDER_LENGTH_THRESHOLD) {
            System.out.println("Start flushing builders to graph");
            writeBuildersToGraph(matchBuilder, createBuilder);
            System.out.println("Completed flushing builders to graph");
            matchBuilder.delete(0, matchBuilder.length());
            createBuilder.delete(0, createBuilder.length());
            System.out.println("Cleaned builders");
        }
    }

    private void flushBuildersForRelations(StringBuilder matchBuilder, StringBuilder createBuilder, int count, String fromNode, String toNode) {
        writeBuildersToGraph(matchBuilder, createBuilder);
        matchBuilder.delete(0, matchBuilder.length());
        createBuilder.delete(0, createBuilder.length());
        System.out.println(String.format("Totally uploaded %d of %s-%s relations from builder to graph", count, fromNode, toNode));
    }

    private void writeBuildersToGraph(StringBuilder matchBuilder, StringBuilder createBuilder) {
        if (matchBuilder.length() + createBuilder.length() == 0) return;
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(matchBuilder.append(createBuilder.toString()).toString()));
        }
    }

    public void createCropGroupToClassRelations(List<CropGroup> groups, List<CropClass> classes) {
        AtomicInteger count = new AtomicInteger(0);
        classes.forEach(cropClass -> {
            CropGroup group = (CropGroup) getFromCollectionById(groups, cropClass.getGroupId());
            createGroupClassRelation(group, cropClass);
            System.out.println(count.incrementAndGet() + " CropGroup to CropClass relations created");
        });
        System.out.println("Group-Class relation uploading completed");
        System.out.println(count.get() + " Group-Class relations uploaded");
    }

    public void createCropGroupToClassRelationsAsBatch(List<CropGroup> groups, List<CropClass> classes) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Group-Class relations");
        classes.forEach(cropClass -> {
            count.incrementAndGet();
            CropGroup group = (CropGroup) getFromCollectionById(groups, cropClass.getGroupId());
            appendGroupClassRelation(group, cropClass, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), group.getClassName(), cropClass.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Group-Class relations uploaded");
    }

    public void createCropClassToSubClassRelations(List<CropClass> ancestors, List<CropSubClass> children) {
        AtomicInteger count = new AtomicInteger(0);
        children.forEach(child -> {
            CropClass ancestor = (CropClass) getFromCollectionById(ancestors, child.getClassId());
            createClassSubClassRelation(ancestor, child);
            System.out.println(count.incrementAndGet() + " Class to SubClass relations created");
        });
        System.out.println("Class-SubClass relation uploading completed");
        System.out.println(count.get() + " Class-SubClass relations uploaded");
    }

    public void createCropClassToSubClassRelationsAsBatch(List<CropClass> cropClasses, List<CropSubClass> subClasses) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Class-SubClass relations");
        subClasses.forEach(subClass -> {
            count.incrementAndGet();
            CropClass cropClass = (CropClass) getFromCollectionById(cropClasses, subClass.getClassId());
            appendClassSubClassRelation(cropClass, subClass, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), cropClass.getClassName(), subClass.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Class-SubClass relations uploaded");
    }

    private void flushBuilder(StringBuilder builder) {
        if (builder.length() > BUILDER_LENGTH_THRESHOLD) {
            writeToGraph(builder);
            builder.delete(0, builder.length());
            System.out.println("Cleaned builder");
        }
    }

    private void flushBuilderForNodes(StringBuilder builder, int count, String nodeType) {
        writeToGraph(builder);
        builder.delete(0, builder.length());
        System.out.println(String.format("Totally uploaded %d of %s nodes from builder to graph", count, nodeType));
    }

    private void writeToGraph(StringBuilder builder) {
        if (builder.length() == 0) return;

//      ********************************************
//        System.out.println(builder.toString());
//      ********************************************

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    public void createCropSubClassToVarietyRelations(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        AtomicInteger count = new AtomicInteger(0);
        varieties.forEach(variety -> {
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, variety.getSubClassId());
            createSubClassVarietyRelation(subClass, variety);
            System.out.println(count.incrementAndGet() + " CSC to CV relations created");
        });
        System.out.println("CropSubClass-CropVariety relation uploading completed");
        System.out.println(count.get() + " CropSubClass-CropVariety relations uploaded");
    }

    public void createCropSubClassToVarietyRelationsAsBatch(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating CropSubClass-CropVariety relations");
        varieties.forEach(variety -> {
            count.incrementAndGet();
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, variety.getSubClassId());
            appendSubClassVarietyRelation(subClass, variety, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), subClass.getClassName(), variety.getClassName());
            }
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " CropSubClass-CropVariety relations uploaded");
    }


    public void createCropSubClassToDescriptionRelations(List<CropSubClass> subClasses, List<CropDescription> descriptions) {
        AtomicInteger count = new AtomicInteger(0);
        descriptions.forEach(description -> {
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, description.getSubClassId());
            createSubClassDescriptionRelation(subClass, description);
            System.out.println(count.incrementAndGet() + " CSC to CD relations created");
        });
        System.out.println("CropSubClass-CropDescription relation uploading completed");
        System.out.println(count.get() + " CropSubClass-CropDescription relations uploaded");
    }

    public void createCropSubClassToDescriptionRelationsAsBatch(List<CropSubClass> subClasses, List<CropDescription> descriptions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating CropSubClass-CropDescription relations");
        descriptions.forEach(description -> {
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, description.getSubClassId());
            createSubClassDescriptionRelationWithBuilders(subClass, description, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), subClass.getClassName(), description.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " CropSubClass-CropDescription relations uploaded");
    }

    public void createCropVarietyToDescriptionRelations(List<CropVariety> cropVarieties,
                                                        List<CropDescription> cropDescriptions,
                                                        List<CropDescriptionVariety> cropDescVars) {
        AtomicInteger count = new AtomicInteger(0);
        cropDescVars.forEach(descvar -> {
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, descvar.getDescId());
            CropVariety variety = (CropVariety) getFromCollectionById(cropVarieties, descvar.getVarId());
            createVarietyDescriptionRelation(variety, description);
            System.out.println(count.incrementAndGet() + " CV to CD relations created");
        });
        System.out.println("CropVariety-CropDescription relation uploading completed");
        System.out.println(count.get() + " CropVariety-CropDescription relations uploaded");
    }

    public void createCropVarietyToDescriptionRelationsAsBatch(List<CropVariety> cropVarieties,
                                                               List<CropDescription> cropDescriptions,
                                                               List<CropDescriptionVariety> cropDescVars) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating CropVariety-CropDescription relations");
        cropDescVars.forEach(descvar -> {
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, descvar.getDescId());
            CropVariety variety = (CropVariety) getFromCollectionById(cropVarieties, descvar.getVarId());
            createVarietyDescriptionRelationWithBuilders(variety, description, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), variety.getClassName(), description.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " CropVariety-CropDescription relations uploaded");
    }

    public void createCropDescriptionsToRegionsRelations(List<CropDescription> cropDescriptions,
                                                         List<Region> regions,
                                                         List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        cropRegions.forEach(cropRegion -> {
            Region region = (Region) getFromCollectionById(regions, cropRegion.getRegionIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cropRegion.getDescriptionId());
            createDescriptionToRegionRelationWithProperties(cropRegion, description, region);
            System.out.println(count.incrementAndGet() + " CD to Region relations created");

        });
        System.out.println("CropDescription-Region relation uploading completed");
        System.out.println(count.get() + " CropDescription-Region relations uploaded");
    }

    public void createCropDescriptionsToRegionsRelationsAsBatch(List<CropDescription> cropDescriptions,
                                                                List<Region> regions,
                                                                List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating CropDescription-Region relations");
        cropRegions.forEach(cropRegion -> {
            Region region = (Region) getFromCollectionById(regions, cropRegion.getRegionIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cropRegion.getDescriptionId());
            createDescriptionToRegionRelationWithBuilders(cropRegion, description, region, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), description.getClassName(), region.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " CropDescription-Region relations uploaded");
    }

    public void createCropDescriptionsToGrowthScaleRelations(List<CropDescription> cropDescriptions,
                                                             List<GrowthScale> growthScales,
                                                             List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropDescription, List<GrowthScale>> createdRelations = new HashMap<>();
        cropRegions.forEach(cr -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, cr.getGrowthScaleIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cr.getDescriptionId());
            List<GrowthScale> scales = new ArrayList<>();
            if (createdRelations.containsKey(description)) {
                scales = createdRelations.get(description);
            } else {
                createdRelations.put(description, scales);
            }

            if (!scales.contains(scale)) {
                scales.add(scale);
                createDescriptionGrowthScaleRelation(description, scale, cr);
                System.out.println(count.incrementAndGet() + " CropDescription to GrowthScale relations created");
            }
        });
        System.out.println("CropDescription-GrowthScale relation uploading completed");
        System.out.println(count.get() + " CropDescription-GrowthScale relations uploaded");
    }

    public void createCropDescriptionsToGrowthScaleRelationsAsBatch(List<CropDescription> cropDescriptions,
                                                                    List<GrowthScale> growthScales,
                                                                    List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating CropDescription-GrowthScale relations");
        cropRegions.forEach(cr -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, cr.getGrowthScaleIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cr.getDescriptionId());
            createDescriptionGrowthScaleRelationWithBuilders(description, scale, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), description.getClassName(), scale.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " CropDescription-GrowthScale relations uploaded");
    }

    public void createGrowthScaleToStagesRelations(List<GrowthScale> growthScales, List<GrowthScaleStages> growthScaleStages) {
        AtomicInteger count = new AtomicInteger(0);
        growthScaleStages.forEach(stage -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
            createGrowthScaleToStageRelation(scale, stage);
            System.out.println(count.incrementAndGet() + " GS to GSS relations created");
        });
        System.out.println("GrowthScale-GrowthScaleStage relation uploading completed");
        System.out.println(count.get() + " GrowthScale-GrowthScaleStage relations uploaded");
    }

    public void createGrowthScaleToStagesRelationsAsBatch(List<GrowthScale> growthScales, List<GrowthScaleStages> growthScaleStages) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating GrowthScale-GrowthScaleStage relations");
        growthScaleStages.forEach(stage -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
            createGrowthScaleToStageRelationWithBuilders(scale, stage, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), scale.getClassName(), stage.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " GrowthScale-GrowthScaleStage relations uploaded");
    }

    public void createNutrientsToUnitsRelations(List<Nutrient> nutrients, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        nutrients.forEach(nutrient -> {
            Units unit = getUnitByName(units, nutrient.getElementalName());
            createNutrientToUnitRelation(nutrient, unit);
            System.out.println(count.incrementAndGet() + " Nutrient to Unit relations created");
        });
        System.out.println("Nutrient-Unit relation uploading completed");
        System.out.println(count.get() + " Nutrient-Unit relations uploaded");
    }

    public void createNutrientsToUnitsRelationsAsBatch(List<Nutrient> nutrients, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Nutrient-Unit relations");
        nutrients.forEach(nutrient -> {
            Units unit = getUnitByName(units, nutrient.getElementalName());
            createNutrientToUnitRelationWithBuilders(nutrient, unit, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), nutrient.getClassName(), unit.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Nutrient-Unit relations uploaded");
    }

    public void createUnitsToConversionsRelations(List<Units> units, List<UnitConversion> conversions) {
        AtomicInteger count = new AtomicInteger(0);
        conversions.forEach(conversion -> {
            Units unit = (Units) getFromCollectionById(units, conversion.getUnitIdRef());
            createUnitToConversionRelation(unit, conversion);
            System.out.println(count.incrementAndGet() + " Unit to UnitConversion relations created");
        });
        System.out.println("Unit-UnitConversion relation uploading completed");
        System.out.println(count.get() + " Unit-UnitConversion relations uploaded");
    }

    public void createUnitsToConversionsRelationsAsBatch(List<Units> units, List<UnitConversion> conversions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Unit-UnitConversion relations");
        conversions.forEach(conversion -> {
            Units unit = (Units) getFromCollectionById(units, conversion.getUnitIdRef());
            createUnitToConversionRelationWithBuilders(unit, conversion, matchBuilder, createBuilder, count);
            if (count.get() % RELATION_BATCH_SIZE == 0) {
                flushBuildersForRelations(matchBuilder, createBuilder, count.get(), unit.getClassName(), conversion.getClassName());
            }
//            flushBuilders(matchBuilder, createBuilder);
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Unit-UnitConversion relations uploaded");
    }

    public void createFertilizersToRegionsRelations(List<Fertilizers> fertilizers, List<Country> countries, List<Region> regions, List<FertilizerRegion> fertilizerRegions) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizerRegions.forEach(fr -> {
            Fertilizers fertilizer = (Fertilizers) getFromCollectionById(fertilizers, fr.getProductId());
            Country country = getCountryFromCollectionById(countries, fr.getCountryId());
            Region region = getRegionFromCollectionById(regions, fr.getRegionId());

            if (!region.getId().equals("empty")) {
                createFertilizerToRegionRelation(fertilizer, country, region, fr);
                System.out.println(count.incrementAndGet() + " Fertilizer to Region relations created");
            }
        });
        System.out.println("Fertilizer-Region relation uploading completed");
        System.out.println(count.get() + " Fertilizer-Region relations uploaded");
    }

    public void createFertilizersToRegionsRelationsAsBatch(List<Fertilizers> fertilizers,
                                                           List<Region> regions,
                                                           List<FertilizerRegion> fertilizerRegions) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Fertilizer-Region relations");
        fertilizerRegions.forEach(fr -> {
            Fertilizers fertilizer = (Fertilizers) getFromCollectionById(fertilizers, fr.getProductId());
            Region region = getRegionFromCollectionById(regions, fr.getRegionId());
            if (!region.getId().equals("empty")) {
                createFertilizerToRegionRelationWithBuilders(fertilizer, region, matchBuilder, createBuilder, count);
                if (count.get() % RELATION_BATCH_SIZE == 0) {
                    flushBuildersForRelations(matchBuilder, createBuilder, count.get(), fertilizer.getClassName(), region.getClassName());
                }
//            flushBuilders(matchBuilder, createBuilder);
            }
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Fertilizer-Region relations uploaded");
    }

    public void createFertilizersToNutrientsRelations(List<Fertilizers> fertilizers, List<Nutrient> nutrients, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizers.forEach(fertilizer -> {
            Map<String, String> nutrientUnitsContent = fertilizer.getNutrientUnitsContent();
            for (Map.Entry<String, String> entry : nutrientUnitsContent.entrySet()) {
                String nutrientUnitId = entry.getKey();
                Units unit = (Units) getFromCollectionById(units, nutrientUnitId);
                String nutrientValue = entry.getValue();
                if (existNutrientWithName(nutrients, unit.getTag())
                        && !nutrientValue.equals("0.0")
                        && !nutrientValue.isEmpty()) {
                    Nutrient nutrient = getNutrientByElementalName(nutrients, unit.getTag());
                    createFertilizerToNutrientRelation(fertilizer, nutrient);
                    System.out.println(count.incrementAndGet() + " Fertilizer to Nutrient relations created");
                }
            }
        });
        System.out.println("Fertilizer-Nutrient relation uploading completed");
        System.out.println(count.get() + " Fertilizer-Nutrient relations uploaded");
    }

    public void createFertilizersToNutrientsRelationsAsBatch(List<Fertilizers> fertilizers,
                                                             List<Nutrient> nutrients,
                                                             List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder matchBuilder = new StringBuilder();
        StringBuilder createBuilder = new StringBuilder();
        System.out.println("Started creating Fertilizer-Nutrient relations");
        fertilizers.forEach(fertilizer -> {
            Map<String, String> nutrientUnitsContent = fertilizer.getNutrientUnitsContent();
            for (Map.Entry<String, String> entry : nutrientUnitsContent.entrySet()) {
                String nutrientUnitId = entry.getKey();
                Units unit = (Units) getFromCollectionById(units, nutrientUnitId);
                String nutrientValue = entry.getValue();
                if (existNutrientWithName(nutrients, unit.getTag())
                        && !nutrientValue.equals("0.0")
                        && !nutrientValue.isEmpty()) {
                    Nutrient nutrient = getNutrientByElementalName(nutrients, unit.getTag());
                    createFertilizerToNutrientRelationWithBuilders(fertilizer, nutrient, matchBuilder, createBuilder, count);
                    if (count.get() % RELATION_BATCH_SIZE == 0) {
                        flushBuildersForRelations(matchBuilder, createBuilder, count.get(), fertilizer.getClassName(), nutrient.getClassName());
                    }
//            flushBuilders(matchBuilder, createBuilder);
                }
            }
        });
        writeBuildersToGraph(matchBuilder, createBuilder);
        System.out.println(count.get() + " Fertilizer-Nutrient relations uploaded");
    }

    public void uploadShaclFromUrl(String url) {
        String commandFormat = "CALL n10s.validation.shacl.import.fetch" +
                "(\"%s\",\"Turtle\")";
        try (Session session = driver.session()) {
            session.run(String.format(commandFormat, url));
        }
        System.out.println("Shacl uploading completed");
    }

    public void uploadShaclInline(String shaclFileName) {
        ShaclRulesReader shaclRulesReader = new ShaclRulesReader();
        String shaclRules = shaclRulesReader.readShaclRules(shaclFileName);
        String commandFormat = "CALL n10s.validation.shacl.import.inline" +
                "('%s','Turtle')";
        try (Session session = driver.session()) {
            session.run(String.format(commandFormat, shaclRules));
        }
        System.out.println("Shacl uploading completed");
    }

    public void uploadShacl(String shaclFileName) {
        String commandFormat = "CALL n10s.validation.shacl.import.fetch" +
                "(\"file:///%s\",\"Turtle\")";
        try (Session session = driver.session()) {
            session.run(String.format(commandFormat, shaclFileName));
        }
        System.out.println("Shacl uploading completed");
    }

    public void activateShaclValidationOfTransactions() {
        String commandFormat = "CALL apoc.trigger.add('shacl-validate'," +
                "'call n10s.validation.shacl.validateTransaction($createdNodes,$createdRelationships, $assignedLabels, " +
                "$removedLabels, $assignedNodeProperties, $removedNodeProperties)', {phase:'before'})";
        try (Session session = driver.session()) {
            session.run(commandFormat);
        }
        System.out.println("Shacl validation for transactions activated");


    }

    private boolean existsInDatabase(Thing thing) {
        boolean answer;
        String existCheckQueryFormat = "MATCH (n) WHERE n.ODX_%s_UUId =\"%s\"" +
                "RETURN count(n)>0";
        String existCheckQuery = String.format(existCheckQueryFormat, thing.getClassName(), thing.getUuId().toString());

        try (Session session = driver.session()) {
            answer = session.readTransaction(tx -> {
                List<Record> records = tx.run(existCheckQuery).list();
                return records.get(0).get(0).asBoolean();
            });
        }
        return answer;
    }

    private Country getCountryFromCollectionById(List<Country> countries, String countryId) {
        return countries.stream()
                .filter(country -> country.getId().equals(countryId))
                .findFirst()
                .orElse(new Country(
                        "empty",
                        "empty",
                        "empty",
                        "empty",
                        "empty"));

    }

    private Region getRegionFromCollectionById(List<Region> regions, String regionId) {
        return regions.stream()
                .filter(region -> region.getId().equals(regionId))
                .findFirst()
                .orElse(new Region(
                        "empty",
                        "empty",
                        "empty",
                        "empty",
                        "empty"));

    }

    private FertilizerRegion getFertilizerRegionByProductId(List<FertilizerRegion> fertilizerRegions, String fertilizerId) {
        return fertilizerRegions.stream()
                .filter(fr -> fr.getProductId().equals(fertilizerId))
                .findFirst()
                //This is done because some productIds does not exist in Fertilizer_Reg file
                .orElse(new FertilizerRegion(
                        "empty",
                        "empty",
                        "empty",
                        "empty",
                        "empty",
                        "empty",
                        "empty"));
//                .orElseThrow(() -> new NoSuchElementException(String.format("No element with id %s in FertilizerRegions collection", fertilizerId)));
    }

    private void createCountryRegionRelation(Country country, Region region) {
        String matchCountry = String.format("MATCH (country:Country{ODX_Country_UUId:\"%s\"})\n", country.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = "CREATE (country)-[:hasRegion]->(region)";
        uploadRelationToDatabase(matchCountry, matchRegion, createRelation);
    }

    private void appendCountryRegionRelation(Country country,
                                             Region region,
                                             StringBuilder matchBuilder,
                                             StringBuilder createBuilder,
                                             AtomicInteger count) {
        String matchCountry = String.format("MATCH (country_%d:Country{ODX_Country_UUId:\"%s\"})\n", count.get(), country.getUuId());
        String matchRegion = String.format("MATCH (region_%d:Region{ODX_Region_UUId:\"%s\"})\n", count.get(), region.getUuId());
        String createRelation = String.format("CREATE (country_%1$d)-[:hasRegion]->(region_%1$d)\n", count.get());
        matchBuilder.append(matchCountry).append(matchRegion);
        createBuilder.append(createRelation);
    }

    private void createGroupClassRelation(CropGroup group, CropClass cropClass) {
        String matchGroup = String.format("MATCH (group:CropGroup{ODX_CropGroup_UUId:\"%s\"})\n", group.getUuId());
        String matchClass = String.format("MATCH (class:CropClass{ODX_CropClass_UUId:\"%s\"})\n", cropClass.getUuId());
        String createRelation = "CREATE (group)-[:hasCropClass]->(class)";
        uploadRelationToDatabase(matchGroup, matchClass, createRelation);
    }

    private void appendGroupClassRelation(CropGroup group,
                                          CropClass cropClass,
                                          StringBuilder matchBuilder,
                                          StringBuilder createBuilder,
                                          AtomicInteger count) {
        String matchGroup = String.format("MATCH (group_%d:CropGroup{ODX_CropGroup_UUId:\"%s\"})\n", count.get(), group.getUuId());
        String matchClass = String.format("MATCH (class_%d:CropClass{ODX_CropClass_UUId:\"%s\"})\n", count.get(), cropClass.getUuId());
        String createRelation = String.format("CREATE (group_%1$d)-[:hasCropClass]->(class_%1$d)\n", count.get());
        matchBuilder.append(matchGroup).append(matchClass);
        createBuilder.append(createRelation);
    }

    private void createClassSubClassRelation(CropClass ancestor, CropSubClass child) {
        String matchAncestor = String.format("MATCH (ancestor:CropClass{ODX_CropClass_UUId:\"%s\"})\n", ancestor.getUuId());
        String matchChild = String.format("MATCH (child:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", child.getUuId());
        String createRelation = "CREATE (ancestor)-[:hasCropSubClass]->(child)";
        uploadRelationToDatabase(matchAncestor, matchChild, createRelation);
    }

    private void appendClassSubClassRelation(CropClass cropClass,
                                             CropSubClass subClass,
                                             StringBuilder matchBuilder,
                                             StringBuilder createBuilder,
                                             AtomicInteger count) {
        String matchClass = String.format("MATCH (cropClass_%d:CropClass{ODX_CropClass_UUId:\"%s\"})\n", count.get(), cropClass.getUuId());
        String matchSubClass = String.format("MATCH (subClass_%d:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", count.get(), subClass.getUuId());
        String createRelation = String.format("CREATE (cropClass_%1$d)-[:hasCropSubClass]->(subClass_%1$d)\n", count.get());
        matchBuilder.append(matchClass).append(matchSubClass);
        createBuilder.append(createRelation);
    }

    private void createSubClassVarietyRelation(CropSubClass subClass, CropVariety variety) {
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", subClass.getUuId());
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", variety.getUuId());
        String createRelation = "CREATE (subClass)-[:hasCropVariety]->(variety)";
        uploadRelationToDatabase(matchSubClass, matchVariety, createRelation);
    }

    private void appendSubClassVarietyRelation(CropSubClass subClass,
                                               CropVariety variety,
                                               StringBuilder matchBuilder,
                                               StringBuilder createBuilder,
                                               AtomicInteger count) {
        String matchSubClass = String.format("MATCH (subClass_%d:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", count.get(), subClass.getUuId());
        String matchVariety = String.format("MATCH (variety_%d:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", count.get(), variety.getUuId());
        String createRelation = String.format("CREATE (subClass_%1$d)-[:hasCropVariety]->(variety_%1$d)\n", count.get());
        matchBuilder.append(matchSubClass).append(matchVariety);
        createBuilder.append(createRelation);
    }

    private void createSubClassDescriptionRelation(CropSubClass subClass, CropDescription description) {
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", subClass.getUuId());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String createRelation = "CREATE (subClass)-[:hasCropDescription]->(description)";
        uploadRelationToDatabase(matchSubClass, matchDescription, createRelation);
    }

    private void createSubClassDescriptionRelationWithBuilders(CropSubClass subClass,
                                                               CropDescription description,
                                                               StringBuilder matchBuilder,
                                                               StringBuilder createBuilder,
                                                               AtomicInteger count) {
        String matchSubClass = String.format("MATCH (subClass_%d:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", count.get(), subClass.getUuId());
        String matchDescription = String.format("MATCH (description_%d:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", count.get(), description.getUuId());
        String createRelation = String.format("CREATE (subClass_%1$d)-[:hasCropDescription]->(description_%1$d)\n", count.get());
        matchBuilder.append(matchSubClass).append(matchDescription);
        createBuilder.append(createRelation);
    }

    private void createVarietyDescriptionRelation(CropVariety variety, CropDescription description) {
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", variety.getUuId());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String createRelation = String.format("CREATE (variety)-[:hasCropDescription {" +
                        "CV_CropDescriptionId_Ref: \"%s\", " +
                        "CV_CD_UUId_Ref: \"%s\"}]->(description)",
                description.getId(),
                description.getUuId());
        uploadRelationToDatabase(matchVariety, matchDescription, createRelation);
    }

    private void createVarietyDescriptionRelationWithBuilders(CropVariety variety,
                                                              CropDescription description,
                                                              StringBuilder matchBuilder,
                                                              StringBuilder createBuilder,
                                                              AtomicInteger count) {
        String matchVariety = String.format("MATCH (variety_%d:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", count.get(), variety.getUuId());
        String matchDescription = String.format("MATCH (description_%d:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", count.get(), description.getUuId());
        String createRelation = String.format("CREATE (variety_%1$d)-[:hasCropDescription {" +
                        "CV_CropDescriptionId_Ref: \"%2$s\", " +
                        "CV_CD_UUId_Ref: \"%3$s\"}]->(description_%1$d)",
                count.get(),
                description.getId(),
                description.getUuId());
        matchBuilder.append(matchVariety).append(matchDescription);
        createBuilder.append(createRelation);
    }

    private void createGrowthScaleToStageRelation(GrowthScale scale, GrowthScaleStages stage) {
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", scale.getUuId());
        String matchStage = String.format("MATCH (stage:GrowthScaleStages{ODX_GrowthScaleStage_UUId:\"%s\"})\n", stage.getUuId());
        String createRelation = "CREATE (scale)-[:hasGrowthScaleStages]->(stage)";
        uploadRelationToDatabase(matchScale, matchStage, createRelation);
    }

    private void createGrowthScaleToStageRelationWithBuilders(GrowthScale scale,
                                                              GrowthScaleStages stage,
                                                              StringBuilder matchBuilder,
                                                              StringBuilder createBuilder,
                                                              AtomicInteger count) {
        String matchScale = String.format("MATCH (scale_%d:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", count.get(), scale.getUuId());
        String matchStage = String.format("MATCH (stage_%d:GrowthScaleStages{ODX_GrowthScaleStage_UUId:\"%s\"})\n", count.get(), stage.getUuId());
        String createRelation = String.format("CREATE (scale_%1$d)-[:hasCropDescription]->(stage_%1$d)\n", count.get());
        matchBuilder.append(matchScale).append(matchStage);
        createBuilder.append(createRelation);
    }

    private void createDescriptionToRegionRelationWithProperties(CropRegion cropRegion,
                                                                 CropDescription description,
                                                                 Region region) {
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = String.format("CREATE (description)-[:isAvailableIn {" +
                        "AdditionalProperties: \"%s\", " +
                        "CD_CountryIdRef: \"%s\", " +
                        "CD_GrowthScaleId_Ref: \"%s\", " +
                        "DefaultSeedingDate: \"%s\", " +
                        "DefaultHarvestDate: \"%s\", " +
                        "DefaultYield: \"%s\", " +
                        "YieldBaseUnitId: \"%s\", " +
                        "DemandBaseUnitId: \"%s\", " +
                        "CD_RegionIdRef: \"%s\"}]->(region)\n",
                cropRegion.getAdditionalProperties().replace("\"", ""),
                cropRegion.getCountryIdRef(),
                cropRegion.getGrowthScaleIdRef(),
                cropRegion.getDefaultSeedingDate(),
                cropRegion.getDefaultHarvestDate(),
                cropRegion.getDefaultYield(),
                cropRegion.getYieldBaseUnitId(),
                cropRegion.getDemandBaseUnitId(),
                cropRegion.getRegionIdRef());
        uploadRelationToDatabase(matchDescription, matchRegion, createRelation);
    }

    private void createDescriptionToRegionRelationWithBuilders(CropRegion cropRegion,
                                                               CropDescription description,
                                                               Region region,
                                                               StringBuilder matchBuilder,
                                                               StringBuilder createBuilder,
                                                               AtomicInteger count) {
        String matchDescription = String.format("MATCH (description_%d:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", count.get(), description.getUuId());
        String matchRegion = String.format("MATCH (region_%d:Region{ODX_Region_UUId:\"%s\"})\n", count.get(), region.getUuId());
        String createRelation = String.format("CREATE (description_%1$d)-[:isAvailableIn {" +
                        "AdditionalProperties: \"%s\", " +
                        "CD_CountryIdRef: \"%s\", " +
                        "CD_GrowthScaleId_Ref: \"%s\", " +
                        "DefaultSeedingDate: \"%s\", " +
                        "DefaultHarvestDate: \"%s\", " +
                        "DefaultYield: \"%s\", " +
                        "YieldBaseUnitId: \"%s\", " +
                        "DemandBaseUnitId: \"%s\"" +
                        "}]->(region_%1$d)",
                count.get(),
                cropRegion.getAdditionalProperties().replace("\"", ""),
                cropRegion.getCountryIdRef(),
                cropRegion.getGrowthScaleIdRef(),
                cropRegion.getDefaultSeedingDate(),
                cropRegion.getDefaultHarvestDate(),
                cropRegion.getDefaultYield(),
                cropRegion.getYieldBaseUnitId(),
                cropRegion.getDemandBaseUnitId());
        matchBuilder.append(matchDescription).append(matchRegion);
        createBuilder.append(createRelation);
    }

    private void createDescriptionGrowthScaleRelation(CropDescription description,
                                                      GrowthScale scale,
                                                      CropRegion cropRegion) {
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", scale.getUuId());
        String createRelation = "CREATE (description)-[:hasGrowthScale]->(scale)\n";
        uploadRelationToDatabase(matchDescription, matchScale, createRelation);
    }

    private void createDescriptionGrowthScaleRelationWithBuilders(CropDescription description,
                                                                  GrowthScale scale,
                                                                  StringBuilder matchBuilder,
                                                                  StringBuilder createBuilder,
                                                                  AtomicInteger count) {
        String matchDescription = String.format("MATCH (description_%d:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", count.get(), description.getUuId());
        String matchScale = String.format("MATCH (scale_%d:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", count.get(), scale.getUuId());
        String createRelation = String.format("CREATE (description_%1$d)-[:hasGrowthScale]->(scale_%1$d)\n", count.get());
        matchBuilder.append(matchDescription).append(matchScale);
        createBuilder.append(createRelation);
    }

    private void createNutrientToUnitRelation(Nutrient nutrient, Units unit) {
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String matchUnit = String.format("MATCH (unit:Units{UnitsName:\"%s\"})\n", unit.getName());
        String createRelation = "CREATE (nutrient)-[:hasNutrientUnit]->(unit)";
        uploadRelationToDatabase(matchNutrient, matchUnit, createRelation);
    }

    private void createNutrientToUnitRelationWithBuilders(Nutrient nutrient,
                                                          Units unit,
                                                          StringBuilder matchBuilder,
                                                          StringBuilder createBuilder,
                                                          AtomicInteger count) {
        String matchNutrient = String.format("MATCH (nutrient_%d:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", count.get(), nutrient.getUuId());
        String matchUnit = String.format("MATCH (unit_%d:Units{UnitsName:\"%s\"})\n", count.get(), unit.getName());
        String createRelation = String.format("CREATE (nutrient_%1$d)-[:hasNutrientUnit]->(unit_%1$d)\n", count.get());
        matchBuilder.append(matchNutrient).append(matchUnit);
        createBuilder.append(createRelation);
    }

    private void createUnitToConversionRelation(Units unit, UnitConversion conversion) {
        String matchUnit = String.format("MATCH (unit:Units{UnitsName:\"%s\"})\n", unit.getName());
        String matchConversion = String.format("MATCH (conversion:UnitConversion{ODX_UnitConversion_UUId:\"%s\"})\n", conversion.getUuId());
        String createRelation = "CREATE (unit)-[:hasUnitConversion]->(conversion)";
        uploadRelationToDatabase(matchUnit, matchConversion, createRelation);
    }

    private void createUnitToConversionRelationWithBuilders(Units unit,
                                                            UnitConversion conversion,
                                                            StringBuilder matchBuilder,
                                                            StringBuilder createBuilder,
                                                            AtomicInteger count) {
        String matchUnit = String.format("MATCH (unit_%d:Units{UnitsName:\"%s\"})\n", count.get(), unit.getName());
        String matchConversion = String.format("MATCH (conversion_%d:UnitConversion{ODX_UnitConversion_UUId:\"%s\"})\n", count.get(), conversion.getUuId());
        String createRelation = String.format("CREATE (unit_%1$d)-[:hasUnitConversion]->(conversion_%1$d)\n", count.get());
        matchBuilder.append(matchUnit).append(matchConversion);
        createBuilder.append(createRelation);
    }

    private void createFertilizerToRegionRelation(Fertilizers fertilizer, Country country, Region region, FertilizerRegion fr) {
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizer_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = String.format("CREATE (fertilizer)-[:isAvailableIn{" +
                        "ApplicationTags: \"%s\", " +
                        "Prod_CountryId_Ref: \"%s\"," +
                        "Prod_RegionId_Ref: \"%s\"," +
                        "ProdCountry_UUId_Ref: \"%s\", " +
                        "ProdRegion_UUId_Ref: \"%s\", " +
                        "LocalizedName: \"%s\"," +
                        "IsAvailable: \"%s\"}]->(region)\n",
                fr.getApplicationTags(),
                fr.getCountryId(),
                fr.getRegionId(),
                country.getUuId(),
                region.getUuId(),
                fr.getLocalizedName(),
                fr.getIsAvailable());
        uploadRelationToDatabase(matchFertilizer, matchRegion, createRelation);
    }

    private void createFertilizerToRegionRelationWithBuilders(Fertilizers fertilizer,
                                                              Region region,
                                                              StringBuilder matchBuilder,
                                                              StringBuilder createBuilder,
                                                              AtomicInteger count) {
        String matchFertilizer = String.format("MATCH (fertilizer_%d:Fertilizers{ODX_Fertilizer_UUId:\"%s\"})\n", count.get(), fertilizer.getUuId());
        String matchRegion = String.format("MATCH (region_%d:Region{ODX_Region_UUId:\"%s\"})\n", count.get(), region.getUuId());
        String createRelation = String.format("CREATE (fertilizer_%1$d)-[:isAvailableIn]->(region_%1$d)\n", count.get());
        matchBuilder.append(matchFertilizer).append(matchRegion);
        createBuilder.append(createRelation);
    }

    private void createFertilizerToNutrientRelation(Fertilizers fertilizer, Nutrient nutrient) {
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizer_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String createRelation = "CREATE (fertilizer)-[:hasProdNutrient]->(nutrient)";
        uploadRelationToDatabase(matchFertilizer, matchNutrient, createRelation);
    }

    private void createFertilizerToNutrientRelationWithBuilders(Fertilizers fertilizer,
                                                                Nutrient nutrient,
                                                                StringBuilder matchBuilder,
                                                                StringBuilder createBuilder,
                                                                AtomicInteger count) {
        String matchFertilizer = String.format("MATCH (fertilizer_%d:Fertilizers{ODX_Fertilizer_UUId:\"%s\"})\n", count.get(), fertilizer.getUuId());
        String matchNutrient = String.format("MATCH (nutrient_%d:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", count.get(), nutrient.getUuId());
        String createRelation = String.format("CREATE (fertilizer_%1$d)-[:hasProdNutrient]->(nutrient_%1$d)\n", count.get());
        matchBuilder.append(matchFertilizer).append(matchNutrient);
        createBuilder.append(createRelation);
    }

    private void uploadRelationToDatabase(String subject, String object, String predicate) {
        StringBuilder builder = new StringBuilder();
        builder.append(subject).append(object).append(predicate);

//      ********************************************
//        System.out.println(builder.toString());
//      ********************************************

        writeToGraph(builder);
    }


    private Thing getFromCollectionById(List<? extends Thing> things, String id) {
        return things.stream()
                .filter(thing -> thing.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No element with id %s in collection", id)));
    }

    private boolean existNutrientWithName(List<Nutrient> nutrients, String tag) {
        return nutrients.stream()
                .anyMatch(nutrient -> nutrient.getElementalName().equals(tag));
    }

    private Units getUnitByName(List<Units> units, String elementalName) {
        return units.stream()
                .filter(unit -> unit.getName().equals(elementalName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No Unit with name %s in collection", elementalName)));
    }

    private Nutrient getNutrientByElementalName(List<Nutrient> nutrients, String tag) {
        return nutrients.stream()
                .filter(nutrient -> nutrient.getElementalName().equals(tag))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No Nutrient with name %s in collection", tag)));
    }

    private String createNodeName(String oldName) {
        return oldName.replace("[^A-Za-z0-9]", "_")
                .replace(" ", "_WhiteSpace_")
                .replace("-", "_EnDash_")
                .replace(",", "_Comma_")
                .replace(".", "_Dot_")
                .replace("'", "_Apostrophe_")
                .replace("\"", "_QuotationMarks_")
                .replace("/", "_Slash_")
                .replace("%", "_Percent_")
                .replace("+", "_Plus_")
                .replace("=", "_equal_")
                .replace("0", "_zero_")
                .replace("1", "_one_")
                .replace("2", "_two_")
                .replace("3", "_tree_")
                .replace("4", "_four_")
                .replace("5", "_five_")
                .replace("6", "_six_")
                .replace("7", "_seven_")
                .replace("8", "_eight_")
                .replace("9", "_nine_")
                .replace("<", "_LeftTriangleBracket_")
                .replace(">", "_RightTriangleBracket_")
                .replace("(", "_LeftRoundBracket_")
                .replace(")", "_RightRoundBracket_");
    }

    private String createUniqueNodeName(String oldName, String count) {
        return oldName.replace("[^A-Za-z0-9]", "_")
                .replace(" ", "_WhiteSpace_")
                .replace("-", "_EnDash_")
                .replace(",", "_Comma_")
                .replace(".", "_Dot_")
                .replace("'", "_Apostrophe_")
                .replace("\"", "_QuotationMarks_")
                .replace("/", "_Slash_")
                .replace("%", "_Percent_")
                .replace("+", "_Plus_")
                .replace("=", "_equal_")
                .replace("0", "_zero_")
                .replace("1", "_one_")
                .replace("2", "_two_")
                .replace("3", "_tree_")
                .replace("4", "_four_")
                .replace("5", "_five_")
                .replace("6", "_six_")
                .replace("7", "_seven_")
                .replace("8", "_eight_")
                .replace("9", "_nine_")
                .replace("<", "_LeftTriangleBracket_")
                .replace(">", "_RightTriangleBracket_")
                .replace("(", "_LeftRoundBracket_")
                .replace(")", "_RightRoundBracket_")
                .concat("_")
                .concat(count);
    }

    private String createOdxUri(Thing thing) {
        return "ODX/" + thing.getClassName() + "/" + thing.getUuId();
    }

    public void createConstraintsAndIndexes() {
        List<String> commands = new ArrayList<>();

        commands.add("CREATE CONSTRAINT country_constraint ON (c:Country) ASSERT c.ODX_Country_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT region_constraint ON (r:Region) ASSERT r.ODX_Region_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT crop_group_constraint ON (cg:CropGroup) ASSERT cg.ODX_CropGroup_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT crop_class_constraint ON (cc:CropClass) ASSERT cc.ODX_CropClass_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT crop_sub_class_constraint ON (csc:CropSubClass) ASSERT csc.ODX_CropSubClass_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT crop_variety_constraint ON (cv:CropVariety) ASSERT cv.ODX_CropVariety_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT crop_description_constraint ON (cd:CropDescription) ASSERT cd.ODX_CropDescription_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT growth_scale_constraint ON (gs:GrowthScale) ASSERT gs.ODX_GrowthScale_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT growth_scale_stages_constraint ON (gss:GrowthScaleStages) ASSERT gss.ODX_GrowthScaleStage_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT nutrient_constraint ON (n:Nutrient) ASSERT n.ODX_Nutrient_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT units_constraint ON (u:Units) ASSERT u.ODX_Units_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT unit_conversion_constraint ON (uc:UnitConversion) ASSERT uc.ODX_UnitConversion_UUId IS UNIQUE\n");
        commands.add("CREATE CONSTRAINT fertilizer_constraint ON (f:Fertilizers) ASSERT f.ODX_Fertilizer_UUId IS UNIQUE\n");

        for (String command : commands) {
            try (Session session = driver.session()) {
                session.run(command);
            }
        }
        System.out.println("Created constraints");
    }

    public void dropConstraintsAndIndexes() {
        String command = "CALL apoc.schema.assert({},{},true) YIELD label, key RETURN *\n";
        try (Session session = driver.session()) {
            session.run(command);
        }
        System.out.println("Dropped indexes");
    }
}
