package com.yara.odx.reader;

import com.yara.odx.domain.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExcelWorkbookReader {

    //Next field should be received from incoming file, not hardcoded
    private static final String POLARIS_SOURCE = "Polaris";

    public List<CropGroup> readCropGroupFromExcel(String fileName) {
        //Next two field should be received from incoming file, not hardcoded
        String className = "CropGroup";

        List<CropGroup> cropGroups = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String name = row.getCell(3).getStringCellValue();
                    CropGroup cropGroup = new CropGroup(POLARIS_SOURCE, className, id, faoId, name);
                    cropGroups.add(cropGroup);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cropGroups;
    }

    public List<CropClass> readCropClassFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropClass";

        List<CropClass> cropClasses = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String name = row.getCell(4).getStringCellValue();
                    CropClass cropClass = new CropClass(POLARIS_SOURCE, className, id, groupId, faoId, name);
                    cropClasses.add(cropClass);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cropClasses;
    }

    public List<CropSubClass> readCropSubClassFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropSubClass";

        List<CropSubClass> subClasses = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
            XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
            int rows = myExcelSheet.getPhysicalNumberOfRows();
            System.out.println("Rows in CropSubClass file: " + rows);
            for (int i = 1; i < rows; i++) {
                XSSFRow row = myExcelSheet.getRow(i);

                if (row != null
                        && row.getCell(0) != null
                        && row.getCell(0).getCellType() == CellType.STRING
                        && !row.getCell(0).getStringCellValue().isEmpty()) {
                    String id = row.getCell(0).getStringCellValue();
                    String classId = row.getCell(1).getStringCellValue();
                    String faoId = getCellDataAsString(row, 2);
                    String name = row.getCell(4).getStringCellValue();
                    CropSubClass subClass = new CropSubClass(POLARIS_SOURCE, className, id, classId, faoId, name);
                    subClasses.add(subClass);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subClasses;
    }

    public List<Country> readCountryFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Country";

        List<Country> countries = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String productSetCode = getCellDataAsString(row, 2);
                    Country country = new Country(POLARIS_SOURCE, className, id, name, "dummy_fips", "dummy_iso2Code",
                            "dummy_iso3Code", "dummy_m49Code", "dummy_ODX_ContinentalSection_UUId_Ref",
                            productSetCode, "dummy_un");
                    countries.add(country);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countries;
    }

    public List<Region> readRegionFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Region";

        List<Region> regions = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return regions;
    }

    public List<CropVariety> readCropVarietyFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropVariety";

        List<CropVariety> varieties = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return varieties;
    }

    public List<CropDescription> readCropDescriptionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropDescription";

        List<CropDescription> descriptions = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String chlorideSensitive = getCellDataAsString(row, 2);
                    String mediaUri = getCellDataAsString(row, 3);
                    String name = row.getCell(4).getStringCellValue();
                    CropDescription description = new CropDescription(POLARIS_SOURCE, className, id, subClassId, chlorideSensitive, name);
                    descriptions.add(description);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return descriptions;
    }

    public List<CropDescriptionVariety> readCropDescriptionVarietyFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropDescriptionVariety";

        List<CropDescriptionVariety> descVars = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return descVars;

    }

    public List<GrowthScale> readGrowthScaleFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "GrowthScale";

        List<GrowthScale> growthScales = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return growthScales;
    }

    public List<GrowthScaleStages> readGrowthScaleStageFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "GrowthScaleStages";

        List<GrowthScaleStages> growthScaleStages = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String baseOrdinal = getCellDataAsString(row, 4);
                    GrowthScaleStages scaleStage = new GrowthScaleStages(POLARIS_SOURCE, className, id, className,
                            growthScaleId, growthScaleStageDescription, ordinal, baseOrdinal);
                    growthScaleStages.add(scaleStage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return growthScaleStages;
    }

    public List<CropRegion> readCropRegionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "CropRegion";

        List<CropRegion> cropRegions = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String descriptionId = getCellDataAsString(row, 1);
                    String countryIdRef = getCellDataAsString(row, 2);
                    String growthScaleIdRef = getCellDataAsString(row, 3);
                    String defaultSeedingDate = getCellDataAsString(row, 4);
                    String defaultHarvestDate = getCellDataAsString(row, 5);
                    String defaultYield = getCellDataAsString(row, 6);
                    String yieldBaseUnitId = getCellDataAsString(row, 7);
                    String demandBaseUnitId = getCellDataAsString(row, 8);
                    String regionIdRef = getCellDataAsString(row, 9);
                    String additionalProperties = getCellDataAsString(row, 10);
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

                    if (containsSame(cropRegions, cropRegion)) {
                        saveFull(cropRegions, cropRegion);
                    } else {
                        cropRegions.add(cropRegion);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cropRegions;
    }

    private void saveFull(List<? extends Duplicate> list, Duplicate newObject) {
        Optional<? extends Duplicate> optionalObject = list.stream()
                .filter(cr -> cr.sameAs(newObject))
                .findFirst();

        if (optionalObject.isPresent()) {
            Duplicate oldObject = optionalObject.get();
            switch (newObject.getClass().getSimpleName()) {
                case ("CropRegion"):
                    //noinspection unchecked
                    saveFullCropRegion((List<CropRegion>) list, (CropRegion) oldObject, (CropRegion) newObject);
                    break;
                case ("FertilizerRegion"):
                    //noinspection unchecked
                    saveFullFertilizerRegion((List<FertilizerRegion>) list, (FertilizerRegion) oldObject, (FertilizerRegion) newObject);
                    break;
                default:
                    System.out.println(newObject.getClass().getSimpleName());
                    throw new IllegalStateException("Unexpected value: " + newObject.getClass());
            }
        }
    }

    private void saveFullFertilizerRegion(List<FertilizerRegion> list, FertilizerRegion oldObject, FertilizerRegion newObject) {
        if (oldObject.getLocalizedName().equals("NULL") &&
                !newObject.getLocalizedName().equals("NULL")) {
            list.remove(oldObject);
            list.add(newObject);
        }
    }

    private void saveFullCropRegion(List<CropRegion> list, CropRegion oldObject, CropRegion newObject) {
        if (oldObject.getDefaultSeedingDate().equals("NULL") &&
                !newObject.getDefaultSeedingDate().equals("NULL")) {
            list.remove(oldObject);
            list.add(newObject);
        }
    }

    private boolean containsSame(List<? extends Duplicate> list, Duplicate object) {
        return list.stream()
                .anyMatch(cr -> cr.sameAs(object));
    }

    private String getCellDataAsString(XSSFRow row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return "";
        }

        CellType cellType = row.getCell(cellIndex).getCellType();
        if (cellType == CellType.BLANK) {
            return "";
        } else if (cellType == CellType.NUMERIC) {
            return Double.toString(row.getCell(cellIndex).getNumericCellValue());
        } else if (cellType == CellType.STRING) {
            return row.getCell(cellIndex).getStringCellValue();
        } else if (cellType == CellType.BOOLEAN) {
            return Boolean.toString(row.getCell(cellIndex).getBooleanCellValue());
        } else {
            System.out.println(String.format("In row # %d, cell # %d occurred unexpected problem", row.getRowNum(), cellIndex));
            System.out.println(cellType);
            return "?";
        }
    }

    public List<Nutrient> readNutrientsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Nutrient";

        List<Nutrient> nutrients = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nutrients;
    }

    public List<Units> readUnitsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Units";

        List<Units> units = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    Units unit = new Units(POLARIS_SOURCE, className, id, name, tag);
                    units.add(unit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return units;
    }

    public List<UnitConversion> readUnitConversionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "UnitConversion";

        List<UnitConversion> conversions = new ArrayList<>();
        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    UnitConversion conversion = new UnitConversion(POLARIS_SOURCE, className, id, unitIdRef, convertToUnitId, multiplier, countryIdRef);
                    conversions.add(conversion);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return conversions;
    }

    public List<Fertilizers> readFertilizersFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Fertilizers";

        List<Fertilizers> fertilizers = new ArrayList<>();

        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
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
                    String utilizationN = getCellDataAsString(row, 56);
                    String utilizationNh4 = getCellDataAsString(row, 57);
                    String tank = getCellDataAsString(row, 58);
                    String electricalConductivity = getCellDataAsString(row, 59);
                    String pH = getCellDataAsString(row, 60);
                    String solubility5C = getCellDataAsString(row, 61);
                    String solubility20C = getCellDataAsString(row, 62);
                    String dhCode = getCellDataAsString(row, 63);
                    String syncId = getCellDataAsString(row, 64);
                    String syncSource = getCellDataAsString(row, 65);
                    String lastSync = getCellDataAsString(row, 66);
                    Fertilizers fertilizer = new Fertilizers.Builder(
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
                            .setNutrientUnitsContent(nUnitId, n)
                            .setNutrientUnitsContent(pUnitId, p)
                            .setNutrientUnitsContent(kUnitId, k)
                            .setNutrientUnitsContent(mgUnitId, mg)
                            .setNutrientUnitsContent(sUnitId, s)
                            .setNutrientUnitsContent(caUnitId, ca)
                            .setNutrientUnitsContent(bUnitId, b)
                            .setNutrientUnitsContent(znUnitId, zn)
                            .setNutrientUnitsContent(mnUnitId, mn)
                            .setNutrientUnitsContent(cuUnitId, cu)
                            .setNutrientUnitsContent(feUnitId, fe)
                            .setNutrientUnitsContent(moUnitId, mo)
                            .setNutrientUnitsContent(naUnitId, na)
                            .setNutrientUnitsContent(seUnitId, se)
                            .setNutrientUnitsContent(coUnitId, co)
//                      should add these ids as columns to Fertilizer sheet
                            .setNutrientUnitsContent("70ae19e2-be4f-4745-b67f-8eeb8a9f12e9", no3)
                            .setNutrientUnitsContent("c6deac28-14f4-4eb3-946d-4ef93e4e9c33", nh4)
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
                            .utilizationN(utilizationN)
                            .utilizationNh4(utilizationNh4)
                            .tank(tank)
                            .electricalConductivity(electricalConductivity)
                            .pH(pH)
                            .solubility5C(solubility5C)
                            .solubility20C(solubility20C)
                            .dhCode(dhCode)
                            .syncId(syncId)
                            .syncSource(syncSource)
                            .lastSync(lastSync)
                            .build();

                    fertilizers.add(fertilizer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fertilizers;
    }

    public List<FertilizerRegion> readFertilizerRegionsFromExcel(String fileName) {
        //Next field should be received from incoming file, not hardcoded
        String className = "Fertilizers_Reg";

        List<FertilizerRegion> fertilizerRegions = new ArrayList<>();

        try (XSSFWorkbook myExcelBook = new XSSFWorkbook(new FileInputStream(fileName))) {
            XSSFSheet myExcelSheet = myExcelBook.getSheet(className);
            int rows = myExcelSheet.getPhysicalNumberOfRows();
            System.out.println("Rows in FertilizerRegion file: " + rows);
            for (int i = 1; i < rows; i++) {
                XSSFRow row = myExcelSheet.getRow(i);

                if (row != null
                        && row.getCell(0) != null
                        && row.getCell(0).getCellType() == CellType.STRING
                        && !row.getCell(0).getStringCellValue().isEmpty()) {
                    String id = row.getCell(0).getStringCellValue();
                    String countryId = row.getCell(1).getStringCellValue();
                    String regionId = row.getCell(2).getStringCellValue();
                    String localizedName = getCellDataAsString(row, 3);
                    String productId = row.getCell(4).getStringCellValue();
                    String isAvailable = getCellDataAsString(row, 5);
                    String appTags = getCellDataAsString(row, 6);
                    FertilizerRegion region = new FertilizerRegion(id, countryId, regionId, localizedName, productId, isAvailable, appTags);

                    if (containsSame(fertilizerRegions, region)) {
                        saveFull(fertilizerRegions, region);
                    } else {
                        fertilizerRegions.add(region);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fertilizerRegions;
    }
}