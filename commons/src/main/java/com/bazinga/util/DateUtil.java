package com.bazinga.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author huliang
 * @version $Id: DateUtil.java, v 0.1 2011-12-19 下午7:23:39 huliang Exp $
 */
public class DateUtil {

	public final static long ONE_DAY_SECONDS = 86400;
	public final static long ONE_DAY_MILL_SECONDS = 86400000;
	public final static String chineseDtFormat = "yyyy年MM月dd日";
	public final static String chineseDtFormat_HHmm = "yyyy年MM月dd日 HH:mm";

	public static final String shortChineseDtFormat = "M月d日";
	public static final String shortChineseDtFormat_Mdd = "M月dd日";
	public static final String MM_DD = "MM-dd";
	public static final String YYYYMD_Point = "yyyy.M.d";
	public static final String YYYYMMDD_Point = "yyyy.MM.dd";
	public static final String YYYYMD_Line = "yyyy-M-d";
	public final static String noSecondFormat = "yyyy-MM-dd HH:mm";
	public final static String MM_dd_HHmm = "MM-dd HH:mm";
	public final static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String yyyyMMdd = "yyyyMMdd";
	public final static String yyMMdd = "yyMMdd";
	public static final String yyyy_MM_dd = "yyyy-MM-dd";
	public final static String slashDateFormat = "yyyy/MM/dd";
	public final static String slashDateFormat2 = "MM/dd/yyyy HH:mm:ss";
	public final static String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
	public final static String YY_MM_DD = "yy-MM-dd";
	public final static String HH_MM = "HH:mm";
	public final static String HH_MM_SS = "HH:mm:ss";
	public final static String HHMMSS = "HHmmss";
	public final static String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	public final static DecimalFormat decimalFormat = new DecimalFormat("00");
	public final static String TIME_SPLIT = ":";

	// public static SimpleDateFormat yyyyMMdd = new
	// SimpleDateFormat("yyyyMMdd");

	/**
	 * 返回当前系统时间 返回的时间格式为 yyyy-MM-dd HH:mm:ss
	 * 
	 * @return
	 */
	public static Date getCurrentDate() {
		return new Date();
	}

	public static int getCurrentYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}


	public static Date getFirstDayOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		//设置为1号,当前日期既为本月第一天
		cal.set(Calendar.DAY_OF_MONTH,1);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return cal.getTime();
	}

	public static Date getFirstDayOfCurrentYear() {
		Calendar ca = Calendar.getInstance();
		int currentYear = ca.get(Calendar.YEAR);
		ca.clear();
		ca.set(Calendar.YEAR, currentYear);
		return ca.getTime();
	}

	/**
	 * 例如：2016年 返回 ‘2016-12-31 23:59:59’
	 * 
	 * @return
	 */
	public static Date getLastDayOfCurrentYear() {
		Calendar ca = Calendar.getInstance();
		int currentYear = ca.get(Calendar.YEAR);
		ca.clear();
		ca.set(Calendar.YEAR, currentYear);
		ca.roll(Calendar.DAY_OF_YEAR, -1);
		ca.set(Calendar.HOUR_OF_DAY, 23);
		ca.set(Calendar.MINUTE, 59);
		ca.set(Calendar.SECOND, 59);
		return ca.getTime();
	}

	public static DateFormat getNewDateFormat(String pattern) {
		DateFormat df = new SimpleDateFormat(pattern);
		df.setLenient(false);
		return df;
	}

	public static String format(Date date, String format) {
		if (date == null) {
			return null;
		}
		return new SimpleDateFormat(format).format(date);
	}

	public static Date parseDateNoTime(String sDate, String format) throws ParseException {
		if (StringUtils.isBlank(format)) {
			throw new ParseException("Null format. ", 0);
		}

		DateFormat dateFormat = new SimpleDateFormat(format);

		if ((sDate == null) || (sDate.length() < format.length())) {
			throw new ParseException("length too little", 0);
		}

		return dateFormat.parse(sDate);
	}

	public static Date parseDateNewFormat(String sDate) {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
		Date d = null;
		dateFormat.setLenient(false);
		if ((sDate != null) && (sDate.length() == DEFAULT_FORMAT.length())) {
			try {
				d = dateFormat.parse(sDate);
			} catch (ParseException ex) {
				return null;
			}
		}
		return d;
	}

	public static Date parseDate(String sDate, String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date d = null;
		dateFormat.setLenient(false);
		if ((sDate != null) && (sDate.length() == format.length())) {
			try {
				d = dateFormat.parse(sDate);
			} catch (ParseException ex) {
				return null;
			}
		}
		return d;
	}

	/**
	 * 计算当前时间几小时之后的时间
	 * 
	 * @param date
	 * @param hours
	 * @return
	 */
	public static Date addHours(Date date, long hours) {
		return addMinutes(date, hours * 60);
	}

	/**
	 * 计算当前时间几分钟之后的时间
	 * 
	 * @param date
	 * @param minutes
	 * @return
	 */
	public static Date addMinutes(Date date, long minutes) {
		return addSeconds(date, minutes * 60);
	}


	public static Date addStockMarketMinutes(Date date, long minutes) {
		String format = DateUtil.format(date, DateUtil.yyyy_MM_dd);
		Date date1130 = DateUtil.parseDate(format + " 11:30:00", DateUtil.DEFAULT_FORMAT);
		Date date1300 = DateUtil.parseDate(format + " 13:00:00", DateUtil.DEFAULT_FORMAT);
		Date startDate = date;
		for (int i=0;i<minutes;i++){
			startDate = addMinutes(startDate,1);
			if((!startDate.before(date1130))&&startDate.before(date1300)){
				startDate = date1300;
			}
		}
		return startDate;
	}

	public static Integer calBetweenStockMinute(String timeStart,String timeEnd){
		Date date1130 = DateUtil.parseDate("11:30", DateUtil.HH_MM);
		Date date1300 = DateUtil.parseDate("13:00", DateUtil.HH_MM);
		Date dateStart = DateUtil.parseDate(timeStart, DateUtil.HH_MM);
		Date dateEnd = DateUtil.parseDate(timeEnd, DateUtil.HH_MM);
		for (int i=1;i<=10000;i++){
			dateStart = addMinutes(dateStart,1);
			if((!dateStart.before(date1130))&&dateStart.before(date1300)){
				dateStart = date1300;
			}
			String format = DateUtil.format(dateStart, DateUtil.HH_MM);
			if(format.equals(timeEnd)){
				return i;
			}
		}
		return null;
	}

	/**
	 * @param date
	 * @param secs
	 * @return
	 */

	public static Date addSeconds(Date date, long secs) {
		return new Date(date.getTime() + (secs * 1000));
	}

	/**
	 * 获取目标日期N个月前/后的日期
	 */
	public static Date addMonths(Date date, int months) {
		Calendar cl = Calendar.getInstance();
		cl.setTime(date);
		cl.add(Calendar.MONTH, months);
		date = cl.getTime();
		return date;
	}

	/**
	 * 判断输入的字符串是否为合法的小时
	 * 
	 * @param hourStr
	 * @return true/false
	 */
	public static boolean isValidHour(String hourStr) {
		if (!StringUtils.isEmpty(hourStr) && StringUtils.isNumeric(hourStr)) {
			int hour = new Integer(hourStr).intValue();

			if ((hour >= 0) && (hour <= 23)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 判断输入的字符串是否为合法的分或秒
	 * 
	 * @param str
	 * @return true/false
	 */
	public static boolean isValidMinuteOrSecond(String str) {
		if (!StringUtils.isEmpty(str) && StringUtils.isNumeric(str)) {
			int hour = new Integer(str).intValue();

			if ((hour >= 0) && (hour <= 59)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得新的日期
	 * 
	 * @param date1
	 *            日期
	 * @param days
	 *            天数
	 * @return 新的日期
	 */
	public static Date addDays(Date date1, long days) {
		return addSeconds(date1, days * ONE_DAY_SECONDS);
	}

	public static String getNewFormatDateString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
		return getDateString(date, dateFormat);
	}

	public static String getDateString(Date date, DateFormat dateFormat) {
		if (date == null || dateFormat == null) {
			return null;
		}

		return dateFormat.format(date);
	}

	/**
	 * 取得“X年X月X日”的日期格式
	 * 
	 * @param date
	 * @return
	 */
	public static String getChineseDateString(Date date) {
		DateFormat dateFormat = getNewDateFormat(chineseDtFormat);

		return getDateString(date, dateFormat);
	}

	/**
	 * 取得两个日期间隔秒数（日期1-日期2）
	 * 
	 * @param one
	 *            日期1
	 * @param two
	 *            日期2
	 * @return 间隔秒数
	 */
	public static long getDiffSeconds(Date one, Date two) {
		Calendar sysDate = new GregorianCalendar();

		sysDate.setTime(one);

		Calendar failDate = new GregorianCalendar();

		failDate.setTime(two);
		return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / 1000;
	}

	public static long getDiffMinutes(Date one, Date two) {
		Calendar sysDate = new GregorianCalendar();

		sysDate.setTime(one);

		Calendar failDate = new GregorianCalendar();

		failDate.setTime(two);
		return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / (60 * 1000);
	}

	/**
	 * 取得两个日期的间隔天数
	 * 
	 * @param one
	 * @param two
	 * @return 间隔天数
	 */
	public static long getDiffDays(Date one, Date two) {
		Calendar sysDate = new GregorianCalendar();

		sysDate.setTime(one);

		Calendar failDate = new GregorianCalendar();

		failDate.setTime(two);
		return (sysDate.getTimeInMillis() - failDate.getTimeInMillis()) / (24 * 60 * 60 * 1000);
	}

	/**
	 * 取得两个日期的间隔天数, 即使昨天的23点与今天凌晨1点, 也是相差1天.
	 * 
	 * @param before
	 * @param after
	 * @return 间隔天数
	 */
	public static long getDiffDays2(Date before, Date after) {
		Calendar c1 = Calendar.getInstance();
		c1.setTime(before);
		c1.set(Calendar.HOUR_OF_DAY, 0);
		c1.set(Calendar.MINUTE, 0);
		c1.set(Calendar.SECOND, 0);
		c1.set(Calendar.MILLISECOND, 0);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(after);
		c2.set(Calendar.HOUR_OF_DAY, 0);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);
		return (c2.getTimeInMillis() - c1.getTimeInMillis()) / (24 * 60 * 60 * 1000);
	}

	public static String convert(String dateString, DateFormat formatIn, DateFormat formatOut) {
		try {
			Date date = formatIn.parse(dateString);

			return formatOut.format(date);
		} catch (ParseException e) {
			return "";
		}
	}

	/**
	 * @param date1
	 * @param date2
	 * @param format
	 * @return
	 */
	public static boolean dateNotLessThan(String date1, String date2, DateFormat format) {
		try {
			Date d1 = format.parse(date1);
			Date d2 = format.parse(date2);

			if (d1.before(d2)) {
				return false;
			} else {
				return true;
			}
		} catch (ParseException e) {
			return false;
		}
	}

	public static String formatTimeRange(Date startDate, Date endDate, String format) {
		if ((endDate == null) || (startDate == null)) {
			return null;
		}

		String rt = null;
		long range = endDate.getTime() - startDate.getTime();
		long day = range / DateUtils.MILLIS_PER_DAY;
		long hour = (range % DateUtils.MILLIS_PER_DAY) / DateUtils.MILLIS_PER_HOUR;
		long minute = (range % DateUtils.MILLIS_PER_HOUR) / DateUtils.MILLIS_PER_MINUTE;

		if (range < 0) {
			day = 0;
			hour = 0;
			minute = 0;
		}

		rt = format.replaceAll("dd", String.valueOf(day));
		rt = rt.replaceAll("hh", String.valueOf(hour));
		rt = rt.replaceAll("mm", String.valueOf(minute));

		return rt;
	}

	/**
	 * 获取系统日期的前一天日期，返回Date
	 */
	public static Date getBeforeDate() {
		return new Date(System.currentTimeMillis() - (ONE_DAY_MILL_SECONDS));
	}

	/**
	 * 获得指定时间当天起点时间
	 * 
	 * @param date
	 * @return
	 */
	public static Date getDayBegin(Date date) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		df.setLenient(false);

		String dateString = df.format(date);

		try {
			return df.parse(dateString);
		} catch (ParseException e) {
			return date;
		}
	}

	/**
	 * 判断参date上min分钟后，是否小于当前时间
	 * 
	 * @param date
	 * @param min
	 * @return
	 */
	public static boolean dateLessThanNowAddMin(Date date, long min) {
		return addMinutes(date, min).before(new Date());

	}

	public static boolean isBeforeNow(Date date) {
		if (date == null)
			return false;
		return date.compareTo(new Date()) < 0;
	}

	public static Date parseNoSecondFormat(String sDate) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(noSecondFormat);

		if ((sDate == null) || (sDate.length() < noSecondFormat.length())) {
			throw new ParseException("length too little", 0);
		}

		if (!StringUtils.isNumeric(sDate)) {
			throw new ParseException("not all digit", 0);
		}

		return dateFormat.parse(sDate);
	}

	/**
	 * 获取当前时间之后（之前）的日期
	 * 
	 * @param days
	 *            天数，可以为负数
	 * @return 指定日期格式的日期增加指定天数的日期
	 */
	public static Date getFutureDay(int days) {
		try {
			Calendar calendar = GregorianCalendar.getInstance();
			Date date = calendar.getTime();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, days);
			return calendar.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取指定时间之后（之前）的日期
	 * 
	 * @param days
	 *            天数，可以为负数
	 * @return 指定日期格式的日期增加指定天数的日期
	 */
	public static Date getFixDayFutureDay(Date fixDay, int days) {
		try {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(fixDay);
			calendar.add(Calendar.DAY_OF_MONTH, days);
			return calendar.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 获取指定年后的日期
	 * 
	 * @param date
	 * @param years
	 */
	public static Date getFixYearFutureYear(Date date, int years) {
		try {
			Calendar calendar = GregorianCalendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.YEAR, 1);
			return calendar.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 判断当前时间，是否在起始时间和结束时间之间，可以精确到秒
	 * 
	 * @param startDate
	 *            其实时间
	 * @param endDate
	 *            结束时间
	 * @return
	 */
	public static boolean betweenStartAndEndDate(Date fixDate, Date startDate, Date endDate) {
		if (null == fixDate) {
			fixDate = new Date();
		}
		return fixDate.before(endDate) && fixDate.after(startDate);
	}

	/**
	 * 获取当前日期是周几
	 * 
	 * @param dt
	 * @return 当前日期是周几
	 */
	public static String getWeekOfDate(Date dt) {
		String[] weekDays = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
		return getWeekOfDate(dt, weekDays);
	}

	/**
	 * 当weekDays 设为：new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
	 * "星期六"} 则返回星期几。
	 * 
	 * @param dt
	 * @param weekDays
	 * @return
	 */
	public static String getWeekOfDate(Date dt, String[] weekDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);

		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if (w < 0){
			w = 0;
		}

		return weekDays[w];
	}

	/**
	 * 例如：19001011
	 * 
	 * @return 19001011
	 */
	public static int getDateAsInt(Date date) {
		String s = new SimpleDateFormat(yyyyMMdd).format(date);
		return Integer.parseInt(s);
	}

	/**
	 * 例如：001011
	 * 
	 * @return 001011
	 */
	public static int getDateAsIntYYMMdd(Date date) {
		String s = new SimpleDateFormat(yyMMdd).format(date);
		return Integer.parseInt(s);
	}

	/**
	 * 1900-10-11
	 * 
	 * @return 19001011
	 */
	public static int date10To8(String date) {
		String s = date.replace("-", "");
		return Integer.parseInt(s);
	}

	/**
	 * convert 19001011 to date
	 * 
	 * @param date
	 *            19001011
	 * @return date
	 */
	public static Date getIntAsDate(Integer date) {
		if (date == null) {
			return null;
		}
		try {
			String dateString = date.toString();
			if (dateString.length() != 8) {
				throw new IllegalArgumentException("date:" + date + " is invalid!");
			}

			return new SimpleDateFormat(yyyyMMdd).parse(date.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 剔除date的time
	 */
	public static Date getDateWithoutTime(Date date) {
		SimpleDateFormat fmt = new SimpleDateFormat(yyyyMMdd);
		String s = fmt.format(date);
		try {
			return fmt.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	public static Date getDateWithFixFormatter(Date date, String formatStr) {
		DateFormat df = new SimpleDateFormat(formatStr);
		df.setLenient(false);

		String dateString = df.format(date);

		try {
			return df.parse(dateString);
		} catch (ParseException e) {
			return date;
		}

	}

	/**
	 * 获取给定时间的下面凌晨两点时间
	 * 
	 * @return
	 */
	public static Date getNextTwoAm(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 2);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}

	/**
	 * 
	 * @Description: 返回Integer类型加Ndays后的Integer 类型时间
	 * @param @param
	 *            date
	 * @param @param
	 *            days
	 * @return Integer 返回类型
	 */
	public static Integer addDates(Integer date, int days) {
		if (date == null) {
			return null;
		}

		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		try {
			Date beginDate = yyyyMMdd.parse(String.valueOf(date));
			return DateUtil.getDateAsInt(DateUtils.addDays(beginDate, days));
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 获取当天时间最后一秒
	 */
	public static Date getDayLastMill(Date date) {
		if (date == null) {
			return null;
		}
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);

			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 0);

			return calendar.getTime();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取指定的时间,格式为"yyyy-MM-dd hh:mm:ss"
	 * 
	 * @param formatStr
	 *            参照 DateUtil中枚举值
	 * @param dateStr
	 *            "1900-1-1 00:00:00"， 依据DateUtil中枚举值来定义参与
	 * @return
	 */
	public static Date getSpecifiedDate(String formatStr, String dateStr) {
		if (dateStr == null) {
			return null;
		}
		Date date = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatStr);
			date = format.parse(dateStr);
		} catch (Exception e) {
			return null;
		}

		return date;

	}

	/**
	 * 获取指定格式的日期时间差 目前暂定格式为:02:30:12[几小时几分钟几秒]
	 */
	public static String getSpecDayDisTimes(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return null;
		}
		try {
			long disSecond = date1.getTime() - date2.getTime();

			long day = disSecond / (24 * 60 * 60 * 1000);
			long hour = (disSecond / (60 * 60 * 1000) - day * 24);
			long min = ((disSecond / (60 * 1000)) - day * 24 * 60 - hour * 60);
			long second = (disSecond / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

			StringBuilder sb = new StringBuilder();

			return sb.append(decimalFormat.format(hour)).append(TIME_SPLIT).append(decimalFormat.format(min)).append(TIME_SPLIT)
					.append(decimalFormat.format(second)).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}



	public static void main(String[] args) {
		System.out.println(getNextTwoAm(new Date()));

		// Calendar c1 = Calendar.getInstance();
		// c1.add(Calendar.DAY_OF_YEAR, -2);
		//
		// // Calendar c2 = Calendar.getInstance();
		//
		// // System.out.println(getDiffDays2(c1.getTime(), c2.getTime()));
		//
		// System.out.println(getFixDayFutureDay(new Date(), 20));
		//
		// System.out.println(getDateWithoutTime(new Date()));

		// System.out.println(getSpecifiedDate(DateUtil.DEFAULT_FORMAT,
		// "1900-1-1 00:00:00"));
		//System.out.println(getFirstDayOfCurrentMonth().toLocaleString());
		System.out.println(getFirstDayOfCurrentYear());
		System.out.println(getLastDayOfCurrentYear());

		System.out.println(getDateAsIntYYMMdd(new Date()));
        System.out.println(getDayLastMill(new Date()));
        System.out.println(getDayBegin(new Date()));
    }

	/**
	 * 判断时间是否重合
	 * 
	 * @param leftStartDate
	 * @param leftEndDate
	 * @param rightStartDate
	 * @param rightEndDate
	 * @return
	 */
	public static boolean isOverlap(Date leftStartDate, Date leftEndDate, Date rightStartDate, Date rightEndDate) {
		return ((leftStartDate.getTime() >= rightStartDate.getTime())
                    && leftStartDate.getTime() < rightEndDate.getTime())  
            ||  
            ((leftStartDate.getTime() > rightStartDate.getTime())   
                    && leftStartDate.getTime() <= rightEndDate.getTime())  
            ||  
            ((rightStartDate.getTime() >= leftStartDate.getTime())   
                    && rightStartDate.getTime() < leftEndDate.getTime())  
            ||  
            ((rightStartDate.getTime() > leftStartDate.getTime())   
                    && rightStartDate.getTime() <= leftEndDate.getTime());  

}  
	
}
