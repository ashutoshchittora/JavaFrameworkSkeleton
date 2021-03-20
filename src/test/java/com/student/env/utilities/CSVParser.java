package com.student.env.utilities;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVParser {
	private static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static void convertToCSV(String inputFile, String outputFile, char currentSeperator) throws IOException {

		XSSFWorkbook wBook = new XSSFWorkbook(new FileInputStream(inputFile));
		DataFormatter formatter = new DataFormatter();
		PrintStream out = new PrintStream(new FileOutputStream(outputFile), true, "UTF-8");
		for (Sheet sheet : wBook) {
			for (Row row : sheet) {
				boolean firstCell = true;
				for (Cell cell : row) {
					if (!firstCell)
						out.print(',');
					String text = formatter.formatCellValue(cell);
					String formattedText = text.replace(currentSeperator, ',');
					out.print(formattedText);
					LOGGER.info(formattedText);
					firstCell = false;

				}
				out.println();
			}
		}

		if (out != null) {
			out.close();
			wBook.close();
		}
	}

	public static void updateCSV(String fileToUpdate, String replace, int row, int col) throws IOException {

		File inputFile = new File(fileToUpdate);

		// read existing file

		CSVReader reader = new CSVReader(new FileReader(inputFile), ',');
		List<String[]> csvBody = reader.readAll();
		// get csv row-col and replace with input method arg
		csvBody.get(row)[col] = replace;
		reader.close();

		// write to the opened csv file
		CSVWriter writer = new CSVWriter(new FileWriter(inputFile), ',');
		writer.writeAll(csvBody);
		writer.flush();
		writer.close();

	}

	public static void writeOrAppendContentToCSV(String outputFile, List<String> lsContent, boolean appendOrWrite)
			throws Exception {
		FileWriter writer = appendOrWrite ? new FileWriter(new File(outputFile), true)
				: new FileWriter(new File(outputFile));
		for (String val : lsContent) {
			writer.append(val);
			writer.append("\n");
		}

		writer.close();
	}

	public static void writeRecordsToCSV(String outputCSVFile, List<String[]> fileContents, boolean appendOrCreate)
			throws Exception {

		try {
			FileWriter writer = appendOrCreate ? new FileWriter(new File(outputCSVFile), true)
					: new FileWriter(new File(outputCSVFile));
			CSVWriter csvWriter = new CSVWriter(writer);
			for (String[] eachLine : fileContents) {
				csvWriter.writeNext(eachLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String[]> getCSVFileHeaders(String delimitedFile, char delimeter) throws Exception {
		CSVReader csvReader = new CSVReader(new FileReader(delimitedFile), delimeter);
		String[] headers = csvReader.readNext(); // get only header
		List<String[]> headerInfo = new ArrayList<String[]>();
		headerInfo.add(headers);
		csvReader.close();
		return headerInfo;
	}

	public static List<String[]> getRecordsFromDelimitedFile(String delimitedFile, char delimeter, boolean withheaders)
			throws Exception {
		CSVReader csvreader = new CSVReader(new FileReader(delimitedFile), delimeter);

		// if you need the records with headers

		if (!withheaders) {
			csvreader.readNext(); // skip the headers
		}
		String[] lines = null;
		List<String[]> csvContents = new ArrayList<String[]>();
		while ((lines = csvreader.readNext()) != null) {
			csvContents.add(lines);

		}
		csvreader.close();
		return csvContents;
	}

	public static List<String> getListOfSpecificColumnRecords(List<String[]> allRecords, String columnName)
			throws Exception {

		List<String> csvRecords = new ArrayList<String>();
		int columnPos = 0;
		boolean foundColumn = false;
		for (String[] eachRecord : allRecords) {
			if (!foundColumn) {
				for (int i = 0; i < eachRecord.length; i++) {
					if (eachRecord[i].equalsIgnoreCase(columnName)) {
						columnPos = i;
						
						foundColumn = true;
						break;
					}
				}
				if (foundColumn) {
					continue;
				} else {
					throw new Exception("no column found with this name : " + columnName);
				}
			}
			csvRecords.add(eachRecord[columnPos]);
		}

		return csvRecords;
	}

}
