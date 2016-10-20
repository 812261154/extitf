package com.yc.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class ConvertUtils {
	
	/**
	 * ����ת�����б�
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
	 * ����ת����Set
	 * @param array
	 * @return
	 */
	public static Set<String> arrayToSet(String[] array) {
		Set<String> set = new HashSet<String>();
		CollectionUtils.addAll(set, array);
		return set;
	}
	
	/**
	 * Mapת����
	 * 
	 * @param map
	 * @return
	 */
	public static String[] mapToArray(Map<String, String> map) {
		String[] array = new String[map.size()];
		return map.values().toArray(array);
	}
	
	/**
	 * Listת����
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
