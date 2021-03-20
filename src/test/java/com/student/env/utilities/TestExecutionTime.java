package com.student.env.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TestExecutionTime {
	public static String insertStringSeperator = "','";
	public static String startLogger = "";
	public static String endLogger = "";
	public static String logger = "";
	public static String dateFormat = "yyyy/MM/dd HH:mm:ss";
	public static boolean startLogging = false;

	public static void startLogger() {
		startLogger = startLoggingUTC();
	}

	public static void endLogger() {
		endLogger = startLoggingUTC();
		;
	}

	private static Connection getDBConnection() throws Exception {
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		Connection conn = null;
		return conn;
	}

	public static String startLoggingUTC() {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar cal = Calendar.getInstance(timeZone);
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		format.setTimeZone(timeZone);
		logger = format.format(cal.getTime());

		return logger;
	}

	public static void logTime(String scenarioName) {
		endLogger();
		String logTime = System.getProperty("Log_TestExecution_Time") == null ? "false"
				: System.getProperty("Log_TestExecution_Time");
		startLogging = logTime.equalsIgnoreCase("true");
		if (!startLogging)
			return;
		String testCaseId = scenarioName.replaceAll("[^0-9]", "");

		try {
			Connection con = getDBConnection();
			Statement stmt = con.createStatement();
			String studentTestProject = System.getProperty("user.dir")
					+ "\\src\\test\\resources\\config\\studentTestCase.json";
			String sScriptType = JsonParser.getArrayElementFromFile(studentTestProject, "studentTestCaseId",
					testCaseId) == null ? "staging" : "uat";
			StringBuilder sQuery = new StringBuilder();
			Date d1 = new SimpleDateFormat(dateFormat).parse(startLogger);
			Date d2 = new SimpleDateFormat(dateFormat).parse(endLogger);

			long diff = d2.getTime() - d1.getTime();
			long diffSeconds = diff / 1000;

			sQuery.append(
					"INSERT INTO [dbo].[StudentTestCaseExecutionTimeTable] ([TestcaseTitle],[StartTime],[EndTime] ,[TotalTime] ,[TestCaseId] VALUES");
			sQuery.append("('" + scenarioName + insertStringSeperator + startLogger + endLogger + "')");
			stmt.setQueryTimeout(15);
			stmt.executeUpdate(sQuery.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static long logTimeOnConsole() throws Throwable {
		endLogger();

		Date d1 = new SimpleDateFormat(dateFormat).parse(startLogger);
		Date d2 = new SimpleDateFormat(dateFormat).parse(endLogger);
		long diff = d2.getTime() - d1.getTime();
		long diffSeconds = diff / 1000;
		return diffSeconds;

	}

	private static String getIPAddress() {
		InetAddress localhost = null;
		try {
			localhost = InetAddress.getLocalHost();
			return localhost.getHostAddress().trim();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return "IPNotFound";
	}

}
