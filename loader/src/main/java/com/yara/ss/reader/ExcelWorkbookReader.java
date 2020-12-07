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
        } else if (cellType == CellType.BOOLEAN) {
            return Boolean.toString(row.getCell(cellIndex).getBooleanCellValue());
        } else {
            System.out.println("WTF?");
            return "WTF?";
        }
    }

    public List<Nutrient> readNutrientsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Nutrient";

        List<Nutrient> nutrients = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in Nutrient file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                String elementalName = row.getCell(2).getStringCellValue();
                String nutrientOrdinal = row.getCell(3).getStringCellValue();
                Nutrient nutrient = new Nutrient(POLARIS_SOURCE, className, id, name,
                        elementalName, nutrientOrdinal);
                nutrients.add(nutrient);
            }
        }
        return nutrients;
    }

    public List<Unit> readUnitsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Unit";

        List<Unit> units = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in Unit file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                String tag = row.getCell(2).getStringCellValue();
                Unit unit = new Unit(POLARIS_SOURCE, className, id, name, tag);
                units.add(unit);
            }
        }
        return units;
    }

    public List<UnitConversion> readUnitConversionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "UnitConversion";

        List<UnitConversion> conversions = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in UnitConversion file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String unitIdRef = row.getCell(1).getStringCellValue();
                String convertToUnitId = row.getCell(2).getStringCellValue();
                String multiplier = getCellDataAsString(row, 3);
                String countryIdRef = row.getCell(4).getStringCellValue();
                String name = row.getCell(5).getStringCellValue();
                UnitConversion conversion = new UnitConversion(POLARIS_SOURCE, className, id, name, unitIdRef, convertToUnitId, multiplier, countryIdRef);
                conversions.add(conversion);
            }
        }
        return conversions;
    }

    public List<Fertilizer> readFertilizersFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Fertilizer";

        List<Fertilizer> fertilizers = new ArrayList<>();
        XSSFWorkbook myExcelBook = null;
        try {
            myExcelBook = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
        int rows = myExcelSheet.getPhysicalNumberOfRows();
        System.out.println("Rows in Fertilizer file: " + rows);
        for (int i = 1; i < rows; i++) {
            XSSFRow row = myExcelSheet.getRow(i);

            if (row != null
                    && row.getCell(0) != null
                    && row.getCell(0).getCellType() == CellType.STRING
                    && !row.getCell(0).getStringCellValue().isEmpty()) {
                String id = row.getCell(0).getStringCellValue();
                String family = row.getCell(1).getStringCellValue();
                String type = row.getCell(2).getStringCellValue();
                String name = getCellDataAsString(row, 3);
                String lowChloride = getCellDataAsString(row, 4);
                String dryMatter = "";
                String spreaderLoss = "";
                String density = getCellDataAsString(row, 7);
                String n = getCellDataAsString(row, 8);
                String nUnitId = getCellDataAsString(row, 10);
                String p = getCellDataAsString(row, 11);
                String pUnitId = getCellDataAsString(row, 13);
                String k = getCellDataAsString(row, 14);
                String kUnitId = getCellDataAsString(row, 16);
                String mg = getCellDataAsString(row, 17);
                String mgUnitId = getCellDataAsString(row, 19);
                String s = getCellDataAsString(row, 20);
                String sUnitId = getCellDataAsString(row, 22);
                String ca = getCellDataAsString(row, 23);
                String caUnitId = getCellDataAsString(row, 25);
                String b = getCellDataAsString(row, 26);
                String bUnitId = getCellDataAsString(row, 28);
                String zn = getCellDataAsString(row, 29);
                String znUnitId = getCellDataAsString(row, 31);
                String mn = getCellDataAsString(row, 32);
                String mnUnitId = getCellDataAsString(row, 34);
                String cu = getCellDataAsString(row, 35);
                String cuUnitId = getCellDataAsString(row, 37);
                String fe = getCellDataAsString(row, 38);
                String feUnitId = getCellDataAsString(row, 40);
                String mo = getCellDataAsString(row, 41);
                String moUnitId = getCellDataAsString(row, 43);
                String na = getCellDataAsString(row, 44);
                String naUnitId = getCellDataAsString(row, 46);
                String se = getCellDataAsString(row, 47);
                String seUnitId = getCellDataAsString(row, 49);
                String co = getCellDataAsString(row, 50);
                String coUnitId = getCellDataAsString(row, 52);
                String no3 = getCellDataAsString(row, 53);
                String nh4 = getCellDataAsString(row, 54);
                String urea = getCellDataAsString(row, 55);
                Fertilizer fertilizer = new Fertilizer.Builder(
                        POLARIS_SOURCE,
                        className,
                        id,
                        name,
                        family,
                        type,
                        lowChloride,
                        dryMatter,
                        spreaderLoss,
                        density)
                        .n(n)
                        .nUnitId(nUnitId)
                        .p(p)
                        .pUnitId(pUnitId)
                        .k(k)
                        .kUnitId(kUnitId)
                        .mg(mg)
                        .mgUnitId(mgUnitId)
                        .s(s)
                        .sUnitId(sUnitId)
                        .ca(ca)
                        .caUnitId(caUnitId)
                        .b(b)
                        .bUnitId(bUnitId)
                        .zn(zn)
                        .znUnitId(znUnitId)
                        .mn(mn)
                        .mnUnitId(mnUnitId)
                        .cu(cu)
                        .cuUnitId(cuUnitId)
                        .fe(fe)
                        .feUnitId(feUnitId)
                        .mo(mo)
                        .moUnitId(moUnitId)
                        .na(na)
                        .naUnitId(naUnitId)
                        .se(se)
                        .seUnitId(seUnitId)
                        .co(co)
                        .coUnitId(coUnitId)
                        .no3(no3)
                        .nh4(nh4)
                        .urea(urea)
                        .build();
                fertilizers.add(fertilizer);
            }
        }
        return fertilizers;
    }
}