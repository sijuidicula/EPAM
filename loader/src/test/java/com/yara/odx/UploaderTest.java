package com.yara.odx;

import com.yara.odx.domain.Country;
import com.yara.odx.domain.Region;
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

    @SuppressWarnings("unchecked")
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
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record  -> {
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

    @SuppressWarnings("unchecked")
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
            assertThat(result.stream())
                    .hasSize(2)
                    .extracting(record  -> {
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
}
