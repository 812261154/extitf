package com.yc.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * ���ڹ�����
 * 
 * @author superfish0714@163.com
 *
 */
public class DateUtils {
	
	public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
	public static final String PATTERN_DATE = "yyyy-MM-dd";
	public static final String PATTERN_TIME = "HH:mm:ss";
	
	public static final long MILLIS_PER_SECOND = 1000L;
	public static final long MILLIS_PER_MINUTE = 60000L;
	public static final long MILLIS_PER_HOUR = 3600000L;
	public static final long MILLIS_PER_DAY = 86400000L;
	
	
	/**
	 * �ַ�����ʽ�����ڱȽ�
	 * 
	 * @param dateStr1
	 * @param dateStr2
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static int compareTo(String dateStr1, String dateStr2, String format) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date1 = sdf.parse(dateStr1);
		Date date2 = sdf.parse(dateStr2);
		return date1.compareTo(date2);
	}
	
	/**
	 * �ַ�����ʽ�����ڱȽ�
	 * 
	 * @param dateStr1
	 * @param format1
	 * @param dateStr2
	 * @param format2
	 * @return
	 * @throws ParseException
	 */
	public static int compareTo(String dateStr1, String format1, String dateStr2, String format2) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat(format1);
		SimpleDateFormat sdf2 = new SimpleDateFormat(format2);
		Date date1 = sdf1.parse(dateStr1);
		Date date2 = sdf2.parse(dateStr2);
		return date1.compareTo(date2);
	}
	
	/**
	 * ȡ��ǰʱ��
	 * 
	 * @param format
	 * @return
	 */
	public static Date getCurrDate() {
		Calendar c = Calendar.getInstance();
		return c.getTime();
	}
	
	/**
	 * ȡ��ǰʱ��
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurrDate(String format) {
		Calendar c = Calendar.getInstance();
		Date date = c.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * 2016-01-02 to 2016
	 * 
	 * @param str
	 * @return
	 */
	public static String getMonthFromStr(String str) {
		return str.split("-")[1];
	}
	
	/**
	 * 2016-01-02 to 01
	 * 
	 * @param str
	 * @return
	 */
	public static String getYearFromStr(String str) {
		return str.split("-")[0];
	}
	
	/**
	 * �ж��ַ����Ƿ�������
	 * 
	 * @param dateStr
	 * @param format
	 * @return
	 */
	public static boolean isDateString(String dateStr, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		boolean isLegal = false;
		if(StringUtils.isNotEmpty(dateStr)) {
			try {
				//lenientΪfalse;����SimpleDateFormat��ȽϿ��ɵ���֤���ڣ�����2007/02/29�ᱻ���ܣ���ת����2007/03/01 
				sdf.setLenient(false);
				sdf.parse(dateStr);
				isLegal = true;
			} catch (ParseException e) {
				isLegal = false;
			}
		}
		return isLegal;
	}
	
	/**
	 * �ַ���ת��������
	 * 
	 * @param dateStr
	 * @param format
	 * @return
	 * @throws ParseException
	 */
	public static Date parse(String dateStr, String format) throws ParseException {
		if (StringUtils.isNotBlank(dateStr)) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.parse(dateStr);
		} else {
			return null;
		}
	}
	
	/**
	 * ����ת�����ַ���
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String format(Date date, String format) {
		if (date != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return sdf.format(date);
		} else {
			return null;
		}
	}
	
	/**
	 * ��ǰ���ڵĶ�����֮��
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date getDateAfterDay(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, amount);
		return c.getTime();
	}
	
	/**
	 * ��ǰ���ڵĶ���Сʱ֮��
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date getDateAfterHour(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, amount);
		return c.getTime();
	}
	
	/**
	 * ��ǰ���ڵĶ��ٷ���֮��
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date getDateAfterMinute(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, amount);
		return c.getTime();
	}
	
	/**
	 * ��ǰ���ڵĶ��ٷ���֮��
	 * 
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date getDateAfterSecond(Date date, int amount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, amount);
		return c.getTime();
	}
	
	
	public static void main(String[] args) {
		Date date = new Date();
		System.out.println(format(date, DateUtils.PATTERN_DATETIME));
		System.out.println(format(getDateAfterSecond(date, 61), DateUtils.PATTERN_DATETIME));
		
	}
}
