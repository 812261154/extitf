package com.yc.reflect;

import java.io.FileWriter;
import java.io.IOException;


public class A {
	public static void main(String[] args) throws IOException {
//		 Constructor[]  cs = "qq".getClass().getDeclaredConstructors();
//		System.out.println((int)1.9);
		
		String str = "abcdefg";
		FileWriter fw = new FileWriter("D:/json.txt");
		fw.write(str);
		fw.flush();
	}
}
