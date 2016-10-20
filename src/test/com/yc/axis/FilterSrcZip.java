package com.yc.axis;

import java.io.File;

public class FilterSrcZip {
	
	public static void main(String[] args) {
		filterFile("E:/nc_d/nc/SRC/jar源码/modules");
	}
	
	
	public static void filterFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile() && !tempList[i].endsWith("_src.jar")) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				if(isDirEmpty(temp)) {
					temp.delete();
					continue;
				}
				filterFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
			}
		}
	}
	
	private static boolean isDirEmpty(File file) {
		if (file.isDirectory()) {
	         String[] files = file.list();
	         if (files.length > 0) {
	            return false;
	         }
	      }
		return true;
	}

}
