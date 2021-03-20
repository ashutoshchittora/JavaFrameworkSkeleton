package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.log4j.LogManager;
import java.io.File;
import java.io.FileInputStream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mysql.jdbc.ResultSetMetaData;

public class ExcelUtil {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	private XSSFWorkbook wb;
	private String filePath;
	Map<String, BiMap<String, Integer>> sheetHeaderMap;

	public void writeSheetQuery(String sheetName, String query, Map<String, String> parameter) throws Exception {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		ResultSet rs = DatabaseConnection.returnScrollableResultSet(query);
		ResultSetMetaData rsmd = (ResultSetMetaData) rs.getMetaData();
		int colNums = rsmd.getColumnCount();
		while (rs != null && rs.next()) {
			dataMap.clear();
			dataMap.putAll(parameter);
			for (int i = 1; i <= colNums; i++) {
				String colName = rsmd.getColumnName(i);
				dataMap.put(colName, rs.getObject(colNums));
			}
			writeToExcel(sheetName, query, dataMap);
		}

	}

	@SuppressWarnings("unchecked")
	public void writeToExcel(String sheetName, String linkKey, Map<String, Object> dataMap) {
		XSSFSheet sheet = getSheet(sheetName, dataMap.keySet());
		Map<String, Integer> headMap = sheetHeaderMap.get(sheetName);
		int rowNum = sheet.getLastRowNum() + 1;
		XSSFRow row = sheet.createRow(rowNum);
		XSSFCell cell = row.createCell(0);
		cell.setCellValue(linkKey);
		for (String key : dataMap.keySet()) {
			cell = row.createCell(headMap.get(key));
			Object obj = dataMap.get(key);
			if (obj == null) {
				continue;
			}
			if (obj instanceof List) {
				String parentKey = linkKey + "." + key + "(list)" + String.format("%03d", rowNum);
				List<Object> list = (List<Object>) obj;
				if (!list.isEmpty() && !(list.get(0) instanceof Map)) {
					parentKey = linkKey + "." + key + "(list)" + String.format("%03d", rowNum);
				}
				writeToExcel(key, parentKey, (Map<String, Object>) obj);
				cell.setCellValue(parentKey);
			} else if (obj instanceof Map) {
				String parentKey = linkKey + "." + key + "(map)" + String.format("%03d", rowNum);
				writeToExcel(key, parentKey, (Map<String, Object>) obj);
				cell.setCellValue(parentKey);
			} else {
				cell.setCellValue(obj.toString());
			}
		}

	}

	public XSSFSheet getSheet(String sheetName, Set<String> heads) {
		XSSFSheet sheet = null;
		if (sheetHeaderMap.containsKey(sheetName)) {
			sheet = wb.getSheet(sheetName);
			XSSFRow head = sheet.getRow(0);
			Map<String, Integer> map = sheetHeaderMap.get(sheetName);
			Iterator<String> it = heads.iterator();
			while (it.hasNext()) {
				String headerName = it.next();
				if (!map.containsKey(headerName)) {
					int count = head.getLastCellNum();
					XSSFCell cell = head.createCell(count);
					cell.setCellValue(headerName);
					map.put(headerName, count);
				}
			}
		} else {
			sheet = wb.createSheet(sheetName);
			XSSFRow head = sheet.createRow(0);
			XSSFCell cell = head.createCell(0);
			cell.setCellValue("LinkKey");
			int count = 0;
			BiMap<String, Integer> map = HashBiMap.create();
			Iterator<String> it = heads.iterator();

			while (it.hasNext()) {
				String headerName = it.next();
				cell = head.createCell(++count);
				cell.setCellValue(headerName);
				map.put(headerName, count);

			}
			sheetHeaderMap.put(sheetName, map);

		}

		return sheet;
	}

	public XSSFRow getLastRowForColumn(XSSFSheet sheet , int column){
		XSSFRow row = null;
		int rowNum = sheet.getLastRowNum();
		for(int i= 1;i<=rowNum;i++){
			row = sheet.getRow(i);
			if(row.getCell(column) == null row.getCell(column).getCellType() == Cell.CELL_TYPE_BLANK ){
				return row;
			}
		}
		rowNum++;
		row = sheet.createRow(rowNum);
		return row;
	}

	public static XSSFWorkbook getExcelTestData(String businessArea, String fileName) {

		try {
			File file = new File("src/test/resources/com/student/TestData/" + businessArea + "/" + fileName);
			FileInputStream filePath = new FileInputStream(file);
			XSSFWorkbook wb = new XSSFWorkbook(filePath);
			return wb;

		} catch (Exception e) {
			LOGGER.info(e);
		}
		return null;
	}

	public List<Map<String, Object>> readExcelList(String sheetName, String linkKey) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		XSSFSheet sheet = getSheet(sheetName);
		BiMap<Integer, String> header = sheetHeaderMap.get(sheetName);
		List<Integer> rowNums = getRowNums(sheet, linkKey);
		Iterator<Integer> it = rowNums.iterator();
		while (it.hasNext()) {
			Map<String, Object> map = new HashMap<String, Object>();
			int rowNum = it.next();
			XSSFRow row = sheet.getRow(rowNum);
			int lastCell = row.getLastCellNum();
			for (int i = 1; i < lastCell; i++) {
				XSSFCell cell = row.getCell(i);
				if (cell.getCellType() != XSSFCell.CELL_TYPE_BLANK) {
					String cellValue = cell.getStringCellValue();
					String last = cellValue.substring(cellValue.lastIndexOf('.') + 1);
					String key = header.get(i);
					Object cellValueObj = cellValue;
					if (last.startsWith(key)) {
						if (last.contains("(list)")) {
							cellValueObj = readExcelList(key, cellValue);
						} else if (last.contains("(map)")) {
							List<Map<String, Object>> lis = readExcelList(key, cellValue);
							if (!lis.isEmpty()) {
								cellValueObj = lis.get(0);
							}
						} else if (last.contains("(listOfString)")) {
							cellValueObj = readExcelList(key, cellValue);
						}
					}
					map.put(key, cellValueObj);
				}
			}
			list.add(map);
		}

		return list;
	}

	public XSSFSheet getSheet(String sheetName) {
		XSSFSheet sheet = wb.getSheet(sheetName);
		if (!sheetHeaderMap.containsKey(sheetName)) {
			BiMap<String, Integer> map = HashBiMap.create();
			XSSFRow row = sheet.getRow(0);
			Iterator<Cell> cells = row.cellIterator();
			int count = 0;
			while (cells.hasNext()) {
				Cell cell = cells.next();
				map.put(cell.getStringCellValue(), count++);
			}
			sheetHeaderMap.put(sheetName, map);
		}

		return sheet;
	}

}
