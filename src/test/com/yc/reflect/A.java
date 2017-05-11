package com.yc.reflect;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class A {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
//		 Constructor[]  cs = "qq".getClass().getDeclaredConstructors();
//		System.out.println((int)1.9);
		
//		String str = "abcdefg";
//		FileWriter fw = new FileWriter("D:/json.txt");
//		fw.write(str);
//		fw.flush();
		
		byte[] bytesOfMessage = "yc".getBytes("UTF-8");
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] thedigest = md.digest(bytesOfMessage);
		System.out.println(thedigest.toString());
		
	}
}
