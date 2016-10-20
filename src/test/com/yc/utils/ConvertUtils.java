package com.yc.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class ConvertUtils {
	
	/**
	 * 数组转换成列表
	 * 
	 * @param array
	 * @return
	 */
	public static List<String> arrayToList(String[] array) {
		List<String> list = new ArrayList<String>();
		CollectionUtils.addAll(list, array);
		return list;
	}
	
	/**
	 * 数组转换成Set
	 * @param array
	 * @return
	 */
	public static Set<String> arrayToSet(String[] array) {
		Set<String> set = new HashSet<String>();
		CollectionUtils.addAll(set, array);
		return set;
	}
	
	/**
	 * Map转数组
	 * 
	 * @param map
	 * @return
	 */
	public static String[] mapToArray(Map<String, String> map) {
		String[] array = new String[map.size()];
		return map.values().toArray(array);
	}
	
	/**
	 * List转数组
	 * 
	 * @param list
	 * @return
	 */
	public static String[] listToArray(List<String> list) {
		return (String[]) list.toArray(new String[0]);
	}
	
	
	public static void main(String[] args) {
		String[] a = new String[] {"w", "q", "e"};
		List<String> la = arrayToList(a);
		System.out.println(la.toString());
//		Integer[] a = sort(1,5);
//		for(int i=0; i<a.length; i++) {
//			a[i] = i+10;
//		}
//		for(int i=0; i<a.length; i++) {
//			System.out.println(a[i]);
//		}
	}
}
