package com.yc.axis;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class UTF8Test {
	public static void main(String[] str) throws UnsupportedEncodingException {
		String gb2312Str = "转换之前的";
		System.out.println(formatUTF8(gb2312Str));
		System.out.println(gb2312Str);
		String newStr = new String(gb2312Str.getBytes("GB2312"),"utf-8");
		System.out.println(formatUTF8(newStr));
		System.out.println(newStr);
	}
	
	public static String formatUTF8(String str) throws UnsupportedEncodingException {
		Set<String> set = new HashSet<String>();
		set.add("GB2312");
		set.add("ISO-8859-1");
		set.add("GBK");
		for(String type : set) {
			if (str.equals(new String(str.getBytes(type), type))) { 
				return type;
			} 
		}
		return str;
	}
}
