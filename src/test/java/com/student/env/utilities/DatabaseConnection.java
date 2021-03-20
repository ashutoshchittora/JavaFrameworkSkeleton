package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

public class DatabaseConnection {

	private static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static Connection connectDB(String connStr, String username, String pwd)
			throws ClassNotFoundException, SQLException {

		Connection conn = null;
		if (connStr.contains("oracle")) {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(connStr, username, pwd);

		} else if (connStr.contains("sqlServer")) {
			Class.forName("com.microsoft.jdbs.SQLServerDriver");
			DriverManager.setLoginTimeout(180);
			conn = DriverManager.getConnection(connStr, username, pwd);
		}

		return conn;
	}

	public static ArrayList<String[]> returnSqlQueryResults(Connection conn, String query) throws Throwable {
		ArrayList<String[]> result = new ArrayList<String[]>();
		Statement stmt = null;
		ResultSet rsSql = null;

		try {
			stmt = conn.createStatement();
			stmt.setQueryTimeout(120);
			rsSql = stmt.executeQuery(query);
			int colCount = rsSql.getMetaData().getColumnCount();
			while (rsSql.next()) {
				String[] row = new String[colCount];
				for (int i = 0; i < colCount; i++) {
					row[i] = rsSql.getString(i + 1);
				}
				result.add(row);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rsSql != null) {
				rsSql.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}

		return result;
	}

	public static ResultSet returnScrollableResultSet(String query) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rsSql = null;

		try {
			conn = connectDB("connStr", "username", "pwd");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rsSql = stmt.executeQuery(query);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return rsSql;
	}

	public int executeUpdate(String sQuery) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		int result = 0;

		try {
			conn = connectDB("connStr", "username", "pwd");
			stmt = conn.createStatement();
			result = stmt.executeUpdate(sQuery);
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return result;
	}

	public List<Object> returnResultSetList(String query) throws Throwable {
		List<Object> result = new ArrayList<Object>();
		Connection conn = null;
		Statement stmt = null;
		ResultSet rsSql = null;

		try {
			conn = connectDB("connStr", "username", "pwd");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rsSql = stmt.executeQuery(query);
			result.add(rsSql);
			result.add(stmt);
			result.add(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void executeQuery(String query) throws Exception {
		Connection conn = connectDB("connStr", "username", "pwd");
		Statement stmt = null;
		ResultSet rsSql = null;

		try {
			stmt = conn.createStatement();
			rsSql = stmt.executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (rsSql != null) {
				rsSql.close();
			}
		}
	}
}
