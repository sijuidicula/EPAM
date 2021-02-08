package com.yara.odx.loader;

import com.yara.odx.domain.*;
import com.yara.odx.reader.ShaclRulesReader;
import org.neo4j.driver.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PropertyGraphUploader implements AutoCloseable {

    public static final int BUILDER_LENGTH_THRESHOLD = 300_000;
    public static final int NODES_BATCH_SIZE = 25;
    public static final int RELATION_BATCH_SIZE = 25;

    private final Driver driver;

    public PropertyGraphUploader(URI uri, Config config) {
        driver = GraphDatabase.driver(uri, config);
    }

    public PropertyGraphUploader(String uri, String login, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(login, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    public void uploadCountries(List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        String createCountryFormat = "CREATE (%s:%s{" +
                "CountryId: \"%s\", " +
                "CountryName: \"%s\", " +
                "FIPS: \"%s\", " +
                "ISO2Code: \"%s\", " +
                "ISO3Code: \"%s\", " +
                "M49Code: \"%s\", " +
                "ODX_Country_Uri: \"%s\", " +
                "ODX_Country_UUId: \"%s\", " +
                "ODX_CS_UUId_Ref: \"%s\", " +
                "ProductSetCode: \"%s\", " +
                "UN: \"%s\"})\n";
        try (Session session = driver.session()) {
            countries.forEach(country -> {
                if (!existsInDatabase(country)) {
                    System.out.println("Uploading Country # " + count.incrementAndGet());
                    session.writeTransaction(tx -> tx.run(String.format(createCountryFormat,
                            createNodeName(country.getName()), country.getClassName(),
                            country.getId(),
                            country.getName(),
                            country.getFips(),
                            country.getIso2Code(),
                            country.getIso3Code(),
                            country.getM49Code(),
                            country.getUri(),
                            country.getUuId(),
                            country.getContinentalSectionUuidRef(),
                            country.getProductSetCode(),
                            country.getUn())));
                }
            });
        }
        System.out.println("Country uploading completed");
        System.out.println(count.get() + " Countries uploaded");
    }

    public void mergeCountries(List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        String mergeCountryFormat = "MERGE (n:%1$s{ODX_Country_UUId: \"%2$s\"})\n" +
                "ON CREATE SET \n" +
                "n.CountryId= \"%3$s\", \n" +
                "n.CountryName= \"%4$s\", \n" +
                "n.FIPS= \"%5$s\", \n" +
                "n.ISO2Code= \"%6$s\", \n" +
                "n.ISO3Code= \"%7$s\", \n" +
                "n.M49Code= \"%8$s\", \n" +
                "n.ODX_Country_Uri= \"9$%s\",\n" +
                "n.ODX_CS_UUId_Ref= \"%10$s\", \n" +
                "n.ProductSetCode= \"%11$s\", \n" +
                "n.UN= \"%12$s\"\n" +
                "ON MATCH SET \n" +
                "n.CountryId= \"%3$s\", \n" +
                "n.CountryName= \"%4$s\", \n" +
                "n.FIPS= \"%5$s\", \n" +
                "n.ISO2Code= \"%6$s\", \n" +
                "n.ISO3Code= \"%7$s\", \n" +
                "n.M49Code= \"%8$s\", \n" +
                "n.ODX_Country_Uri= \"9$%s\",\n" +
                "n.ODX_CS_UUId_Ref= \"%10$s\", \n" +
                "n.ProductSetCode= \"%11$s\", \n" +
                "n.UN= \"%12$s\"\n";

        countries.forEach(country -> {
            count.incrementAndGet();
            String mergeCountryCommand = String.format(mergeCountryFormat,
                    country.getClassName(), country.getUuId(),
                    country.getId(),
                    country.getName(),
                    country.getFips(),
                    country.getIso2Code(),
                    country.getIso3Code(),
                    country.getM49Code(),
                    country.getUri(),
                    country.getContinentalSectionUuidRef(),
                    country.getProductSetCode(),
                    country.getUn());
            writeToGraph(mergeCountryCommand);
        });
        System.out.println(count.get() + " Countries merged");
    }

    public void uploadCountriesAsBatch(List<Country> countries) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createCountryFormat = "CREATE (%s:%s{" +
                "CountryId: \"%s\", " +
                "CountryName: \"%s\", " +
                "FIPS: \"%s\", " +
                "ISO2Code: \"%s\", " +
                "ISO3Code: \"%s\", " +
                "M49Code: \"%s\", " +
                "ODX_Country_Uri: \"%s\", " +
                "ODX_Country_UUId: \"%s\", " +
                "ODX_CS_UUId_Ref: \"%s\", " +
                "ProductSetCode: \"%s\", " +
                "UN: \"%s\"})\n";

        countries.forEach(country -> {
            count.incrementAndGet();
            String countryNodeName = createUniqueNodeName(country.getName(), Integer.toString(count.get()));
            String createCountryCommand = String.format(createCountryFormat,
                    countryNodeName, country.getClassName(),
                    country.getId(),
                    country.getName(),
                    country.getFips(),
                    country.getIso2Code(),
                    country.getIso3Code(),
                    country.getM49Code(),
                    country.getUri(),
                    country.getUuId(),
                    country.getContinentalSectionUuidRef(),
                    country.getProductSetCode(),
                    country.getUn());
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
                                region.getUri(),
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
                    region.getUri(),
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

    public void mergeRegions(List<Region> regions) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "Country";
        String superClassIdentifier = "ODX_Country_UUId";
        String mergeRegionFormat = "MATCH (c:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (r:%4$s{ODX_Region_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "r.ODX_Region_Uri = \"%6$s\",\n" +
                "r.RegionId = \"%7$s\",\n" +
                "r.Region_CountryId_Ref = c.CountryId,\n" +
                "r.Region_Country_UUId_Ref = c.ODX_Country_UUId,\n" +
                "r.RegionName = \"%8$s\"\n" +
                "ON MATCH SET\n" +
                "r.ODX_Region_Uri = \"%6$s\",\n" +
                "r.RegionId = \"%7$s\",\n" +
                "r.Region_CountryId_Ref = c.CountryId,\n" +
                "r.Region_Country_UUId_Ref = c.ODX_Country_UUId,\n" +
                "r.RegionName = \"%8$s\"\n";

        regions.forEach(region -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(region.getSource(), ontologySuperClass, region.getCountryId());
            String mergeRegionCommand = String.format(mergeRegionFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    region.getClassName(), region.getUuId(),
                    region.getUri(),
                    region.getId(),
                    region.getName());
            writeToGraph(mergeRegionCommand);
        });
        System.out.println(count.get() + " Regions merged");
    }

    private UUID computeUUid(String source, String className, String id) {
        byte[] arr = (source + className + id).getBytes(StandardCharsets.UTF_8);
        return UUID.nameUUIDFromBytes(arr);
    }

    public void uploadCropGroups(List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String createGroupFormat = "CREATE (%s:%s{" +
                "CG_FAOId: \"%s\", " +
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
                        group.getId(),
                        group.getName(),
                        group.getUri(),
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
                    group.getId(),
                    group.getName(),
                    group.getUri(),
                    group.getUuId());
            builder.append(createGroupCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), group.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropGroups uploaded");
    }

    public void mergeCropGroups(List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String mergeGroupFormat = "MERGE (n:%1$s{ODX_CropGroup_UUId: \"%2$s\"})\n" +
                "ON CREATE SET \n" +
                "n.CG_FAOId= \"%3$s\", \n" +
                "n.CropGroupId= \"%4$s\", \n" +
                "n.CropGroupName= \"%5$s\", \n" +
                "n.ODX_CropGroup_Uri= \"%6$s\" \n" +
                "ON MATCH SET \n" +
                "n.CG_FAOId= \"%3$s\", \n" +
                "n.CropGroupId= \"%4$s\", \n" +
                "n.CropGroupName= \"%5$s\", \n" +
                "n.ODX_CropGroup_Uri= \"%6$s\"\n";

        cropGroups.forEach(group -> {
            count.incrementAndGet();
            String mergeGroupCommand = String.format(mergeGroupFormat,
                    group.getClassName(), group.getUuId(),
                    group.getFaoId(),
                    group.getId(),
                    group.getName(),
                    group.getUri());
            writeToGraph(mergeGroupCommand);
        });
        System.out.println(count.get() + " CropGroups merged");
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
                "CropClassName: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropClasses.forEach(cropClass -> session.writeTransaction(tx -> {
                System.out.println("Uploading CropClass # " + count.incrementAndGet());
                CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
                String newClassName = createNodeName(cropClass.getName());
                return tx.run(String.format(createClassFormat,
                        newClassName, cropClass.getClassName(),
                        cropClass.getUuId(),
                        cropClass.getUri(),
                        cropClass.getId(),
                        cropClass.getGroupId(),
                        cropGroup.getUuId(),
                        cropClass.getFaoId(),
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
                "CropClassName: \"%s\"})\n";

        cropClasses.forEach(cropClass -> {
            count.incrementAndGet();
            CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
            String classNodeName = createUniqueNodeName(cropClass.getName(), Integer.toString(count.get()));
            String createClassCommand = String.format(createClassFormat,
                    classNodeName, cropClass.getClassName(),
                    cropClass.getUuId(),
                    cropClass.getUri(),
                    cropClass.getId(),
                    cropClass.getGroupId(),
                    cropGroup.getUuId(),
                    cropClass.getFaoId(),
                    cropClass.getName());
            builder.append(createClassCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), cropClass.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropClasses uploaded");
    }

    public void mergeCropClasses(List<CropClass> cropClasses) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "CropGroup";
        String superClassIdentifier = "ODX_CropGroup_UUId";
        String mergeClassFormat = "MATCH (cg:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (cc:%4$s{ODX_CropClass_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "cc.ODX_CropClass_Uri = \"%6$s\",\n" +
                "cc.CropClassId = \"%7$s\",\n" +
                "cc.CropGroupId_Ref = cg.CropGroupId,\n" +
                "cc.ODX_CG_UUId_Ref = cg.ODX_CropGroup_UUId,\n" +
                "cc.CC_FAOId = \"%8$s\",\n" +
                "cc.CropClassName = \"%9$s\"\n" +
                "ON MATCH SET\n" +
                "cc.ODX_CropClass_Uri = \"%6$s\",\n" +
                "cc.CropClassId = \"%7$s\",\n" +
                "cc.CropGroupId_Ref = cg.CropGroupId,\n" +
                "cc.ODX_CG_UUId_Ref = cg.ODX_CropGroup_UUId,\n" +
                "cc.CC_FAOId = \"%8$s\",\n" +
                "cc.CropClassName = \"%9$s\"\n";

        cropClasses.forEach(cropClass -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(cropClass.getSource(), ontologySuperClass, cropClass.getGroupId());
            String mergeClassCommand = String.format(mergeClassFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    cropClass.getClassName(), cropClass.getUuId(),
                    cropClass.getUri(),
                    cropClass.getId(),
                    cropClass.getFaoId(),
                    cropClass.getName());
            writeToGraph(mergeClassCommand);
        });
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
                        subClass.getUri(),
                        subClass.getId(),
                        subClass.getClassId(),
                        cropClass.getUuId(),
                        subClass.getFaoId(),
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
                "CropSubClassName: \"%s\"})\n";

        cropSubClasses.forEach(subClass -> {
            count.incrementAndGet();
            CropClass cropClass = (CropClass) getFromCollectionById(cropClasses, subClass.getClassId());
            String subClassNodeName = createUniqueNodeName(subClass.getName(), Integer.toString(count.get()));
            String createClassCommand = String.format(createSubClassFormat,
                    subClassNodeName, subClass.getClassName(),
                    subClass.getUuId(),
                    subClass.getUri(),
                    subClass.getId(),
                    subClass.getClassId(),
                    cropClass.getUuId(),
                    subClass.getFaoId(),
                    subClass.getName());
            builder.append(createClassCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), subClass.getClassName());
            }
        });
        writeToGraph(builder);
        System.out.println(count.get() + " CropSubClasses uploaded");
    }

    public void mergeCropSubClasses(List<CropSubClass> cropSubClasses) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "CropClass";
        String superClassIdentifier = "ODX_CropClass_UUId";
        String mergeSubClassFormat = "MATCH (cc:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (csc:%4$s{ODX_CropSubClass_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "csc.ODX_CropSubClass_Uri = \"%6$s\",\n" +
                "csc.CropSubClassId = \"%7$s\",\n" +
                "csc.CropClassId_Ref = cc.CropClassId,\n" +
                "csc.ODX_CC_UUId_Ref = cc.ODX_CropClass_UUId,\n" +
                "csc.CSC_FAOId = \"%8$s\",\n" +
                "csc.CropSubClassName = \"%9$s\"\n" +
                "ON MATCH SET\n" +
                "csc.ODX_CropSubClass_Uri = \"%6$s\",\n" +
                "csc.CropSubClassId = \"%7$s\",\n" +
                "csc.CropClassId_Ref = cc.CropClassId,\n" +
                "csc.ODX_CC_UUId_Ref = cc.ODX_CropClass_UUId,\n" +
                "csc.CSC_FAOId = \"%8$s\",\n" +
                "csc.CropSubClassName = \"%9$s\"\n";

        cropSubClasses.forEach(subClass -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(subClass.getSource(), ontologySuperClass, subClass.getClassId());
            String mergeSubClassCommand = String.format(mergeSubClassFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    subClass.getClassName(), subClass.getUuId(),
                    subClass.getUri(),
                    subClass.getId(),
                    subClass.getFaoId(),
                    subClass.getName());
            writeToGraph(mergeSubClassCommand);
        });
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
                        variety.getUri(),
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
                    variety.getUri(),
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

    public void mergeCropVarieties(List<CropVariety> cropVarieties) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "CropSubClass";
        String superClassIdentifier = "ODX_CropSubClass_UUId";
        String mergeVarietyFormat = "MATCH (csc:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (cv:%4$s{ODX_CropVariety_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "cv.ODX_CropVariety_Uri = \"%6$s\",\n" +
                "cv.CV_CropSubClassId_Ref = csc.CropSubClassId,\n" +
                "cv.CV_CSC_UUId_Ref = csc.ODX_CropSubClass_UUId,\n" +
                "cv.CropVarietyId = \"%7$s\",\n" +
                "cv.CropVarietyName = \"%8$s\"\n" +
                "ON MATCH SET\n" +
                "cv.ODX_CropVariety_Uri = \"%6$s\",\n" +
                "cv.CV_CropSubClassId_Ref = csc.CropSubClassId,\n" +
                "cv.CV_CSC_UUId_Ref = csc.ODX_CropSubClass_UUId,\n" +
                "cv.CropVarietyId = \"%7$s\",\n" +
                "cv.CropVarietyName = \"%8$s\"\n";

        cropVarieties.forEach(variety -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(variety.getSource(), ontologySuperClass, variety.getSubClassId());
            String mergeVarietyCommand = String.format(mergeVarietyFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    variety.getClassName(), variety.getUuId(),
                    variety.getUri(),
                    variety.getId(),
                    variety.getName());
            writeToGraph(mergeVarietyCommand);
        });
        System.out.println(count.get() + " CropVarieties uploaded");
    }

    public void uploadCropDescriptions(List<CropDescription> cropDescriptions, List<CropSubClass> cropSubClasses,
                                       List<CropRegion> cropRegions, List<GrowthScale> growthScales) {
        String createDescriptionFormat = "CREATE (%s:%s{" +
                "ODX_CropDescription_UUId: \"%s\", " +
                "ODX_CropDescription_Uri: \"%s\", " +
                "CD_GrowthScaleId_Ref: \"%s\", " +
                "CD_ODX_GrowthScale_UUId_Ref: \"%s\", " +
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
                GrowthScale growthScale = getGrowthScaleForDescription(cropRegions, growthScales, description.getId());
                String descriptionNodeName = createNodeName(description.getName());
                return tx.run(String.format(createDescriptionFormat,
                        descriptionNodeName, description.getClassName(),
                        description.getUuId(),
                        description.getUri(),
                        growthScale.getId(),
                        growthScale.getUuId(),
                        description.isChlorideSensitive(),
                        description.getId(),
                        description.getName(),
                        description.getSubClassId(),
                        subClass.getUuId(),
                        description.getSource()));
            }));
        }
        System.out.println("CropDescription uploading completed");
        System.out.println(count.get() + " CropDescriptions uploaded");
    }

    public void uploadCropDescriptionsAsBatch(List<CropDescription> cropDescriptions, List<CropSubClass> cropSubClasses,
                                              List<CropRegion> cropRegions, List<GrowthScale> growthScales) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createDescriptionFormat = "CREATE (%s:%s{" +
                "ODX_CropDescription_UUId: \"%s\", " +
                "ODX_CropDescription_Uri: \"%s\", " +
                "CD_GrowthScaleId_Ref: \"%s\", " +
                "CD_ODX_GrowthScale_UUId_Ref: \"%s\", " +
                "ChlorideSensitive: \"%s\", " +
                "CropDescriptionId: \"%s\", " +
                "CropDescriptionName: \"%s\", " +
                "CD_CropSubClassId_Ref: \"%s\", " +
                "CD_CSC_UUId_Ref: \"%s\", " +
                "ODX_CD_SourceSystem: \"%s\"})\n";

        cropDescriptions.forEach(description -> {
            count.incrementAndGet();
            CropSubClass subClass = (CropSubClass) getFromCollectionById(cropSubClasses, description.getSubClassId());
            GrowthScale growthScale = getGrowthScaleForDescription(cropRegions, growthScales, description.getId());
            String descriptionNodeName = createUniqueNodeName(description.getName(), Integer.toString(count.get()));
            String createDescriptionCommand = String.format(createDescriptionFormat,
                    descriptionNodeName, description.getClassName(),
                    description.getUuId(),
                    description.getUri(),
                    growthScale.getId(),
                    growthScale.getUuId(),
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

    public void mergeCropDescriptions(List<CropDescription> cropDescriptions, List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "CropSubClass";
        String superClassIdentifier = "ODX_CropSubClass_UUId";
        String ontologyRelatedClass = "GrowthScale";
        String relatedClassIdentifier = "ODX_GrowthScale_UUId";
        String mergeDescriptionFormat = "MATCH (csc:%1$s{%2$s: \"%3$s\"})\n" +
                "MATCH (gs:%4$s{%5$s: \"%6$s\"})\n" +
                "MERGE (cd:%7$s{ODX_CropDescription_UUId: \"%8$s\"})\n" +
                "ON CREATE SET\n" +
                "cd.ODX_CropDescription_Uri = \"%9$s\",\n" +
                "cd.CD_GrowthScaleId_Ref = gs.GrowthScaleId,\n" +
                "cd.CD_ODX_GrowthScale_UUId_Ref = gs.ODX_GrowthScale_UUId,\n" +
                "cd.ChlorideSensitive = \"%10$s\",\n" +
                "cd.CropDescriptionId = \"%11$s\",\n" +
                "cd.CropDescriptionName = \"%12$s\",\n" +
                "cd.CD_CropSubClassId_Ref = csc.CropSubClassId,\n" +
                "cd.CD_CSC_UUId_Ref = csc.ODX_CropSubClass_UUId,\n" +
                "cd.ODX_CD_SourceSystem = \"%13$s\"\n" +
                "ON MATCH SET\n" +
                "cd.ODX_CropDescription_Uri = \"%9$s\",\n" +
                "cd.CD_GrowthScaleId_Ref = gs.GrowthScaleId,\n" +
                "cd.CD_ODX_GrowthScale_UUId_Ref = gs.ODX_GrowthScale_UUId,\n" +
                "cd.ChlorideSensitive = \"%10$s\",\n" +
                "cd.CropDescriptionId = \"%11$s\",\n" +
                "cd.CropDescriptionName = \"%12$s\",\n" +
                "cd.CD_CropSubClassId_Ref = csc.CropSubClassId,\n" +
                "cd.CD_CSC_UUId_Ref = csc.ODX_CropSubClass_UUId,\n" +
                "cd.ODX_CD_SourceSystem = \"%13$s\"\n";

        cropDescriptions.forEach(description -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(description.getSource(), ontologySuperClass, description.getSubClassId());
            String growthScaleId = getGrowthScaleId(cropRegions, description);
            UUID calculatedRelatedClassUUId = computeUUid(description.getSource(), ontologyRelatedClass, growthScaleId);
            String mergeDescriptionCommand = String.format(mergeDescriptionFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    ontologyRelatedClass, relatedClassIdentifier, calculatedRelatedClassUUId.toString(),
                    description.getClassName(), description.getUuId(),
                    description.getUri(),
                    description.isChlorideSensitive(),
                    description.getId(),
                    description.getName(),
                    description.getSource());
            writeToGraph(mergeDescriptionCommand);
        });
        System.out.println(count.get() + " CropDescriptions uploaded");
    }

    private String getGrowthScaleId(List<CropRegion> cropRegions, CropDescription description) {
        String growthScaleId = "empty";
        Optional<CropRegion> optionalCropRegion = cropRegions.stream()
                .filter(cr -> cr.getDescriptionId().equals(description.getId()))
                .findAny();
        if (optionalCropRegion.isPresent()) {
            growthScaleId = optionalCropRegion.get().getGrowthScaleIdRef();
        }
        return growthScaleId;
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
                        scale.getUri()));
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
                    scale.getUri());
            builder.append(createGrowthScaleCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), scale.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " GrowthScales uploaded");
    }

    public void mergeGrowthScales(List<GrowthScale> growthScales) {
        AtomicInteger count = new AtomicInteger(0);
        String createGrowthScaleFormat = "MERGE (gs:%1$s{ODX_GrowthScale_UUId: \"%2$s\"})\n" +
                "ON CREATE SET\n" +
                "gs.GrowthScaleId = \"%3$s\",\n" +
                "gs.GrowthScaleName = \"%4$s\",\n" +
                "gs.ODX_GrowthScale_Uri = \"%5$s\"\n" +
                "ON MATCH SET\n" +
                "gs.GrowthScaleId = \"%3$s\",\n" +
                "gs.GrowthScaleName = \"%4$s\",\n" +
                "gs.ODX_GrowthScale_Uri = \"%5$s\"\n";

        growthScales.forEach(scale -> {
            count.incrementAndGet();
            String createGrowthScaleCommand = String.format(createGrowthScaleFormat,
                    scale.getClassName(), scale.getUuId(),
                    scale.getId(),
                    scale.getName(),
                    scale.getUri());
            writeToGraph(createGrowthScaleCommand);
        });
        System.out.println(count.get() + " GrowthScales uploaded");
    }

    public void uploadGrowthScaleStages(List<GrowthScaleStages> growthScaleStages, List<GrowthScale> growthScales) {
        String createGrowthScaleStageCommandFormat = "CREATE (%s:%s{" +
                "BaseOrdinal: \"%s\", " +
                "GrowthScaleId_Ref: \"%s\", " +
                "GrowthScaleStagesDescription: \"%s\", " +
                "GrowthScaleStagesId: \"%s\", " +
                "ODX_GrowthScaleStages_SourceSystem: \"%s\", " +
                "ODX_GrowthScaleStages_Uri: \"%s\", " +
                "ODX_GrowthScaleStages_UUId: \"%s\", " +
                "ODX_GS_UUId_Ref: \"%s\", " +
                "Ordinal: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            growthScaleStages.forEach(stage -> session.writeTransaction(tx -> {
                System.out.println("Uploading GSS # " + count.incrementAndGet());
                GrowthScale growthScale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
                String stageNodeName = createNodeName("GSS_number_" + count.get());
                return tx.run(String.format(createGrowthScaleStageCommandFormat,
                        stageNodeName, stage.getClassName(),
                        stage.getBaseOrdinal(),
                        stage.getGrowthScaleId(),
                        stage.getGrowthScaleStageDescription(),
                        stage.getId(),
                        stage.getSource(),
                        stage.getUri(),
                        stage.getUuId(),
                        growthScale.getUuId(),
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
                "BaseOrdinal: \"%s\", " +
                "GrowthScaleId_Ref: \"%s\", " +
                "GrowthScaleStagesDescription: \"%s\", " +
                "GrowthScaleStagesId: \"%s\", " +
                "ODX_GrowthScaleStages_SourceSystem: \"%s\", " +
                "ODX_GrowthScaleStages_Uri: \"%s\", " +
                "ODX_GrowthScaleStages_UUId: \"%s\", " +
                "ODX_GS_UUId_Ref: \"%s\", " +
                "Ordinal: \"%s\"})\n";

        growthScaleStages.forEach(stage -> {
            count.incrementAndGet();
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
            String stageNodeName = createNodeName("GSS_number_" + count.get());
            String createGrowthScaleCommand = String.format(createGrowthScaleStageFormat,
                    stageNodeName, stage.getClassName(),
                    stage.getBaseOrdinal(),
                    stage.getGrowthScaleId(),
                    stage.getGrowthScaleStageDescription(),
                    stage.getId(),
                    stage.getSource(),
                    stage.getUri(),
                    stage.getUuId(),
                    scale.getUuId(),
                    stage.getOrdinal());
            builder.append(createGrowthScaleCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), stage.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " GrowthScaleStages uploaded");
    }

    public void mergeGrowthScaleStages(List<GrowthScaleStages> growthScaleStages) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "GrowthScale";
        String superClassIdentifier = "ODX_GrowthScale_UUId";
        String mergeGrowthScaleStageFormat = "MATCH (gs:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (gss:%4$s{ODX_GrowthScaleStages_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "gss.BaseOrdinal = \"%6$s\",\n" +
                "gss.GrowthScaleId_Ref = gs.GrowthScaleId,\n" +
                "gss.GrowthScaleStagesDescription = \"%7$s\",\n" +
                "gss.GrowthScaleStagesId = \"%8$s\",\n" +
                "gss.ODX_GrowthScaleStages_SourceSystem = \"%9$s\",\n" +
                "gss.ODX_GrowthScaleStages_Uri = \"%10$s\",\n" +
                "gss.ODX_GS_UUId_Ref = gs.ODX_GrowthScale_UUId,\n" +
                "gss.Ordinal = \"%11$s\"\n" +
                "ON MATCH SET\n" +
                "gss.BaseOrdinal = \"%6$s\",\n" +
                "gss.GrowthScaleId_Ref = gs.GrowthScaleId,\n" +
                "gss.GrowthScaleStagesDescription = \"%7$s\",\n" +
                "gss.GrowthScaleStagesId = \"%8$s\",\n" +
                "gss.ODX_GrowthScaleStages_SourceSystem = \"%9$s\",\n" +
                "gss.ODX_GrowthScaleStages_Uri = \"%10$s\",\n" +
                "gss.ODX_GS_UUId_Ref = gs.ODX_GrowthScale_UUId,\n" +
                "gss.Ordinal = \"%11$s\"\n";

        growthScaleStages.forEach(stage -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(stage.getSource(), ontologySuperClass, stage.getGrowthScaleId());
            String mergeGrowthScaleCommand = String.format(mergeGrowthScaleStageFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    stage.getClassName(), stage.getUuId(),
                    stage.getBaseOrdinal(),
                    stage.getGrowthScaleStageDescription(),
                    stage.getId(),
                    stage.getSource(),
                    stage.getUri(),
                    stage.getUuId(),
                    stage.getOrdinal());
            writeToGraph(mergeGrowthScaleCommand);
        });
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
                        nutrient.getUri(),
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
                    nutrient.getUri(),
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

    public void mergeNutrients(List<Nutrient> nutrients) {
        AtomicInteger count = new AtomicInteger(0);
        String mergeNutrientFormat = "MERGE (n:%1$s{ODX_Nutrient_UUId: \"%2$s\"})\n" +
                "ON CREATE SET\n" +
                "n.ODX_Nutrient_Uri = \"%3$s\",\n" +
                "n.NutrientId = \"%4$s\",\n" +
                "n.NutrientName = \"%5$s\",\n" +
                "n.ElementalName = \"%6$s\",\n" +
                "n.Nutr_Ordinal = \"%7$s\",\n" +
                "n.ODX_Nutr_SourceSystem = \"%8$s\"\n" +
                "ON MATCH SET\n" +
                "n.ODX_Nutrient_Uri = \"%3$s\",\n" +
                "n.NutrientId = \"%4$s\",\n" +
                "n.NutrientName = \"%5$s\",\n" +
                "n.ElementalName = \"%6$s\",\n" +
                "n.Nutr_Ordinal = \"%7$s\",\n" +
                "n.ODX_Nutr_SourceSystem = \"%8$s\"\n";

        nutrients.forEach(nutrient -> {
            count.incrementAndGet();
            String mergeNutrientCommand = String.format(mergeNutrientFormat,
                    nutrient.getClassName(), nutrient.getUuId(),
                    nutrient.getUri(),
                    nutrient.getId(),
                    nutrient.getName(),
                    nutrient.getElementalName(),
                    nutrient.getNutrientOrdinal(),
                    nutrient.getSource());
            writeToGraph(mergeNutrientCommand);
        });
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
                        unit.getUri(),
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
                    unit.getUri(),
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

    public void mergeUnits(List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        String mergeUnitFormat = "MERGE (u:%1$s{ODX_Units_UUId: \"%2$s\"})\n" +
                "ON CREATE SET\n" +
                "u.ODX_Units_Uri = \"%3$s\",\n" +
                "u.UnitsId = \"%4$s\",\n" +
                "u.UnitsName = \"%5$s\",\n" +
                "u.UnitsTags = \"%6$s\"\n" +
                "ON MATCH SET\n" +
                "u.ODX_Units_Uri = \"%3$s\",\n" +
                "u.UnitsId = \"%4$s\",\n" +
                "u.UnitsName = \"%5$s\",\n" +
                "u.UnitsTags = \"%6$s\"\n";

        units.forEach(unit -> {
            count.incrementAndGet();
            String mergeUnitCommand = String.format(mergeUnitFormat,
                    unit.getClassName(), unit.getUuId(),
                    unit.getUri(),
                    unit.getId(),
                    unit.getName(),
                    unit.getTag());
            writeToGraph(mergeUnitCommand);
        });
        System.out.println(count.get() + " Units uploaded");
    }

    public void uploadUnitConversions(List<UnitConversion> conversions, List<Units> units) {
        String createUnitConversionCommandFormat = "CREATE (%s:%s{" +
                "ConvertToUnitId: \"%s\", " +
                "CountryId_Ref: \"%s\", " +
                "Multiplier: \"%s\", " +
                "ODX_UnitConversion_SourceSystem: \"%s\", " +
                "ODX_UnitConversion_Uri: \"%s\", " +
                "ODX_UnitConversion_UUId: \"%s\", " +
                "ODX_Units_UUId_Ref: \"%s\", " +
                "UnitConversionId: \"%s\", " +
                "UnitId_Ref: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            conversions.forEach(conversion -> session.writeTransaction(tx -> {
                System.out.println("Uploading UnitConversion # " + count.incrementAndGet());
                Units originalUnits = (Units) getFromCollectionById(units, conversion.getUnitIdRef());
                String conversionNodeName = createNodeName(conversion.getName());
                return tx.run(String.format(createUnitConversionCommandFormat,
                        conversionNodeName, conversion.getClassName(),
                        conversion.getConvertToUnitId(),
                        conversion.getCountryIdRef(),
                        conversion.getMultiplier(),
                        conversion.getSource(),
                        conversion.getUri(),
                        conversion.getUuId(),
                        originalUnits.getUuId(),
                        conversion.getId(),
                        conversion.getUnitIdRef()));
            }));
        }
        System.out.println("UnitConversion uploading completed");
        System.out.println(count.get() + " UnitConversions uploaded");
    }

    public void uploadUnitConversionsAsBatch(List<UnitConversion> conversions, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        StringBuilder builder = new StringBuilder();
        String createConversionFormat = "CREATE (%s:%s{" +
                "ConvertToUnitId: \"%s\", " +
                "CountryId_Ref: \"%s\", " +
                "Multiplier: \"%s\", " +
                "ODX_UnitConversion_SourceSystem: \"%s\", " +
                "ODX_UnitConversion_Uri: \"%s\", " +
                "ODX_UnitConversion_UUId: \"%s\", " +
                "ODX_Units_UUId_Ref: \"%s\", " +
                "UnitConversionId: \"%s\", " +
                "UnitsId_Ref: \"%s\"})\n";

        conversions.forEach(conversion -> {
            count.incrementAndGet();
            Units originalUnits = (Units) getFromCollectionById(units, conversion.getUnitIdRef());
            String conversionNodeName = createUniqueNodeName(conversion.getName(), Integer.toString(count.get()));
            String createConversionCommand = String.format(createConversionFormat,
                    conversionNodeName, conversion.getClassName(),
                    conversion.getConvertToUnitId(),
                    conversion.getCountryIdRef(),
                    conversion.getMultiplier(),
                    conversion.getSource(),
                    conversion.getUri(),
                    conversion.getUuId(),
                    originalUnits.getUuId(),
                    conversion.getId(),
                    conversion.getUnitIdRef());
            builder.append(createConversionCommand);
            if (count.get() % NODES_BATCH_SIZE == 0) {
                flushBuilderForNodes(builder, count.get(), conversion.getClassName());
            }
        });

        writeToGraph(builder);
        System.out.println(count.get() + " UnitConversions uploaded");
    }

    public void mergeUnitConversions(List<UnitConversion> conversions) {
        AtomicInteger count = new AtomicInteger(0);
        String ontologySuperClass = "Units";
        String superClassIdentifier = "ODX_Units_UUId";
        String mergeConversionFormat = "MATCH (u:%1$s{%2$s: \"%3$s\"})\n" +
                "MERGE (uc:%4$s{ODX_UnitConversion_UUId: \"%5$s\"})\n" +
                "ON CREATE SET\n" +
                "uc.ConvertToUnitId = \"%6$s\",\n" +
                "uc.CountryId_Ref = \"%7$s\",\n" +
                "uc.Multiplier = \"%8$s\",\n" +
                "uc.ODX_UnitConversion_SourceSystem = \"%9$s\",\n" +
                "uc.ODX_UnitConversion_Uri = \"%10$s\",\n" +
                "uc.ODX_Units_UUId_Ref = u.ODX_Units_UUId,\n" +
                "uc.UnitConversionId = \"%11$s\",\n" +
                "uc.UnitsId_Ref = u.UnitsId \n" +
                "ON MATCH SET\n" +
                "uc.ConvertToUnitId = \"%6$s\",\n" +
                "uc.CountryId_Ref = \"%7$s\",\n" +
                "uc.Multiplier = \"%8$s\",\n" +
                "uc.ODX_UnitConversion_SourceSystem = \"%9$s\",\n" +
                "uc.ODX_UnitConversion_Uri = \"%10$s\",\n" +
                "uc.ODX_Units_UUId_Ref = u.ODX_Units_UUId,\n" +
                "uc.UnitConversionId = \"%11$s\",\n" +
                "uc.UnitsId_Ref = u.UnitsId \n";

        conversions.forEach(conversion -> {
            count.incrementAndGet();
            UUID calculatedSuperClassUUId = computeUUid(conversion.getSource(), ontologySuperClass, conversion.getUnitIdRef());
            String mergeConversionCommand = String.format(mergeConversionFormat,
                    ontologySuperClass, superClassIdentifier, calculatedSuperClassUUId.toString(),
                    conversion.getClassName(), conversion.getUuId(),
                    conversion.getConvertToUnitId(),
                    conversion.getCountryIdRef(),
                    conversion.getMultiplier(),
                    conversion.getSource(),
                    conversion.getUri(),
                    conversion.getId());
            writeToGraph(mergeConversionCommand);
        });
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
                "ODX_Fertilizers_Uri: \"%s\", " +
                "ODX_Fertilizers_UUId: \"%s\", " +
                "P: \"%s\", " +
                "PUnitId: \"%s\", " +
                "Ph: \"%s\", " +
                "ProdFamily: \"%s\", " +
                "FertilizersName: \"%s\", " +
                "FertilizersId: \"%s\", " +
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
                        fertilizer.getSource(),
                        fertilizer.getUri(),
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
                "ODX_Fertilizers_Uri: \"%s\", " +
                "ODX_Fertilizers_UUId: \"%s\", " +
                "P: \"%s\", " +
                "PUnitId: \"%s\", " +
                "Ph: \"%s\", " +
                "ProdFamily: \"%s\", " +
                "FertilizersName: \"%s\", " +
                "FertilizersId: \"%s\", " +
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
                    fertilizer.getSource(),
                    fertilizer.getUri(),
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

    public void mergeFertilizers(List<Fertilizers> fertilizers) {
        AtomicInteger count = new AtomicInteger(0);
        String createFertilizerFormat = "MERGE (f:%1$s{ODX_Fertilizers_UUId: \"%2$s\"})\n" +
                "ON CREATE SET\n" +
                "f.B= \"%3$s\", " +
                "f.BUnitId= \"%4$s\", " +
                "f.Ca= \"%5$s\", " +
                "f.CaUnitId= \"%6$s\", " +
                "f.Co= \"%7$s\", " +
                "f.CoUnitId= \"%8$s\", " +
                "f.Cu= \"%9$s\", " +
                "f.CuUnitId= \"%10$s\", " +
                "f.Density= \"%11$s\", " +
                "f.DhCode= \"%12$s\", " +
                "f.DryMatter= \"%13$s\", " +
                "f.ElectricalConductivity= \"%14$s\", " +
                "f.Fe= \"%15$s\", " +
                "f.FeUnitId= \"%16$s\", " +
                "f.K= \"%17$s\", " +
                "f.KUnitId= \"%18$s\", " +
                "f.LastSync= \"%19$s\", " +
                "f.LowChloride= \"%20$s\", " +
                "f.Mg= \"%21$s\", " +
                "f.MgUnitId= \"%22$s\", " +
                "f.Mn= \"%23$s\", " +
                "f.MnUnitId= \"%24$s\", " +
                "f.Mo= \"%25$s\", " +
                "f.MoUnitId= \"%26$s\", " +
                "f.N= \"%27$s\", " +
                "f.NUnitId= \"%28$s\", " +
                "f.Na= \"%29$s\", " +
                "f.NaUnitId= \"%30$s\", " +
                "f.NH4= \"%31$s\", " +
                "f.NO3= \"%32$s\", " +
                "f.ODX_Fert_SourceSystem= \"%33$s\", " +
                "f.ODX_Fertilizers_Uri= \"%34$s\", " +
                "f.P= \"%35$s\", " +
                "f.PUnitId= \"%36$s\", " +
                "f.Ph= \"%37$s\", " +
                "f.ProdFamily= \"%38$s\", " +
                "f.FertilizersName= \"%39$s\", " +
                "f.FertilizersId= \"%40$s\", " +
                "f.ProductType= \"%41$s\", " +
                "f.S= \"%42$s\", " +
                "f.SUnitId= \"%43$s\", " +
                "f.Se= \"%44$s\", " +
                "f.SeUnitId= \"%45$s\", " +
                "f.Solubility20C= \"%46$s\", " +
                "f.Solubility5C= \"%47$s\", " +
                "f.SpreaderLoss= \"%48$s\", " +
                "f.SyncId= \"%49$s\", " +
                "f.SyncSource= \"%50$s\", " +
                "f.Tank= \"%51$s\", " +
                "f.Urea= \"%52$s\", " +
                "f.UtilizationN= \"%53$s\", " +
                "f.UtilizationNH4= \"%54$s\", " +
                "f.Zn= \"%55$s\", " +
                "f.ZnUnitId= \"%56$s\"\n" +
                "ON MATCH SET\n" +
                "f.B= \"%3$s\", " +
                "f.BUnitId= \"%4$s\", " +
                "f.Ca= \"%5$s\", " +
                "f.CaUnitId= \"%6$s\", " +
                "f.Co= \"%7$s\", " +
                "f.CoUnitId= \"%8$s\", " +
                "f.Cu= \"%9$s\", " +
                "f.CuUnitId= \"%10$s\", " +
                "f.Density= \"%11$s\", " +
                "f.DhCode= \"%12$s\", " +
                "f.DryMatter= \"%13$s\", " +
                "f.ElectricalConductivity= \"%14$s\", " +
                "f.Fe= \"%15$s\", " +
                "f.FeUnitId= \"%16$s\", " +
                "f.K= \"%17$s\", " +
                "f.KUnitId= \"%18$s\", " +
                "f.LastSync= \"%19$s\", " +
                "f.LowChloride= \"%20$s\", " +
                "f.Mg= \"%21$s\", " +
                "f.MgUnitId= \"%22$s\", " +
                "f.Mn= \"%23$s\", " +
                "f.MnUnitId= \"%24$s\", " +
                "f.Mo= \"%25$s\", " +
                "f.MoUnitId= \"%26$s\", " +
                "f.N= \"%27$s\", " +
                "f.NUnitId= \"%28$s\", " +
                "f.Na= \"%29$s\", " +
                "f.NaUnitId= \"%39$s\", " +
                "f.NH4= \"%31$s\", " +
                "f.NO3= \"%32$s\", " +
                "f.ODX_Fert_SourceSystem= \"%33$s\", " +
                "f.ODX_Fertilizers_Uri= \"%34$s\", " +
                "f.P= \"%35$s\", " +
                "f.PUnitId= \"%36$s\", " +
                "f.Ph= \"%37$s\", " +
                "f.ProdFamily= \"%38$s\", " +
                "f.FertilizersName= \"%39$s\", " +
                "f.FertilizersId= \"%40$s\", " +
                "f.ProductType= \"%41$s\", " +
                "f.S= \"%42$s\", " +
                "f.SUnitId= \"%43$s\", " +
                "f.Se= \"%44$s\", " +
                "f.SeUnitId= \"%45$s\", " +
                "f.Solubility20C= \"%46$s\", " +
                "f.Solubility5C= \"%47$s\", " +
                "f.SpreaderLoss= \"%48$s\", " +
                "f.SyncId= \"%49$s\", " +
                "f.SyncSource= \"%50$s\", " +
                "f.Tank= \"%51$s\", " +
                "f.Urea= \"%52$s\", " +
                "f.UtilizationN= \"%53$s\", " +
                "f.UtilizationNH4= \"%54$s\", " +
                "f.Zn= \"%55$s\", " +
                "f.ZnUnitId= \"%56$s\"\n";

        fertilizers.forEach(fertilizer -> {
            count.incrementAndGet();
            String createFertilizerCommand = String.format(createFertilizerFormat,
                    fertilizer.getClassName(), fertilizer.getUuId(),
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
                    fertilizer.getSource(),
                    fertilizer.getUri(),
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
            writeToGraph(createFertilizerCommand);
        });
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


    public void mergeCountryToRegionRelations(List<Region> regions) {
        AtomicInteger count = new AtomicInteger(0);
        regions.forEach(region -> {
            mergeCountryRegionRelation(region);
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

    public void mergeCropGroupToClassRelations(List<CropClass> classes) {
        AtomicInteger count = new AtomicInteger(0);
        classes.forEach(cropClass -> {
            mergeGroupClassRelation(cropClass);
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

    public void mergeCropClassToSubClassRelations(List<CropSubClass> children) {
        AtomicInteger count = new AtomicInteger(0);
        children.forEach(child -> {
            mergeClassSubClassRelation(child);
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

    private void writeToGraph(String command) {
        if (command.length() == 0) return;

//      ********************************************
//        System.out.println(command);
//      ********************************************

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(command));
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

    public void mergeCropSubClassToVarietyRelations(List<CropVariety> varieties) {
        AtomicInteger count = new AtomicInteger(0);
        varieties.forEach(variety -> {
            mergeSubClassVarietyRelation(variety);
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

    public void mergeCropSubClassToDescriptionRelations(List<CropDescription> descriptions) {
        AtomicInteger count = new AtomicInteger(0);
        descriptions.forEach(description -> {
            mergeSubClassDescriptionRelation(description);
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

    public void mergeCropVarietyToDescriptionRelations(List<CropDescriptionVariety> cropDescVars) {
        AtomicInteger count = new AtomicInteger(0);
        cropDescVars.forEach(descvar -> {
            mergeVarietyDescriptionRelation(descvar);
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

    public void mergeCropDescriptionsToRegionsRelations(List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        cropRegions.forEach(cropRegion -> {
            mergeDescriptionToRegionRelationWithProperties(cropRegion);
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

    public void mergeCropDescriptionsToGrowthScaleRelations(List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropDescription, List<GrowthScale>> createdRelations = new HashMap<>();
        cropRegions.forEach(cropRegion -> {
            mergeDescriptionGrowthScaleRelation(cropRegion);
            System.out.println(count.incrementAndGet() + " CropDescription to GrowthScale relations created");
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

    public void mergeGrowthScaleToStagesRelations(List<GrowthScaleStages> growthScaleStages) {
        AtomicInteger count = new AtomicInteger(0);
        growthScaleStages.forEach(stage -> {
            mergeGrowthScaleToStageRelation(stage);
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

    public void mergeNutrientsToUnitsRelations(List<Nutrient> nutrients) {
        AtomicInteger count = new AtomicInteger(0);
        nutrients.forEach(nutrient -> {
            mergeNutrientToUnitRelation(nutrient);
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

    public void mergeUnitsToConversionsRelations(List<UnitConversion> conversions) {
        AtomicInteger count = new AtomicInteger(0);
        conversions.forEach(conversion -> {
            mergeUnitToConversionRelation(conversion);
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

    public void mergeFertilizersToRegionsRelations(List<FertilizerRegion> fertilizerRegions) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizerRegions.forEach(fr -> {
            mergeFertilizerToRegionRelation(fr);
            System.out.println(count.incrementAndGet() + " Fertilizer to Region relations created");
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

    public void mergeFertilizersToNutrientsRelations(List<Fertilizers> fertilizers, List<Nutrient> nutrients, List<Units> units) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizers.forEach(fertilizer -> {
            Map<String, String> nutrientUnitsContent = fertilizer.getNutrientUnitsContent();
            for (Map.Entry<String, String> entry : nutrientUnitsContent.entrySet()) {
                String nutrientUnitId = entry.getKey();
//                Units unit = (Units) getFromCollectionById(units, nutrientUnitId);
                String nutrientValue = entry.getValue();
                if (!nutrientValue.equals("0.0") &&
                        !nutrientValue.isEmpty()) {
//                    Nutrient nutrient = getNutrientByElementalName(nutrients, unit.getTag());
                    mergeFertilizerToNutrientRelation(fertilizer, nutrientUnitId);
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
                        "empty",
                        "empty",
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

    private GrowthScale getGrowthScaleForDescription(List<CropRegion> cropRegions, List<GrowthScale> growthScales, String descriptionId) {
        Optional<CropRegion> cropRegion = cropRegions.stream()
                .filter(cr -> cr.getDescriptionId().equals(descriptionId))
                .findFirst();
        if (cropRegion.isPresent()) {
            return (GrowthScale) getFromCollectionById(growthScales, cropRegion.get().getGrowthScaleIdRef());
        }
        return new GrowthScale("empty", "empty", "empty", "empty");
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

    private void mergeCountryRegionRelation(Region region) {
        UUID calculatedCountryUUId = computeUUid(region.getSource(), "Country", region.getCountryId());
        String matchCountry = String.format("MATCH (country:Country{ODX_Country_UUId:\"%s\"})\n", calculatedCountryUUId.toString());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String mergeRelation = "MERGE (country)-[:hasRegion]->(region)";
        uploadRelationToDatabase(matchCountry, matchRegion, mergeRelation);
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

    private void mergeGroupClassRelation(CropClass cropClass) {
        UUID calculatedGroupUUId = computeUUid(cropClass.getSource(), "CropGroup", cropClass.getGroupId());
        String matchGroup = String.format("MATCH (group:CropGroup{ODX_CropGroup_UUId:\"%s\"})\n", calculatedGroupUUId.toString());
        String matchClass = String.format("MATCH (class:CropClass{ODX_CropClass_UUId:\"%s\"})\n", cropClass.getUuId());
        String mergeRelation = "MERGE (group)-[:hasCropClass]->(class)";
        uploadRelationToDatabase(matchGroup, matchClass, mergeRelation);
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

    private void mergeClassSubClassRelation(CropSubClass subClass) {
        UUID calculatedCropClassUUId = computeUUid(subClass.getSource(), "CropClass", subClass.getClassId());
        String matchClass = String.format("MATCH (class:CropClass{ODX_CropClass_UUId:\"%s\"})\n", calculatedCropClassUUId.toString());
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", subClass.getUuId());
        String mergeRelation = "MERGE (class)-[:hasCropSubClass]->(subClass)";
        uploadRelationToDatabase(matchClass, matchSubClass, mergeRelation);
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

    private void mergeSubClassVarietyRelation(CropVariety variety) {
        UUID calculatedCropSubClassUUId = computeUUid(variety.getSource(), "CropSubClass", variety.getSubClassId());
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", calculatedCropSubClassUUId.toString());
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", variety.getUuId());
        String mergeRelation = "MERGE (subClass)-[:hasCropVariety]->(variety)";
        uploadRelationToDatabase(matchSubClass, matchVariety, mergeRelation);
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

    private void mergeSubClassDescriptionRelation(CropDescription description) {
        UUID calculatedCropSubClassUUId = computeUUid(description.getSource(), "CropSubClass", description.getSubClassId());
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", calculatedCropSubClassUUId.toString());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String mergeRelation = "MERGE (subClass)-[:hasCropDescription]->(description)";
        uploadRelationToDatabase(matchSubClass, matchDescription, mergeRelation);
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

    private void mergeVarietyDescriptionRelation(CropDescriptionVariety descriptionVariety) {
        UUID calculatedDescriptionUUId = computeUUid("Polaris", "CropDescription", descriptionVariety.getDescId());
        UUID calculatedVarietyUUId = computeUUid("Polaris", "CropVariety", descriptionVariety.getVarId());
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", calculatedVarietyUUId.toString());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", calculatedDescriptionUUId.toString());
        String mergeRelation = String.format("MERGE (variety)-[:hasCropDescription {" +
                        "CV_CropDescriptionId_Ref: \"%s\", " +
                        "CV_CD_UUId_Ref: \"%s\"}]->(description)",
                descriptionVariety.getDescId(),
                calculatedDescriptionUUId.toString());
        uploadRelationToDatabase(matchVariety, matchDescription, mergeRelation);
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
        String matchStage = String.format("MATCH (stage:GrowthScaleStages{ODX_GrowthScaleStages_UUId:\"%s\"})\n", stage.getUuId());
        String createRelation = "CREATE (scale)-[:hasGrowthScaleStages]->(stage)";
        uploadRelationToDatabase(matchScale, matchStage, createRelation);
    }

    private void mergeGrowthScaleToStageRelation(GrowthScaleStages stage) {
        UUID calculatedScaleUUId = computeUUid(stage.getSource(), "GrowthScale", stage.getGrowthScaleId());
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", calculatedScaleUUId.toString());
        String matchStage = String.format("MATCH (stage:GrowthScaleStages{ODX_GrowthScaleStages_UUId:\"%s\"})\n", stage.getUuId());
        String mergeRelation = "MERGE (scale)-[:hasGrowthScaleStages]->(stage)";
        uploadRelationToDatabase(matchScale, matchStage, mergeRelation);
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

    private void mergeDescriptionToRegionRelationWithProperties(CropRegion cropRegion) {
        UUID calculatedDescriptionUUId = computeUUid("Polaris", "CropDescription", cropRegion.getDescriptionId());
        UUID calculatedRegionUUId = computeUUid("Polaris", "Region", cropRegion.getRegionIdRef());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", calculatedDescriptionUUId.toString());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", calculatedRegionUUId.toString());
        String mergeRelation = String.format("MERGE (description)-[:isAvailableIn {" +
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
        uploadRelationToDatabase(matchDescription, matchRegion, mergeRelation);
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

    private void mergeDescriptionGrowthScaleRelation(CropRegion cropRegion) {
        UUID calculatedDescriptionUUId = computeUUid("Polaris", "CropDescription", cropRegion.getDescriptionId());
        UUID calculatedGrowthScaleUUId = computeUUid("Polaris", "GrowthScale", cropRegion.getGrowthScaleIdRef());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", calculatedDescriptionUUId.toString());
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", calculatedGrowthScaleUUId.toString());
        String mergeRelation = "MERGE (description)-[:hasGrowthScale]->(scale)\n";
        uploadRelationToDatabase(matchDescription, matchScale, mergeRelation);
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

    private void mergeNutrientToUnitRelation(Nutrient nutrient) {
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String matchUnit = String.format("MATCH (unit:Units{UnitsName:\"%s\"})\n", nutrient.getElementalName());
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

    private void mergeUnitToConversionRelation(UnitConversion conversion) {
        UUID calculatedUnitsUUId = computeUUid(conversion.getSource(), "Units", conversion.getUnitIdRef());
        String matchUnit = String.format("MATCH (unit:Units{ODX_Units_UUId:\"%s\"})\n", calculatedUnitsUUId.toString());
        String matchConversion = String.format("MATCH (conversion:UnitConversion{ODX_UnitConversion_UUId:\"%s\"})\n", conversion.getUuId());
        String mergeRelation = "MERGE (unit)-[:hasUnitConversion]->(conversion)";
        uploadRelationToDatabase(matchUnit, matchConversion, mergeRelation);
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
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", fertilizer.getUuId());
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

    private void mergeFertilizerToRegionRelation(FertilizerRegion fertilizerRegion) {
        UUID calculatedFertilizerUUId = computeUUid("Polaris", "Fertilizers", fertilizerRegion.getProductId());
        UUID calculatedRegionUUId = computeUUid("Polaris", "Region", fertilizerRegion.getRegionId());
        UUID calculatedCountryUUId = computeUUid("Polaris", "Country", fertilizerRegion.getCountryId());
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", calculatedFertilizerUUId.toString());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", calculatedRegionUUId.toString());
        String createRelation = String.format("CREATE (fertilizer)-[:isAvailableIn{" +
                        "ApplicationTags: \"%s\", " +
                        "Prod_CountryId_Ref: \"%s\"," +
                        "Prod_RegionId_Ref: \"%s\"," +
                        "ProdCountry_UUId_Ref: \"%s\", " +
                        "ProdRegion_UUId_Ref: \"%s\", " +
                        "LocalizedName: \"%s\"," +
                        "IsAvailable: \"%s\"}]->(region)\n",
                fertilizerRegion.getApplicationTags(),
                fertilizerRegion.getCountryId(),
                fertilizerRegion.getRegionId(),
                calculatedCountryUUId.toString(),
                calculatedRegionUUId.toString(),
                fertilizerRegion.getLocalizedName(),
                fertilizerRegion.getIsAvailable());
        uploadRelationToDatabase(matchFertilizer, matchRegion, createRelation);
    }

    private void createFertilizerToRegionRelationWithBuilders(Fertilizers fertilizer,
                                                              Region region,
                                                              StringBuilder matchBuilder,
                                                              StringBuilder createBuilder,
                                                              AtomicInteger count) {
        String matchFertilizer = String.format("MATCH (fertilizer_%d:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", count.get(), fertilizer.getUuId());
        String matchRegion = String.format("MATCH (region_%d:Region{ODX_Region_UUId:\"%s\"})\n", count.get(), region.getUuId());
        String createRelation = String.format("CREATE (fertilizer_%1$d)-[:isAvailableIn]->(region_%1$d)\n", count.get());
        matchBuilder.append(matchFertilizer).append(matchRegion);
        createBuilder.append(createRelation);
    }

    private void createFertilizerToNutrientRelation(Fertilizers fertilizer, Nutrient nutrient) {
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String createRelation = "CREATE (fertilizer)-[:hasProdNutrient]->(nutrient)";
        uploadRelationToDatabase(matchFertilizer, matchNutrient, createRelation);
    }

    private void mergeFertilizerToNutrientRelation(Fertilizers fertilizer, String unitsId) {
        UUID calculatedUnitsUUId = computeUUid("Polaris", "Units", unitsId);
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchNutrient = String.format(
                "MATCH (units:Units{ODX_Units_UUId:\"%s\"})\n" +
                "MATCH (nutrient:Nutrient{ElementalName:units.UnitsTags})\n", calculatedUnitsUUId.toString());
        String mergeRelation = "MERGE (fertilizer)-[:hasProdNutrient]->(nutrient)";
        uploadRelationToDatabase(matchFertilizer, matchNutrient, mergeRelation);
    }

    private void createFertilizerToNutrientRelationWithBuilders(Fertilizers fertilizer,
                                                                Nutrient nutrient,
                                                                StringBuilder matchBuilder,
                                                                StringBuilder createBuilder,
                                                                AtomicInteger count) {
        String matchFertilizer = String.format("MATCH (fertilizer_%d:Fertilizers{ODX_Fertilizers_UUId:\"%s\"})\n", count.get(), fertilizer.getUuId());
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
        commands.add("CREATE CONSTRAINT fertilizer_constraint ON (f:Fertilizers) ASSERT f.ODX_Fertilizers_UUId IS UNIQUE\n");

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
