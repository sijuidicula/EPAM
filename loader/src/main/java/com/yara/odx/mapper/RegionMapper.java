package com.yara.odx.mapper;

import com.yara.odx.domain.Region;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class RegionMapper implements Mapper{

    private static final String REGION_CLASS_NAME = "Region";

    @Override
    public Object map(String source, XSSFRow row) {
        String id = row.getCell(0).getStringCellValue();
        String countryId = row.getCell(1).getStringCellValue();
        String name = row.getCell(2).getStringCellValue();
        return new Region(source, REGION_CLASS_NAME, id, countryId, name);
    }
}
