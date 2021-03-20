package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.time.LocalDate;


public class DateUtil {

	private static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static String getTodayDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(new Date());
	}

	public static String getYesterdayDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(cal.getTime());

	}

	public static String getNDayBeforeDate(int nDaysBefore) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -nDaysBefore);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(cal.getTime());
	}

	public static String getFutureDateInSpecifiedFormat(int nDaysAfter, String format) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, nDaysAfter);
		SimpleDateFormat formatOf = new SimpleDateFormat("yyyy-MM-dd");
		return formatOf.format(cal.getTime());
	}

	public static String getTodayDateInSpecificFormat(String dateFormat) {
		SimpleDateFormat formatOf = new SimpleDateFormat(dateFormat);
		return formatOf.format(new Date());
	}

	public static String getTimeStamp() {
		String timestamp = new java.text.SimpleDateFormat("hhmmss").format(new Date());
		return timestamp;
	}

	public static String getDateTimeStamp() {
		String timestamp = new java.text.SimpleDateFormat("ddMMYYYYhhmmss").format(new Date());
		return timestamp;
	}

	public static String getFutureDate(int noOfDays, String format) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		LocalDate parsedDate = LocalDate.parse(LocalDate.now().format(formatter), formatter);
		LocalDate plusDays = parsedDate.plusDays(noOfDays);
		return plusDays.format(formatter);
	}

	public static String addDaysTodate(int noOfDays, String sDate, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(sDate));
		cal.add(Calendar.DATE, noOfDays);
		return dateFormat.format(cal.getTime());
	}

	public static String transformDate(String actualDate, String fromFormat, String targetFormat) {
		SimpleDateFormat ff = new SimpleDateFormat(fromFormat);
		SimpleDateFormat tf = new SimpleDateFormat(targetFormat);

		try {
			return tf.format(ff.parse(actualDate));
		} catch (ParseException e) {
			e.printStackTrace();
			return actualDate;
		}
	}

	protected static String getTodayDateUTCformat() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(new Date());
	}

}
