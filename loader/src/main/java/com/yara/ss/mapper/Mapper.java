package com.yara.ss.mapper;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;

public interface Mapper {
    Object map(String source, XSSFRow row);

    default String getCellDataAsString(XSSFRow row, int cellIndex) {
        if (row.getCell(cellIndex) == null) {
            return "";
        }

        CellType cellType = row.getCell(cellIndex).getCellType();
        if (cellType == CellType.NUMERIC) {
            return Double.toString(row.getCell(cellIndex).getNumericCellValue());
        } else if (cellType == CellType.STRING) {
            return row.getCell(cellIndex).getStringCellValue();
        } else if (cellType == CellType.BOOLEAN) {
            return Boolean.toString(row.getCell(cellIndex).getBooleanCellValue());
        } else {
            System.out.println(String.format("In row # %d, cell # %d occurred unexpected problem", row.getRowNum(), cellIndex));
            return "?";
        }
    }
}
