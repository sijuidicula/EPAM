package com.yara.ss.loader;

import com.yara.ss.domain.*;
import com.yara.ss.reader.ExcelWorkbookReader;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PropertyGraphUploader implements AutoCloseable {

//    private static final String URI = "bolt+s://odx-storage.yara.com:7687";
//    private static final String USER = "neo4j";
//    private static final String PASSWORD = "MjY4Yjc0OTNmNjZmNzgxNDYyOWMzNDAz";
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "1234";

    private final Driver driver;

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
                "ODX_CS_UUId_Ref: \"%s\", " +
                "CountryId: \"%s\", " +
                "name: \"%s\", " +
                "CountryName: \"%s\", " +
                "ProductSetCode: \"%s\", " +
                "M49Code: \"%s\", " +
                "ISO2Code: \"%s\", " +
                "ISO3Code: \"%s\", " +
                "UN: \"%s\", " +
                "FIPS: \"%s\"})";
        try (Session session = driver.session()) {
            countries.forEach(country -> {
                if (!existsInDatabase(country)) {
                    System.out.println("Uploading Country # " + count.incrementAndGet());
                    session.writeTransaction(tx -> tx.run(String.format(createCountryFormat,
                            createNodeName(country.getName()),
                            country.getClassName(), country.getUuId(),
                            createOdxUri(country),
                            "dummy_CS_UUId_Ref",
                            country.getId(),
                            country.getName(),
                            country.getName(),
                            country.getProductSetCode(),
                            "dummy_M49_code",
                            "dummy_ISO_2_code",
                            "dummy_ISO_3_code",
                            "dummy_UN",
                            "dummy_FIPS")));
                }
            });
        }
        System.out.println("Country uploading completed");
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
                "name: \"%s\", " +
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
                                region.getName(),
                                region.getName()));
                    });
                }
            });
        }
        System.out.println("Region uploading completed");
        System.out.println(count.get() + " Regions uploaded");
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

    public void uploadCropGroups(List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String createGroupFormat = "CREATE (%s:%s{" +
                "CG_FAOId: \"%s\", " +
                "CG_MediaUri: \"%s\", " +
                "CropGroupId: \"%s\", " +
                "name: \"%s\", " +
                "CropGroupName: \"%s\", " +
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
                        group.getName(),
                        group.getUuId()));
            }));
        }
        System.out.println("CropGroup uploading completed");
        System.out.println(count.get() + " CropGroups uploaded");
    }

    public void uploadCropClasses(List<CropClass> cropClasses, List<CropGroup> cropGroups) {
        AtomicInteger count = new AtomicInteger(0);
        String createClassFormat = "CREATE (%s:%s{" +
                "ODX_CropClass_UUId: \"%s\", " +
                "CropClassId: \"%s\", " +
                "CropGroupId_Ref: \"%s\", " +
                "ODX_CG_UUId_Ref: \"%s\", " +
                "CC_FAOId: \"%s\", " +
                "CC_MediaUri: \"%s\", " +
                "name: \"%s\", " +
                "CropClassName: \"%s\"})\n";
        try (Session session = driver.session()) {
            cropClasses.forEach(cropClass -> session.writeTransaction(tx -> {
                System.out.println("Uploading CropClass # " + count.incrementAndGet());
                CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
                String newClassName = createNodeName(cropClass.getName());
                return tx.run(String.format(createClassFormat,
                        newClassName, cropClass.getClassName(),
                        cropClass.getUuId(),
                        cropClass.getId(),
                        cropClass.getGroupId(),
                        cropGroup.getUuId(),
                        cropClass.getFaoId(),
                        cropClass.getMediaUri(),
                        cropClass.getName(),
                        cropClass.getName()));
            }));
        }
        System.out.println("CropClass uploading completed");
        System.out.println(count.get() + " CropClasses uploaded");
    }

    public void uploadCropSubClasses(List<CropSubClass> cropSubClasses, List<CropClass> cropClasses) {
        String createSubClassFormat = "CREATE (%s:%s{" +
                "ODX_CropSubClass_UUId: \"%s\", " +
                "CropSubClassId: \"%s\", " +
                "CropClassId_Ref: \"%s\", " +
                "ODX_CC_UUId_Ref: \"%s\", " +
                "CSC_FAOId: \"%s\", " +
                "CSC_MediaUri: \"%s\", " +
                "name: \"%s\", " +
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
                        subClass.getId(),
                        subClass.getClassId(),
                        cropClass.getUuId(),
                        subClass.getFaoId(),
                        subClass.getMediaUri(),
                        subClass.getName(),
                        subClass.getName()));
            }));
        }
        System.out.println("CropSubClass uploading completed");
        System.out.println(count.get() + " CropSubClasses uploaded");
    }

    public void uploadCropVarieties(List<CropVariety> cropVarieties, List<CropSubClass> cropSubClasses) {
        AtomicInteger count = new AtomicInteger(0);
        String createVarietyFormat = "CREATE (%s:%s{" +
                "ODX_CropVariety_UUId: \"%s\", " +
                "ODX_CV_Uri: \"%s\", " +
                "CV_CropDescriptionId_Ref: \"%s\", " +
                "CV_CD_UUId_Ref: \"%s\", " +
                "CV_CropSubClassId_Ref: \"%s\", " +
                "CV_CSC_UUId_Ref: \"%s\", " +
                "CropVarietyId: \"%s\", " +
                "name: \"%s\", " +
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
                        "dummy_CV_CropDescriptionId_Ref",
                        "dummy_CV_CD_UUId_Ref",
                        variety.getSubClassId(),
                        subClass.getUuId(),
                        variety.getId(),
                        variety.getName(),
                        variety.getName()));
            }));
        }
        System.out.println("CropVariety uploading completed");
        System.out.println(count.get() + " CropVariety uploaded");
    }

    public void uploadCropDescriptions(List<CropDescription> cropDescriptions, List<CropSubClass> cropSubClasses) {
        String createCreateVarietyCommandFormat = "CREATE (%s:%s{" +
                "ODX_CropDescription_UUId: \"%s\", " +
                "CD_MediaUri: \"%s\", " +
                "ChlorideSensitive: \"%s\", " +
                "CropDescriptionId: \"%s\", " +
                "name: \"%s\", " +
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
                return tx.run(String.format(createCreateVarietyCommandFormat,
                        descriptionNodeName, description.getClassName(),
                        description.getUuId(),
                        description.getMediaUri(),
                        description.isChlorideSensitive(),
                        description.getId(),
                        description.getName(),
                        description.getName(),
                        description.getSubClassId(),
                        subClass.getUuId(),
                        "dummy_Polaris"));
            }));
        }
        System.out.println("CropDescription uploading completed");
        System.out.println(count.get() + " CropDescriptions uploaded");
    }

    public void uploadGrowthScales(List<GrowthScale> growthScales) {
        String createGrowthScaleCommandFormat = "CREATE (%s:%s{" +
                "ODX_GrowthScale_UUId: \"%s\", " +
                "GrowthScaleId: \"%s\", " +
                "name: \"%s\", " +
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
                        scale.getName(),
                        createOdxUri(scale)));
            }));
        }
        System.out.println("GrowthScale uploading completed");
        System.out.println(count.get() + " GrowthScales uploaded");
    }

    public void uploadGrowthScaleStages(List<GrowthScaleStage> growthScaleStages, List<GrowthScale> growthScales) {
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
                        "dummy_Polaris",
                        stage.getOrdinal()));
            }));
        }
        System.out.println("GrowthScaleStage uploading completed");
        System.out.println(count.get() + " GrowthScaleStage uploaded");
    }

    public void uploadNutrients(List<Nutrient> nutrients) {
        String createNutrientsCommandFormat = "CREATE (%s:%s{" +
                "ODX_Nutrient_UUId: \"%s\", " +
                "ODX_Nutrient_Uri: \"%s\", " +
                "NutrientId: \"%s\", " +
                "NutrientName: \"%s\", " +
                "name: \"%s\", " +
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
                        nutrient.getElementalName(),
                        nutrient.getNutrientOrdinal(),
                        "dummy_Polaris"));
            }));
        }
        System.out.println("Nutrient uploading completed");
        System.out.println(count.get() + " Nutrients uploaded");
    }

    public void uploadUnits(List<Unit> units) {
        String createUnitCommandFormat = "CREATE (%s:%s{" +
                "ODX_Unit_Uri: \"%s\", " +
                "UnitId: \"%s\", " +
                "name: \"%s\", " +
                "UnitName: \"%s\", " +
                "UnitTags: \"%s\"})\n";

        AtomicInteger count = new AtomicInteger(0);
        try (Session session = driver.session()) {
            units.forEach(unit -> session.writeTransaction(tx -> {
                System.out.println("Uploading Unit # " + count.incrementAndGet());
                String unitNodeName = createNodeName(unit.getName());
                return tx.run(String.format(createUnitCommandFormat,
                        unitNodeName, unit.getClassName(),
                        createOdxUri(unit),
                        unit.getId(),
                        unit.getName(),
                        unit.getName(),
                        unit.getTag()));
            }));
        }
        System.out.println("Unit uploading completed");
        System.out.println(count.get() + " Units uploaded");
    }

    public void uploadUnitConversions(List<UnitConversion> conversions, List<Unit> units) {
        String createUnitConversionCommandFormat = "CREATE (%s:%s{" +
                "ODX_UnitConversion_UUId: \"%s\", " +
                "ODX_UC_Uri: \"%s\", " +
                "name: \"%s\", " +
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
                Unit convertToUnit = (Unit) getFromCollectionById(units, conversion.getConvertToUnitId());
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

    public void uploadFertilizers(List<Fertilizer> fertilizers,
                                  List<FertilizerRegion> fertilizerRegions,
                                  List<Country> countries,
                                  List<Region> regions) {
        String createFertilizerCommandFormat = "CREATE (%s:%s{" +
                "Application_tags: \"%s\", " +
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
                "IsAvailable: \"%s\", " +
                "K: \"%s\", " +
                "KUnitId: \"%s\", " +
                "LastSync: \"%s\", " +
                "LocalizedName: \"%s\", " +
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
                "Prod_CountryId_Ref: \"%s\", " +
                "Prod_RegionId_Ref: \"%s\", " +
                "ProdCountry_UUId_Ref: \"%s\", " +
                "ProdFamily: \"%s\", " +
                "name: \"%s\", " +
                "ProdName: \"%s\", " +
                "ProdRegion_UUId_Ref: \"%s\", " +
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
                FertilizerRegion fertilizerRegion = getFertilizerRegionByProductId(fertilizerRegions, fertilizer.getId());
                Country country = (Country) getFromCollectionById(countries, fertilizerRegion.getCountryId());
                Region region = (Region) getFromCollectionById(regions, fertilizerRegion.getRegionId());
                String nodeName = createNodeName(fertilizer.getName());
                return tx.run(String.format(createFertilizerCommandFormat,
                        nodeName, fertilizer.getClassName(),
                        fertilizerRegion.getApplicationTags(),
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
                        fertilizerRegion.getIsAvailable(),
                        fertilizer.getK(),
                        fertilizer.getKUnitId(),
                        fertilizer.getLastSync(),
                        fertilizerRegion.getLocalizedName(),
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
                        "dummy_ODX_Fert_SourceSystem",
                        createOdxUri(fertilizer),
                        fertilizer.getUuId(),
                        fertilizer.getP(),
                        fertilizer.getPUnitId(),
                        fertilizer.getPh(),
                        fertilizerRegion.getCountryId(),
                        fertilizerRegion.getRegionId(),
                        country.getUuId(),
                        fertilizer.getFamily(),
                        fertilizer.getName(),
                        fertilizer.getName(),
                        region.getUuId(),
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

    private FertilizerRegion getFertilizerRegionByProductId(List<FertilizerRegion> fertilizerRegions, String fertilizerId) {
        return fertilizerRegions.stream()
                .filter(fr -> fr.getProductId().equals(fertilizerId))
                .findFirst()
                //This is done because some productIds does not exist in Fertilizer_Reg file
                .orElse(new FertilizerRegion(
                        "ee0b0aa2-849b-41af-b5ea-06ed98e8e178",
                        "2442570a-62d2-4719-b8d6-2bbc3daec9d6",
                        "08dfead4-392e-4fe6-8966-505e6f16d7a4",
                        "[NULL]",
                        "c79ee3e0-71bd-40b4-ba6d-2116e44ef0cc",
                        "TRUE",
                        ""));
//                .orElseThrow(() -> new NoSuchElementException(String.format("No element with id %s in FertilizerRegions collection", fertilizerId)));
    }

    public void createCountryToRegionRelations(List<Country> countries, List<Region> regions) {
        AtomicInteger count = new AtomicInteger(0);
        Map<Country, List<Region>> map = getCountryRegionMap(countries, regions);
        for (Map.Entry<Country, List<Region>> entry : map.entrySet()) {
            Country country = entry.getKey();
            List<Region> countryRegions = entry.getValue();
            countryRegions.forEach(region -> {
                createCountryRegionRelation(country, region);
                System.out.println(count.incrementAndGet() + " Country to Region relations created");
            });
        }
        System.out.println("Country-Region relation uploading completed");
        System.out.println(count.get() + " Country-Region relations uploaded");
    }

    public void createCropGroupToClassRelations(List<CropGroup> groups, List<CropClass> classes) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropGroup, List<CropClass>> map = getCropGroupClassMap(groups, classes);
        for (Map.Entry<CropGroup, List<CropClass>> entry : map.entrySet()) {
            CropGroup group = entry.getKey();
            List<CropClass> groupClasses = entry.getValue();
            groupClasses.forEach(cropClass -> {
                createGroupClassRelation(group, cropClass);
                System.out.println(count.incrementAndGet() + " CropGroup to CropClass relations created");
            });
        }
        System.out.println("Group-Class relation uploading completed");
        System.out.println(count.get() + " Group-Class relations uploaded");
    }

    public void createCropClassToSubClassRelations(List<CropClass> ancestors, List<CropSubClass> children) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropClass, List<CropSubClass>> map = getAncestorSubClassMap(ancestors, children);
        for (Map.Entry<CropClass, List<CropSubClass>> entry : map.entrySet()) {
            CropClass ancestor = entry.getKey();
            List<CropSubClass> relatedChildren = entry.getValue();
            relatedChildren.forEach(child -> {
                createClassSubClassRelation(ancestor, child);
                System.out.println(count.incrementAndGet() + " Class to SubClass relations created");
            });
        }
        System.out.println("Class-SubClass relation uploading completed");
        System.out.println(count.get() + " Class-SubClass relations uploaded");
    }

    public void createCropSubClassToVarietyRelations(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropSubClass, List<CropVariety>> map = getSubClassesVarietiesMap(subClasses, varieties);
        for (Map.Entry<CropSubClass, List<CropVariety>> entry : map.entrySet()) {
            CropSubClass subClass = entry.getKey();
            List<CropVariety> relatedVarieties = entry.getValue();
            relatedVarieties.forEach(variety -> {
                createSubClassVarietyRelation(subClass, variety);
                System.out.println(count.incrementAndGet() + " CSC to CV relations created");
            });
        }
        System.out.println("CropSubClass-CropVariety relation uploading completed");
        System.out.println(count.get() + " CropSubClass-CropVariety relations uploaded");
    }

    public void createCropSubClassToDescriptionRelations(List<CropSubClass> subClasses, List<CropDescription> descriptions) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropSubClass, List<CropDescription>> map = getSubClassesDescriptionsMap(subClasses, descriptions);
        for (Map.Entry<CropSubClass, List<CropDescription>> entry : map.entrySet()) {
            CropSubClass subClass = entry.getKey();
            List<CropDescription> relatedDescriptions = entry.getValue();
            relatedDescriptions.forEach(description -> {
                createSubClassDescriptionRelation(subClass, description);
                System.out.println(count.incrementAndGet() + " CSC to CD relations created");
            });
        }
        System.out.println("CropSubClass-CropDescription relation uploading completed");
        System.out.println(count.get() + " CropSubClass-CropDescription relations uploaded");
    }

    public void createCropVarietyToDescriptionRelations(List<CropVariety> cropVarieties,
                                                        List<CropDescription> cropDescriptions,
                                                        List<CropDescriptionVariety> cropDescVars) {
        AtomicInteger count = new AtomicInteger(0);
        Map<CropDescription, List<CropVariety>> map = getVarietiesDescriptionsMap(cropVarieties, cropDescriptions, cropDescVars);
        for (Map.Entry<CropDescription, List<CropVariety>> entry : map.entrySet()) {
            CropDescription description = entry.getKey();
            List<CropVariety> relatedVarieties = entry.getValue();
            relatedVarieties.forEach(variety -> {
                createVarietyDescriptionRelation(variety, description);
                System.out.println(count.incrementAndGet() + " CV to CD relations created");
            });
        }
        System.out.println("CropVariety-CropDescription relation uploading completed");
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

    public void createCropDescriptionsToGrowthScaleRelations(List<CropDescription> cropDescriptions,
                                                             List<GrowthScale> growthScales,
                                                             List<CropRegion> cropRegions) {
        AtomicInteger count = new AtomicInteger(0);
        Map<GrowthScale, List<CropDescription>> map = getGrowthScalesDescriptionsMap(cropDescriptions, growthScales, cropRegions);
        for (Map.Entry<GrowthScale, List<CropDescription>> entry : map.entrySet()) {
            GrowthScale scale = entry.getKey();
            List<CropDescription> relatedDescriptions = entry.getValue();
            relatedDescriptions.forEach(description -> {
                createDescriptionGrowthScaleRelation(description, scale);
                System.out.println(count.incrementAndGet() + " CropDescription to GrowthScale relations created");
            });
        }
        System.out.println("CropDescription-GrowthScale relation uploading completed");
        System.out.println(count.get() + " CropDescription-GrowthScale relations uploaded");
    }

    public void createGrowthScaleToStagesRelations(List<GrowthScale> growthScales, List<GrowthScaleStage> growthScaleStages) {
        AtomicInteger count = new AtomicInteger(0);
        Map<GrowthScale, List<GrowthScaleStage>> map = getGrowthScalesToStagesMap(growthScales, growthScaleStages);
        for (Map.Entry<GrowthScale, List<GrowthScaleStage>> entry : map.entrySet()) {
            GrowthScale scale = entry.getKey();
            List<GrowthScaleStage> stages = entry.getValue();
            stages.forEach(stage -> {
                createGrowthScaleToStageRelation(scale, stage);
                System.out.println(count.incrementAndGet() + " GS to GSS relations created");
            });
        }
        System.out.println("GrowthScale-GrowthScaleStage relation uploading completed");
        System.out.println(count.get() + " GrowthScale-GrowthScaleStage relations uploaded");
    }

    public void createNutrientsToUnitsRelations(List<Nutrient> nutrients, List<Unit> units) {
        AtomicInteger count = new AtomicInteger(0);
        nutrients.forEach(nutrient -> {
            Unit unit = getUnitByName(units, nutrient.getElementalName());
            createNutrientToUnitRelation(nutrient, unit);
            System.out.println(count.incrementAndGet() + " Nutrient to Unit relations created");
        });
        System.out.println("Nutrient-Unit relation uploading completed");
        System.out.println(count.get() + " Nutrient-Unit relations uploaded");
    }

    public void createUnitsToConversionsRelations(List<Unit> units, List<UnitConversion> conversions) {
        AtomicInteger count = new AtomicInteger(0);
        conversions.forEach(conversion -> {
            Unit unit = (Unit) getFromCollectionById(units, conversion.getUnitIdRef());
            createUnitToConversionRelation(unit, conversion);
            System.out.println(count.incrementAndGet() + " Unit to UnitConversion relations created");
        });
        System.out.println("Unit-UnitConversion relation uploading completed");
        System.out.println(count.get() + " Unit-UnitConversion relations uploaded");
    }

    public void createFertilizersToRegionsRelations(List<Fertilizer> fertilizers, List<Region> regions, List<FertilizerRegion> fertilizerRegions) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizerRegions.forEach(fr -> {
            Fertilizer fertilizer = (Fertilizer) getFromCollectionById(fertilizers, fr.getProductId());
            Region region = (Region) getFromCollectionById(regions, fr.getRegionId());
            createFertilizerToRegionRelation(fertilizer, region);
            System.out.println(count.incrementAndGet() + " Fertilizer to Region relations created");
        });
        System.out.println("Fertilizer-Region relation uploading completed");
        System.out.println(count.get() + " Fertilizer-Region relations uploaded");
    }

    public void createFertilizersToNutrientsRelations(List<Fertilizer> fertilizers, List<Nutrient> nutrients, List<Unit> units) {
        AtomicInteger count = new AtomicInteger(0);
        fertilizers.forEach(fertilizer -> {
            Map<String, String> nutrientUnitsContent = fertilizer.getNutrientUnitsContent();
            for (Map.Entry<String, String> entry : nutrientUnitsContent.entrySet()) {
                String nutrientUnitId = entry.getKey();
                Unit unit = (Unit) getFromCollectionById(units, nutrientUnitId);
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

    private void appendCropClassCommand(StringBuilder builder, CropClass cropClass, String newClassName, CropGroup cropGroup) {
        String createClassFormat = "CREATE (%s:%s{" +
                "CropClassId: \"%s\", " +
                "CropGroupId_Ref: \"%s\", " +
                "CC_FAOId: \"%s\", " +
                "CC_MediaUri: \"%s\", " +
                "name: \"%s\", " +
                "CropClassName: \"%s\", " +
                "ODX_CropClass_UUId: \"%s\", " +
                "ODX_CropClass_URI: \"%s\", " +
                "ODX_CG_UUId_Ref: \"%s\"})\n";
        String createClassCommand = String.format(createClassFormat,
                newClassName, cropClass.getClassName(),
                cropClass.getId(),
                cropClass.getGroupId(),
                cropClass.getFaoId(),
                cropClass.getMediaUri(),
                cropClass.getName(),
                cropClass.getName(),
                cropClass.getUuId(),
                createOdxUri(cropClass),
                cropGroup.getUuId());
        builder.append(createClassCommand);
    }

    private void appendGroupClassRelationCommand(StringBuilder builder, String cropGroupNodeName, String cropClassNodeName) {
        String createRelationFormat = "CREATE (%s)-[:HAS_CROP_CLASS]->(%s)";
        builder.append(String.format(createRelationFormat, cropGroupNodeName, cropClassNodeName));
    }

    private void createCountryRegionRelation(Country country, Region region) {

        String matchCountry = String.format("MATCH (country:Country{ODX_Country_UUId:\"%s\"})\n", country.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = "CREATE (country)-[:HAS_REGION]->(region)";

        StringBuilder builder = new StringBuilder();
        builder.append(matchCountry).append(matchRegion).append(createRelation);

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private void createGroupClassRelation(CropGroup group, CropClass cropClass) {
        String matchGroup = String.format("MATCH (group:CropGroup{ODX_CropGroup_UUId:\"%s\"})\n", group.getUuId());
        String matchClass = String.format("MATCH (class:CropClass{ODX_CropClass_UUId:\"%s\"})\n", cropClass.getUuId());
        String createRelation = "CREATE (group)-[:HAS_CROP_CLASS]->(class)";
        uploadRelationToDatabase(matchGroup, matchClass, createRelation);
    }

    private void createClassSubClassRelation(CropClass ancestor, CropSubClass child) {
        String matchAncestor = String.format("MATCH (ancestor:CropClass{ODX_CropClass_UUId:\"%s\"})\n", ancestor.getUuId());
        String matchChild = String.format("MATCH (child:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", child.getUuId());
        String createRelation = "CREATE (ancestor)-[:HAS_CROP_SUB_CLASS]->(child)";
        uploadRelationToDatabase(matchAncestor, matchChild, createRelation);
    }

    private void createSubClassVarietyRelation(CropSubClass subClass, CropVariety variety) {
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", subClass.getUuId());
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", variety.getUuId());
        String createRelation = "CREATE (subClass)-[:HAS_CROP_VARIETY]->(variety)";
        uploadRelationToDatabase(matchSubClass, matchVariety, createRelation);
    }

    private void createSubClassDescriptionRelation(CropSubClass subClass, CropDescription description) {
        String matchSubClass = String.format("MATCH (subClass:CropSubClass{ODX_CropSubClass_UUId:\"%s\"})\n", subClass.getUuId());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String createRelation = "CREATE (subClass)-[:HAS_CROP_DESCRIPTION]->(description)";
        uploadRelationToDatabase(matchSubClass, matchDescription, createRelation);
    }

    private void createVarietyDescriptionRelation(CropVariety variety, CropDescription description) {
        String matchVariety = String.format("MATCH (variety:CropVariety{ODX_CropVariety_UUId:\"%s\"})\n", variety.getUuId());
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String createRelation = "CREATE (variety)-[:HAS_CROP_DESCRIPTION]->(description)";
        uploadRelationToDatabase(matchVariety, matchDescription, createRelation);
    }

    private void createGrowthScaleToStageRelation(GrowthScale scale, GrowthScaleStage stage) {
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", scale.getUuId());
        String matchStage = String.format("MATCH (stage:GrowthScaleStage{ODX_GrowthScaleStage_UUId:\"%s\"})\n", stage.getUuId());
        String createRelation = "CREATE (scale)-[:HAS_GROWTH_SCALE_STAGE]->(stage)";
        uploadRelationToDatabase(matchScale, matchStage, createRelation);
    }

    private void createDescriptionRegionRelation(CropDescription description, Region region) {
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = "CREATE (description)-[:IS_AVAILABLE_IN]->(region)";
        uploadRelationToDatabase(matchDescription, matchRegion, createRelation);
    }

    private void createDescriptionToRegionRelationWithProperties(CropRegion cropRegion,
                                                                 CropDescription description,
                                                                 Region region) {
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = String.format("CREATE (description)-[:IS_AVAILABLE_IN {" +
                        "AdditionalProperties: \"%s\", " +
                        "CD_CountryIdRef: \"%s\", " +
                        "CD_GrowthScaleId_Ref: \"%s\", " +
                        "DefaultSeedingDate: \"%s\", " +
                        "DefaultHarvestDate: \"%s\", " +
                        "DefaultYield: \"%s\", " +
                        "YieldBaseUnitId: \"%s\", " +
                        "DemandBaseUnitId: \"%s\"" +
                        "}]->(region)",
                cropRegion.getAdditionalProperties().replace("\"", ""),
                cropRegion.getCountryIdRef(),
                cropRegion.getGrowthScaleIdRef(),
                cropRegion.getDefaultSeedingDate(),
                cropRegion.getDefaultHarvestDate(),
                cropRegion.getDefaultYield(),
                cropRegion.getYieldBaseUnitId(),
                cropRegion.getDemandBaseUnitId());
        uploadRelationToDatabase(matchDescription, matchRegion, createRelation);
    }


    private void createDescriptionGrowthScaleRelation(CropDescription description, GrowthScale scale) {
        String matchDescription = String.format("MATCH (description:CropDescription{ODX_CropDescription_UUId:\"%s\"})\n", description.getUuId());
        String matchScale = String.format("MATCH (scale:GrowthScale{ODX_GrowthScale_UUId:\"%s\"})\n", scale.getUuId());
        String createRelation = "CREATE (description)-[:HAS_GROWTH_SCALE]->(scale)";
        uploadRelationToDatabase(matchDescription, matchScale, createRelation);
    }

    private void createNutrientToUnitRelation(Nutrient nutrient, Unit unit) {
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String matchUnit = String.format("MATCH (unit:Unit{UnitName:\"%s\"})\n", unit.getName());
        String createRelation = "CREATE (nutrient)-[:HAS_NUTRIENT_UNIT]->(unit)";
        uploadRelationToDatabase(matchNutrient, matchUnit, createRelation);
    }

    private void createUnitToConversionRelation(Unit unit, UnitConversion conversion) {
        String matchUnit = String.format("MATCH (unit:Unit{UnitName:\"%s\"})\n", unit.getName());
        String matchConversion = String.format("MATCH (conversion:UnitConversion{ODX_UnitConversion_UUId:\"%s\"})\n", conversion.getUuId());
        String createRelation = "CREATE (unit)-[:HAS_UNIT_CONVERSION]->(conversion)";
        uploadRelationToDatabase(matchUnit, matchConversion, createRelation);
    }

    private void createFertilizerToRegionRelation(Fertilizer fertilizer, Region region) {
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizer{ODX_Fertilizer_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchRegion = String.format("MATCH (region:Region{ODX_Region_UUId:\"%s\"})\n", region.getUuId());
        String createRelation = "CREATE (fertilizer)-[:IS_AVAILABLE_IN]->(region)";
        uploadRelationToDatabase(matchFertilizer, matchRegion, createRelation);
    }

    private void createFertilizerToNutrientRelation(Fertilizer fertilizer, Nutrient nutrient) {
        String matchFertilizer = String.format("MATCH (fertilizer:Fertilizer{ODX_Fertilizer_UUId:\"%s\"})\n", fertilizer.getUuId());
        String matchNutrient = String.format("MATCH (nutrient:Nutrient{ODX_Nutrient_UUId:\"%s\"})\n", nutrient.getUuId());
        String createRelation = "CREATE (fertilizer)-[:HAS_PROD_NUTRIENT]->(nutrient)";
        uploadRelationToDatabase(matchFertilizer, matchNutrient, createRelation);
    }

    private void uploadRelationToDatabase(String subject, String object, String predicate) {
        StringBuilder builder = new StringBuilder();
        builder.append(subject).append(object).append(predicate);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(builder.toString()));
        }
    }

    private Map<CropSubClass, List<CropVariety>> getSubClassesVarietiesMap(List<CropSubClass> subClasses, List<CropVariety> varieties) {
        Map<CropSubClass, List<CropVariety>> map = new HashMap();
        varieties.forEach(variety -> {
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, variety.getSubClassId());
            List<CropVariety> relatedVarieties = new ArrayList<>();
            if (map.containsKey(subClass)) {
                relatedVarieties = map.get(subClass);
            }
            relatedVarieties.add(variety);
            map.put(subClass, relatedVarieties);
        });
        return map;
    }

    private Map<CropSubClass, List<CropDescription>> getSubClassesDescriptionsMap(List<CropSubClass> subClasses, List<CropDescription> descriptions) {
        Map<CropSubClass, List<CropDescription>> map = new HashMap();
        descriptions.forEach(description -> {
            CropSubClass subClass = (CropSubClass) getFromCollectionById(subClasses, description.getSubClassId());
            List<CropDescription> relatedDescriptions = new ArrayList<>();
            if (map.containsKey(subClass)) {
                relatedDescriptions = map.get(subClass);
            }
            relatedDescriptions.add(description);
            map.put(subClass, relatedDescriptions);
        });
        return map;
    }

    private Map<CropDescription, List<CropVariety>> getVarietiesDescriptionsMap(List<CropVariety> varieties,
                                                                                List<CropDescription> descriptions,
                                                                                List<CropDescriptionVariety> cropDescVars) {
        Map<CropDescription, List<CropVariety>> map = new HashMap();
        cropDescVars.forEach(descvar -> {
            CropDescription description = (CropDescription) getFromCollectionById(descriptions, descvar.getDescId());
            CropVariety variety = (CropVariety) getFromCollectionById(varieties, descvar.getVarId());
            List<CropVariety> relatedVarieties = new ArrayList<>();
            if (map.containsKey(description)) {
                relatedVarieties = map.get(description);
            }
            relatedVarieties.add(variety);
            map.put(description, relatedVarieties);
        });
        return map;
    }

    private Map<Region, List<CropDescription>> getDescriptionsRegionsMap(List<CropDescription> cropDescriptions,
                                                                         List<Region> regions,
                                                                         List<CropRegion> cropRegions) {
        Map<Region, List<CropDescription>> map = new HashMap();
        cropRegions.forEach(cr -> {
            Region region = (Region) getFromCollectionById(regions, cr.getRegionIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cr.getDescriptionId());
            List<CropDescription> relatedDescriptions = new ArrayList<>();
            if (map.containsKey(region)) {
                relatedDescriptions = map.get(region);
            }
            relatedDescriptions.add(description);
            map.put(region, relatedDescriptions);
        });
        return map;
    }

    private Map<GrowthScale, List<CropDescription>> getGrowthScalesDescriptionsMap(List<CropDescription> cropDescriptions,
                                                                                   List<GrowthScale> growthScales,
                                                                                   List<CropRegion> cropRegions) {
        Map<GrowthScale, List<CropDescription>> map = new HashMap();
        cropRegions.forEach(cr -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, cr.getGrowthScaleIdRef());
            CropDescription description = (CropDescription) getFromCollectionById(cropDescriptions, cr.getDescriptionId());
            List<CropDescription> relatedDescriptions = new ArrayList<>();
            if (map.containsKey(scale)) {
                relatedDescriptions = map.get(scale);
            }
            relatedDescriptions.add(description);
            map.put(scale, relatedDescriptions);
        });
        return map;

    }

    private Map<GrowthScale, List<GrowthScaleStage>> getGrowthScalesToStagesMap(List<GrowthScale> growthScales, List<GrowthScaleStage> growthScaleStages) {
        Map<GrowthScale, List<GrowthScaleStage>> map = new HashMap();
        growthScaleStages.forEach(stage -> {
            GrowthScale scale = (GrowthScale) getFromCollectionById(growthScales, stage.getGrowthScaleId());
            List<GrowthScaleStage> relatedStages = new ArrayList<>();
            if (map.containsKey(scale)) {
                relatedStages = map.get(scale);
            }
            relatedStages.add(stage);
            map.put(scale, relatedStages);
        });
        return map;
    }

//    private Map<Nutrient, Unit> getNutrientsToUnitsMap(List<Nutrient> nutrients, List<Unit> units) {
//        Map<Nutrient, Unit> map = new HashMap();
//        nutrients.forEach(nutrient -> {
//            Unit unit = getFromCollectionByName(units, nutrient.getElementalName());
//            map.put(nutrient, unit);
//        });
//        return map;
//    }


    private Map<Country, List<Region>> getCountryRegionMap(List<Country> countries, List<Region> regions) {
        Map<Country, List<Region>> map = new HashMap();
        regions.forEach(region -> {
            Country country = (Country) getFromCollectionById(countries, region.getCountryId());
            List<Region> countryRegions = new ArrayList<>();
            if (map.containsKey(country)) {
                countryRegions = map.get(country);
            }
            countryRegions.add(region);
            map.put(country, countryRegions);
        });
        return map;
    }

    private Map<CropGroup, List<CropClass>> getCropGroupClassMap(List<CropGroup> groups, List<CropClass> classes) {
        Map<CropGroup, List<CropClass>> map = new HashMap();
        classes.forEach(cl -> {
            CropGroup group = (CropGroup) getFromCollectionById(groups, cl.getGroupId());
            List<CropClass> groupClasses = new ArrayList<>();
            if (map.containsKey(group)) {
                groupClasses = map.get(group);
            }
            groupClasses.add(cl);
            map.put(group, groupClasses);
        });
        return map;
    }

    private Map<CropClass, List<CropSubClass>> getAncestorSubClassMap(List<CropClass> ancestors, List<CropSubClass> subClasses) {
        Map<CropClass, List<CropSubClass>> map = new HashMap();
        subClasses.forEach(scl -> {
            CropClass ancestor = (CropClass) getFromCollectionById(ancestors, scl.getClassId());
            List<CropSubClass> relatedChildren = new ArrayList<>();
            if (map.containsKey(ancestor)) {
                relatedChildren = map.get(ancestor);
            }
            relatedChildren.add(scl);
            map.put(ancestor, relatedChildren);
        });
        return map;
    }

    private Thing getFromCollectionById(List<? extends Thing> things, String id) {
        return things.stream()
                .filter(thing -> thing.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No element with id %s in collection", id)));
    }

    private boolean collectionContainsId(List<? extends Thing> things, String id) {
        return things.stream()
                .anyMatch(thing -> thing.getId().equals(id));
    }

    private boolean existNutrientWithName(List<Nutrient> nutrients, String tag) {
        return nutrients.stream()
                .anyMatch(nutrient -> nutrient.getElementalName().equals(tag));
    }

    private Unit getUnitByName(List<Unit> units, String elementalName) {
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

    private String composeCreateCropGroupCommand(CropGroup group) {
        String createGroupFormat = "CREATE (%s:%s{" +
                "CropGroupId: \"%s\", " +
                "CG_FAOId: \"%s\", " +
                "CG_MediaUri: \"%s\", " +
                "name: \"%s\", " +
                "CropGroupName: \"%s\", " +
                "ODX_CropGroup_UUId: \"%s\", " +
                "ODX_CropGroup_URI: \"%s\"})\n";
        String newGroupName = createNodeName(group.getName());
        String createGroupNode = String.format(createGroupFormat,
                newGroupName, group.getClassName(),
                group.getId(),
                group.getFaoId(),
                group.getMediaUri(),
                group.getName(),
                group.getName(),
                group.getUuId(),
                createOdxUri(group));
        return new StringBuilder().append(createGroupNode).toString();
    }

    private String createOdxUri(Thing thing) {
        return new StringBuilder()
                .append("ODX/")
                .append(thing.getClassName())
                .append("/")
                .append(thing.getUuId())
                .toString();
    }

    private CropGroup createGroupFromExcel(String groupId) {
        //Need to pass this variable from outside
        String cropGroupFileName = "loader/src/main/resources/CropGroup.xlsx";

        //May be need to pass this from outside as well
        ExcelWorkbookReader excelWorkbookReader = new ExcelWorkbookReader();

        List<CropGroup> cropGroups = excelWorkbookReader.readCropGroupFromExcel(cropGroupFileName);

        return cropGroups.stream()
                .filter(group -> group.getId().equals(groupId))
                .findAny()
                .orElse(new CropGroup("dummy_group", "dummy_group", "dummy_group", "dummy_group", "dummy_group", "dummy_group"));
    }

    //TODO need to be implemented
    public void createIncorrectCropClassRecord() {

    }

    public void createIncorrectGroupClassRelation() {
        String commandFormat = "MATCH (rice:CropClass{name: 'Rice'})" +
                "MATCH (maize:CropClass{name: 'Maize'})" +
                "CREATE (rice)-[r:HAS_CROP_CLASS]->(maize)";
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> tx.run(commandFormat));
            System.out.println("Creating incorrect relation completed");
        } catch (ClientException e) {
            System.out.println("Creation of incorrect relation is impossible");
            System.out.println(e.getMessage());
        }

    }

    //    private boolean cropClassUUIdExistsInDatabase(UUID uuid) {
//        boolean answer;
//        String existCheckQueryFormat = "MATCH (n) WHERE n.ODX_CropClass_UUId =\"%s\"" +
//                "RETURN count(n)>0";
//        String existCheckQuery = String.format(existCheckQueryFormat, uuid.toString());
//
//        try (Session session = driver.session()) {
//            answer = session.readTransaction(tx -> {
//                List<Record> records = tx.run(existCheckQuery).list();
//                return records.get(0).get(0).asBoolean();
//            });
//        }
//        return answer;
//    }

//    private boolean cropGroupUUIdExistsInDatabase(UUID uuid) {
//        boolean answer;
//        String existCheckQueryFormat = "MATCH (n) WHERE n.ODX_CropGroup_UUId =\"%s\"" +
//                "RETURN count(n)>0";
//        String existCheckQuery = String.format(existCheckQueryFormat, uuid.toString());
//
//        try (Session session = driver.session()) {
//            answer = session.readTransaction(tx -> {
//                List<Record> records = tx.run(existCheckQuery).list();
//                return records.get(0).get(0).asBoolean();
//            });
//        }
//        System.out.println(uuid + " exists in DB: " + answer);
//        return answer;
//    }

    //    public void uploadCountriesFromCsvByNeo4j(String countryCsvFileName) {
//        String commandFormat = "LOAD CSV WITH HEADERS FROM  \"file:///%s\" AS country\n" +
//                "\n" +
//                "CREATE (p:Resource :NamedIndividual :Country\n" +
//                "{\n" +
//                "CountryId: country.CountryId,\n" +
//                "CountryName: country.CountryName,\n" +
//                "ProductSetCode: country.ProductSetCode,\n" +
//                "rdfs__label: country.CountryName\n" +
//                "})";
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(String.format(commandFormat, countryCsvFileName)));
//        }
//        System.out.println("Country uploading completed");
//    }

//    public void createIncorrectCropSubClassRelation() {
//        String commandFormat = "MATCH (rice:CropSubClass{name: 'Rice'})" +
//                "MATCH (maize:CropSubClass{name: 'Maize'})" +
//                "CREATE (rice)-[r:HAS_CROP_VARIETY]->(maize)";
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(commandFormat));
//            System.out.println("Creating incorrect relation completed");
//        } catch (ClientException e) {
//            System.out.println("Creation of incorrect relation is impossible");
//            System.out.println(e.getMessage());
//        }
//    }
    //    public void uploadCropClassAsRecordWithOdxNodes(CropClass cropClass) {
//        if (uuIdExistsInDatabase(cropClass.getUuId())) {
//            return;
//        }
//        StringBuilder builder = new StringBuilder();
//        CropGroup cropGroup = createGroupFromExcel(cropClass.getGroupId());
//        String cropGroupNodeName = createNodeName(cropGroup.getName());
//        String cropClassNodeName = createNodeName(cropClass.getName());
//
////      Create CG node
//        appendCropGroupNodeCommand(builder, cropGroup, cropGroupNodeName);
////      Create ODX CG node
//        appendOdxCropGroupNodeCommand(builder, cropGroup, cropGroupNodeName);
////      Create "HAS_SOURCE_VERSION" relation ODX CG to CG node
//        appendRelationOdxGroupToGroupCommand(builder, cropGroup, cropGroupNodeName);
//
////      Create CC node
////      Create ODX CC node
////      Create "HAS_SOURCE_VERSION" relation ODX CC to CC node
//
//
////      Create "HAS_CROP_CLASS" relation ODX CG to ODX CC node
//
//
//    }
//    private Country getCountryById(List<Country> countries, String id) {
//        return countries.stream()
//                .filter(country -> country.getId().equals(id))
//                .findFirst()
//                .orElse(new Country("xxx", "xxx", "xxx", "xxx"));
//    }
//
//    private CropGroup getGroupById(List<CropGroup> groups, String id) {
//        return groups.stream()
//                .filter(group -> group.getId().equals(id))
//                .findFirst()
//                .orElse(new CropGroup("xxx", "xxx", "xxx", "xxx", "xxx", "xxx"));
//    }
//
//    private CropClass getAncestorById(List<CropClass> classes, String id) {
//        return classes.stream()
//                .filter(cl -> cl.getId().equals(id))
//                .findFirst()
//                .orElse(new CropClass("xxx", "xxx", "xxx", "xxx", "xxx", "xxx", "xxx"));
//    }
//
//    private CropSubClass getSubClassById(List<CropSubClass> subClasses, String id) {
//        return subClasses.stream()
//                .filter(scl -> scl.getId().equals(id))
//                .findFirst()
//                .orElse(new CropSubClass("xxx", "xxx", "xxx", "xxx", "xxx", "xxx", "xxx"));
//    }


//    public void uploadCropClassAsRecord(CropClass cropClass) {
//        if (existsInDatabase(cropClass)) {
//            System.out.println("CropClass exists in DB");
//            updateCropClass(cropClass);
//        } else {
//            createCropClassAsRecord(cropClass);
//        }
//    }
//
//    private void createCropClassAsRecord(CropClass cropClass) {
//        String cropGroupFileName = "loader/src/main/resources/CropGroup.xlsx";
//
//        //May be need to pass this from outside as well
//        ExcelWorkbookReader excelWorkbookReader = new ExcelWorkbookReader();
//
//        List<CropGroup> cropGroups = excelWorkbookReader.readCropGroupFromExcel(cropGroupFileName);
//
//
//        StringBuilder builder = new StringBuilder();
////        CropGroup cropGroup = createGroupFromExcel(cropClass.getGroupId());
//        CropGroup cropGroup = (CropGroup) getFromCollectionById(cropGroups, cropClass.getGroupId());
//        String cropGroupNodeName = createNodeName(cropGroup.getName());
//        String cropClassNodeName = createNodeName(cropClass.getName());
//        appendCropGroupCommand(builder, cropGroup, cropGroupNodeName);
//        appendCropClassCommand(builder, cropClass, cropClassNodeName, cropGroup);
//        appendGroupClassRelationCommand(builder, cropGroupNodeName, cropClassNodeName);
//
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(builder.toString()));
//        }
//        System.out.println("CropClass uploading completed");
//    }
//
//    private void updateCropClass(CropClass cropClass) {
//        StringBuilder builder = new StringBuilder();
//        String setClassFormat = "MATCH (cc:%s{ODX_UUid: \"%s\"})\n" +
//                "SET cc = {id: \"%s\", groupId: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\", ODX_UUid: \"%s\", ODX_URI: \"%s\"}\n";
//        String createClassCommand = String.format(setClassFormat, cropClass.getClassName(), cropClass.getUuId(),
//                cropClass.getId(), cropClass.getGroupId(), cropClass.getFaoId(), cropClass.getMediaUri(),
//                cropClass.getName(), cropClass.getUuId(), createOdxUri(cropClass));
//        builder.append(createClassCommand);
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(builder.toString()));
//        }
//        System.out.println("CropClass updating completed");
//    }
//
//    private void appendCropGroupNodeCommand(StringBuilder builder, CropGroup group, String cropGroupNodeName) {
//        String createGroupFormat = "CREATE (%s:%s{id: \"%s\", faoId: \"%s\", mediaUri: \"%s\", name: \"%s\"})\n";
//        String createGroupNodeCommand = String.format(createGroupFormat,
//                cropGroupNodeName, group.getClassName(), group.getId(), group.getFaoId(), group.getMediaUri(), group.getName());
//        builder.append(createGroupNodeCommand);
//    }
//
//    private void appendOdxCropGroupNodeCommand(StringBuilder builder, CropGroup group, String cropGroupNodeName) {
//        String createCropGroupOdxUUidNodeFormat = "CREATE (%s:%s{ODX_UUid: \"%s\", ODX_URI: \"%s\"})\n";
//        String createOdxCropGroupNodeCommand = String.format(createCropGroupOdxUUidNodeFormat,
//                "ODX_" + cropGroupNodeName, "ODX_" + group.getClassName(), group.getUuId(), createOdxUri(group));
//        builder.append(createOdxCropGroupNodeCommand);
//    }
//
//    private void appendRelationOdxGroupToGroupCommand(StringBuilder builder, CropGroup cropGroup, String cropGroupNodeName) {
//        String createRelationFormat = "CREATE (%s)-[:HAS_SOURCE_VERSION]->(%s)\n";
//        String createRelationCommand = String.format(createRelationFormat, "ODX_" + cropGroupNodeName, cropGroupNodeName);
//        builder.append(createRelationCommand);
//    }
//
//    public void uploadAnotherCropGroup() {
//        String createGroup = "CREATE (cg:CropGroup{id: \"xxx\", faoId: \"xxx\", mediaUri: \"xxx\", name: \"xxx\"})\n";
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(createGroup));
//        }
//        System.out.println("Creation of XXX CropGroup is completed");
//    }
//
//    public void createIncorrectCropSubClassRelation2() {
//        String commandFormat = "MATCH (cg1:CropGroup{name: 'Other crops'})" +
//                "MATCH (cg2:CropGroup{name: 'xxx'})" +
//                "CREATE (cg1)-[r:HAS_CROP_CLASS]->(cg2)";
//        try (Session session = driver.session()) {
//            session.writeTransaction(tx -> tx.run(commandFormat));
//            System.out.println("Creating incorrect CropGroup relation completed");
//        } catch (ClientException e) {
//            System.out.println("Creation of incorrect CropGroup relation is impossible");
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private void appendCropGroupCommand(StringBuilder builder, CropGroup group, String cropGroupNodeName) {
//        if (!existsInDatabase(group)) {
//            String createCropGroupCommand = composeCreateCropGroupCommand(group);
//            builder.append(createCropGroupCommand);
//        } else {
//            String groupMatchFormat = "MATCH (%s:%s{ODX_CropGroup_UUId: \"%s\"})\n";
//            String matchGroupById = String.format(groupMatchFormat, cropGroupNodeName, group.getClassName(), group.getUuId());
//            builder.append(matchGroupById);
//        }
//    }
//
}

