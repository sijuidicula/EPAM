package com.yara.ss.reader;

import com.yara.ss.domain.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelWorkbookReader {

    //Next field should be received from incoming file, not hardcoded
    private static final String POLARIS_SOURCE = "Polaris";

    public List<CropGroup> readCropGroupFromExcel(String fileName) {
        //Next two field should be received from incoming file, not hardcoded
        String className = "CropGroup";

        List<CropGroup> cropGroups = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropGroup file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String faoId = row.getCell(1).getRawValue();
                String mediaUri = "";
//                String mediaUri = row.getCell(2).getStringCellValue();
                String name = row.getCell(3).getStringCellValue();
                CropGroup cropGroup = new CropGroup(POLARIS_SOURCE, className, id, faoId, mediaUri, name);
                cropGroups.add(cropGroup);
            }
        }
        return cropGroups;
    }

    public List<CropClass> readCropClassFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropClass";

        List<CropClass> cropClasses = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet("CropClass");
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropClass file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String groupId = row.getCell(1).getStringCellValue();
                String faoId = row.getCell(2).getRawValue();
                String mediaUri = "";
//                String mediaUri = row.getCell(3).getStringCellValue();
                String name = row.getCell(4).getStringCellValue();
                CropClass cropClass = new CropClass(POLARIS_SOURCE, className, id, groupId, faoId, mediaUri, name);
                cropClasses.add(cropClass);
            }
        }
        return cropClasses;
    }


    public List<CropSubClass> readCropSubClassFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropSubClass";

        List<CropSubClass> subClasses = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropSubClass file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
//            ) {
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
//                System.out.println(row != null);
//                System.out.println(row.getCell(0) != null);
//                System.out.println(row.getCell(0).getCellType() == CellType.STRING);
//                System.out.println(!row.getCell(0).getStringCellValue().isEmpty());
//                System.out.println(row.getCell(0).getStringCellValue());
                String id = row.getCell(0).getStringCellValue();
                String classId = row.getCell(1).getStringCellValue();
                String faoId = "";
                if (row.getCell(2) != null) {
                    faoId = String.valueOf(row.getCell(2).getNumericCellValue());
                }
                String mediaUri = "";
//                String mediaUri = row.getCell(3).getStringCellValue();
                String name = row.getCell(4).getStringCellValue();
                CropSubClass cropClass = new CropSubClass(POLARIS_SOURCE, className, id, classId, faoId, mediaUri, name);
                subClasses.add(cropClass);
            }
        }
        return subClasses;
    }

    public List<Country> readCountryFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Country";

        List<Country> countries = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in Country file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);
            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                Country country = new Country(POLARIS_SOURCE, className, id, name);
                countries.add(country);
            }
        }
        return countries;
    }

    public List<Region> readRegionFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Region";

        List<Region> regions = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in Region file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);
            if (row != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String countryId = row.getCell(1).getStringCellValue();
                String name = row.getCell(2).getStringCellValue();
                Region region = new Region(POLARIS_SOURCE, className, id, countryId, name);
                regions.add(region);
            }
        }
        return regions;
    }

    public List<CropVariety> readCropVarietyFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropVariety";

        List<CropVariety> varieties = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet("CropVariety");
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropVariety file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String subClassId = row.getCell(1).getStringCellValue();
                String name = row.getCell(2).getStringCellValue();
                CropVariety variety = new CropVariety(POLARIS_SOURCE, className, id, subClassId, name);
                varieties.add(variety);
            }
        }
        return varieties;
    }

    public List<CropDescription> readCropDescriptionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropDescription";

        List<CropDescription> descriptions = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropDescription file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String subClassId = row.getCell(1).getStringCellValue();
                boolean chlorideSensitiveBool = row.getCell(2).getBooleanCellValue();
                String chlorideSensitive = Boolean.toString(chlorideSensitiveBool);
                String mediaUri = "";
//                String mediaUri = row.getCell(3).getStringCellValue();
                String name = row.getCell(4).getStringCellValue();
                CropDescription description = new CropDescription(POLARIS_SOURCE, className, id, subClassId, chlorideSensitive, mediaUri, name);
                descriptions.add(description);
            }
        }
        return descriptions;
    }

    public List<CropDescriptionVariety> readCropDescriptionVarietyFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropDescriptionVariety";

        List<CropDescriptionVariety> descVars = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropDescriptionVariety file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String varId = row.getCell(1).getStringCellValue();
                String descId = row.getCell(2).getStringCellValue();
                CropDescriptionVariety descVar = new CropDescriptionVariety(id, varId, descId);
                descVars.add(descVar);
            }
        }
        return descVars;

    }

    public List<GrowthScale> readGrowthScaleFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "GrowthScale";

        List<GrowthScale> growthScales = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in GrowthScale file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                GrowthScale growthScale = new GrowthScale(POLARIS_SOURCE, className, id, name);
                growthScales.add(growthScale);
            }
        }
        return growthScales;
    }

    public List<GrowthScaleStage> readGrowthScaleStageFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "GrowthScaleStage";

        List<GrowthScaleStage> growthScaleStages = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in GrowthScaleStage file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String growthScaleId = row.getCell(1).getStringCellValue();
                String growthScaleStageDescription = row.getCell(2).getStringCellValue();
                String ordinal = row.getCell(3).getStringCellValue();
                String baseOrdinal = "";
//                float baseOrdinal = (float) row.getCell(4).getNumericCellValue();
                GrowthScaleStage scaleStage = new GrowthScaleStage(POLARIS_SOURCE, className, id, "GrowthScaleStage",
                        growthScaleId, growthScaleStageDescription, ordinal, baseOrdinal);
                growthScaleStages.add(scaleStage);
            }
        }
        return growthScaleStages;
    }

    public List<CropRegion> readCropRegionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropRegion";

        List<CropRegion> cropRegions = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in CropRegion file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String descriptionId = row.getCell(1).getStringCellValue();
                String countryIdRef = row.getCell(2).getStringCellValue();
                String growthScaleIdRef = row.getCell(3).getStringCellValue();
                String defaultSeedingDate = getCellDataAsString(row, 4);
                String defaultHarvestDate = getCellDataAsString(row, 5);
                String defaultYield = getCellDataAsString(row, 6);
                String yieldBaseUnitId = row.getCell(7).getStringCellValue();
                String demandBaseUnitId = row.getCell(8).getStringCellValue();
                String regionIdRef = row.getCell(9).getStringCellValue();
                String additionalProperties = row.getCell(10).getStringCellValue();
                CropRegion cropRegion = new CropRegion(
                        id,
                        descriptionId,
                        countryIdRef,
                        regionIdRef,
                        growthScaleIdRef,
                        defaultSeedingDate,
                        defaultHarvestDate,
                        defaultYield,
                        yieldBaseUnitId,
                        demandBaseUnitId,
                        additionalProperties);
                cropRegions.add(cropRegion);
            }
        }
        return cropRegions;
    }

    private String getCellDataAsString(XSSFRow row, int cellIndex) {
        CellType cellType = row.getCell(cellIndex).getCellType();
        if (cellType == CellType.NUMERIC) {
            return Double.toString(row.getCell(cellIndex).getNumericCellValue());
        } else if (cellType == CellType.STRING) {
            return row.getCell(cellIndex).getStringCellValue();
        } else {
            System.out.println("WTF?");
            return "WTF?";
        }
    }
}