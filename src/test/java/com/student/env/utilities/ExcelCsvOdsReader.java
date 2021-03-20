package com.student.env.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;

public class ExcelCsvOdsReader {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static int getRowId(XSSFSheet sheet, String colName, String textToFind) {
		try {
			int colIndex = 0;
			for (int colNum = 0; colNum <= sheet.getRow(0).getLastCellNum(); colNum++) {
				if (sheet.getRow(0).getCell(colNum).toString().equalsIgnoreCase(colName)) {
					colIndex = colNum;
					break;
				}
			}
			for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
				if (sheet.getRow(rowNum).getCell(colIndex).toString().equalsIgnoreCase(textToFind)) {
					return rowNum;
				}
			}
			LOGGER.info("no row found containing the search text ...");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 1;
	}

	public static int getRowId(XSSFSheet sheet, int colIndex, String textToFind) {

		try {
			for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
				if (sheet.getRow(rowNum).getCell(colIndex).toString().equalsIgnoreCase(textToFind)) {
					return rowNum;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	// get row num of any key in any speadsheet . starting is 1-based index and
	// not 0.
	public static int getRowNumBasedOnKey(XSSFSheet excelSheet, String keyToLookUp) {

		Iterator<Row> rowIterator = excelSheet.rowIterator();
		int rowNum = 1; // exclusing headers and index is 1 and not 0 ....

		while (rowIterator.hasNext()) {
			Row nextRow = rowIterator.next();
			if (nextRow.getRowNum() == 0) {
				continue;
			}
			Iterator<Cell> cellIterator = nextRow.cellIterator();
			while (cellIterator.hasNext()) {
				Cell excelCell = cellIterator.next();
				// only check String typ and should match with your intended
				// KEY.
				if (excelCell.getCellType() == XSSFCell.CELL_TYPE_STRING
						&& excelCell.getStringCellValue().equalsIgnoreCase(keyToLookUp)) {
					return rowNum;
				}
			}
			rowNum++;
		}

		return -1;
	}

	public static void writeDataToODS(String businessArea, String filename, String sheetName, String rangeCellLoc,
			Object value) throws IOException {

		File file = Paths.get("src/test/resources/com/studentTestData/" + businessArea + "/" + filename)
				.toAbsolutePath().toFile();
		org.jopendocument.dom.spreadsheet.Sheet sheet = SpreadSheet.createFromFile(file).getSheet(sheetName);

		if (value instanceof BigDecimal) {
			sheet.getCellAt(rangeCellLoc).setValue((BigDecimal) value);
		} else {
			sheet.getCellAt(rangeCellLoc).setValue(null);
		}

		sheet.getSpreadSheet().saveAs(file);

	}

	// write data to ods

	public static void writeDataToODS(String businessArea, String filename, String sheetName, String rangeCellLoc,
			String value) throws IOException {

		File file = Paths.get("src/test/resources/com/studentTestData/" + businessArea + "/" + filename)
				.toAbsolutePath().toFile();
		org.jopendocument.dom.spreadsheet.Sheet sheet = SpreadSheet.createFromFile(file).getSheet(sheetName);
		sheet.getCellAt(rangeCellLoc).setValue(value);
		sheet.getSpreadSheet().saveAs(file);
	}

	public static void writeDataToODS(String businessArea, String filename, String sheetName, String value, int column,
			int row) throws IOException {

		File file = Paths.get("src/test/resources/com/studentTestData/" + businessArea + "/" + filename)
				.toAbsolutePath().toFile();
		org.jopendocument.dom.spreadsheet.Sheet sheet = SpreadSheet.createFromFile(file).getSheet(sheetName);
		sheet.setValueAt(value, column, row);
		sheet.getSpreadSheet().saveAs(file);
	}

	public static void updateSpreadSheetOTR(String module, String excelName, String sheetName, int rowNum, int cellNum,
			String updateValue, String type) throws IOException {

		// read the spreadsheet that neds to be updated .

		FileInputStream filePath = new FileInputStream(
				"src/test/resources/com/studentTestData/studentFeeReceipt/" + module + "/" + excelName + ".xlsx");
		XSSFWorkbook wb = new XSSFWorkbook(filePath);

		// Access the wb
		XSSFSheet sheet = wb.getSheet(sheetName);
		Cell updateCell = sheet.getRow(rowNum).getCell(cellNum);
		if (type.equals("Number")) {
			updateCell.setCellValue(new Integer(updateValue));
		} else if (type.equals("String")) {
			updateCell.setCellValue(updateValue);
		}

		filePath.close();

		// open fileOutStream to write updates
		FileOutputStream output_file = new FileOutputStream(
				"src/test/resources/com/studentTestData/studentFeeReceipt/" + module + excelName + ".xlsx");

		// write changes
		wb.write(output_file);
		output_file.close();

	}

	public static XSSFRow getRow(XSSFSheet sheet, String colName, String textToFind) {

		int colIndex = 0;
		for (int colNum = 0; colNum <= sheet.getRow(0).getLastCellNum(); colNum++) {
			if (sheet.getRow(0).getCell(colNum).toString().equalsIgnoreCase(colName)) {
				colIndex = colNum;
				break;
			}
		}

		for (int RowNum = 0; RowNum <= sheet.getLastRowNum(); RowNum++) {
			if (textToFind.equalsIgnoreCase(sheet.getRow(RowNum).getCell(colIndex).toString().trim())) {
				return sheet.getRow(RowNum);
			}
		}
		LOGGER.info("no row found with text having -->  " + textToFind);
		return null;
	}

	public static org.jopendocument.dom.spreadsheet.Sheet getTestDataFromODSFile(String businessArea, String fileName,
			String sheetName) throws IOException {
		File file = Paths.get("src/test/resources/com//studenTestData/" + businessArea + "/" + fileName)
				.toAbsolutePath().toFile();
		org.jopendocument.dom.spreadsheet.Sheet sheet = SpreadSheet.createFromFile(file).getSheet(sheetName);
		return sheet;

	}

}
