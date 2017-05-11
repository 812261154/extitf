package nc.bs.arap.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import nc.vo.pub.lang.UFDateTime;

public class DateUtils {
	
	//2016-01-01 12:01:01  to  20160101
	public static String formatDateFileStr(UFDateTime datetime) {
		if(datetime == null)
			return "";
		return datetime.toString().substring(0, 10).replace("-", "");
	}
	
	//2016-01-01 12:01:01  to  20160101120101
	public static String formatDatetimeFileStr(UFDateTime datetime) {
		if(datetime == null)
			return "";
		return datetime.toString().replace("-", "").replace(" ", "").replace(":", "");
	}
	
	//2016-01-01 to 2016
	public static String getYearFromStr(String str) {
		return str.split("-")[0];
	}
	
	//2016-01-01 to 2016
	public static String getMonthFromStr(String str) {
		return str.split("-")[1];
	}
	
	//��ȡ��ǰʱ��
	public static String getCurrTime() {
		return new UFDateTime().toString();
	}
	
	//ȡǰN���Ŀ¼
	public static String getDateFileStrBefore(UFDateTime datetime, int diff) {
		return formatDateFileStr(new UFDateTime(datetime.getMillis() + 24 * 60 * 60 * 1000L * (long) diff));
	}  
	
	//�ж��ַ����Ƿ��Ǳ�׼�����ڸ�ʽ
	public static boolean isFormatDateTimeString(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		boolean isLegal = false;
		if(StringUtils.isNotEmpty(str)) {
			try {
				sdf.setLenient(false);
				sdf.parse(str);
				isLegal = true;
			} catch (ParseException e) {
				isLegal = false;
			}
		}
		return isLegal;
	}
	
	//�ж��ַ����Ƿ��Ǳ�׼�����ڸ�ʽ
	public static boolean isFormatDateString(String str) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		boolean isLegal = false;
		if(StringUtils.isNotEmpty(str)) {
			try {
				sdf.setLenient(false);
				sdf.parse(str);
				isLegal = true;
			} catch (ParseException e) {
				isLegal = false;
			}
		}
		return isLegal;
	}
}
