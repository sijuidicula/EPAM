package com.yara.odx.mapper;

import com.yara.odx.domain.Country;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class CountryMapper implements Mapper {

    private static final String COUNTRY_CLASS_NAME = "Country";

    @Override
    public Object map(String source, XSSFRow row) {
        String id = row.getCell(0).getStringCellValue();
        String name = row.getCell(1).getStringCellValue();
        String productSetCode = getCellDataAsString(row, 2);
        return new Country(source, COUNTRY_CLASS_NAME, id, name, "dummy_fips", "dummy_iso2Code",
                "dummy_iso3Code", "dummy_m49Code", "dummy_ODX_ContinentalSection_UUId_Ref", productSetCode, "dummy_un");
    }
}
