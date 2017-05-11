package com.yc.sort;

import java.util.Arrays;



public class Sort {
	public static void main(String[] args) {
		int[] datas = { 3,3,5,6,7,8,9,2, 3,4 };
		quick(datas);
		System.out.println(Arrays.toString(datas));
	}
	
	public static void quick(int[] datas) {
		if(datas.length > 0) {
			quickSort(datas, 0, datas.length-1);
		}
	}
	
	public static void quickSort(int[] datas, int begin, int end) {
		if(begin < end) {
			int middlePosition = getMiddlePosition(datas, begin, end);
			quickSort(datas, begin, middlePosition-1);
			quickSort(datas, middlePosition+1, end);
		}
	}
	
	public static int getMiddlePosition(int[] datas, int begin, int end) {
		int middleData = datas[begin];
		
		while(begin < end) {
			
			while(begin < end && datas[end] > middleData) {
				end--;
			}
			datas[begin] = datas[end];
			
			while(begin < end && datas[begin] <= middleData) {
				begin++;
			}
			datas[end] = datas[begin];
		}
		
		datas[begin] = middleData;
		return begin;
	}
}
