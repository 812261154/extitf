package com.yc.axis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nc.bs.arap.util.HttpUtils;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

public class HttpRequestTest {
	
	public static Set <String> set = new HashSet <String>();
	private static String lock = new String();   
	
	public static void main(String[] args) {
		String url = "http://130.1.0.1:8080/api/NC/POST_RetBillStatus";
//		String response = postHttp(url, getMacAddress(), "2016-10-15");

//		System.out.println(response);
		
//		for(int i=0; i<3; i++) {
//			set.add(String.valueOf(i));
//		}
//		
//		for(int i=0; i<10; i++) {
//			MyThread1 tt = new MyThread1();
//			tt.start();
//		}
//		
//		for(int i=0; i<10; i++) {
//			MyThread3 tt = new MyThread3();
//			tt.start();
//		}
//		
//		for(int i=0; i<10; i++) {
//			MyThread2 tt = new MyThread2();
//			tt.start();
//		}
		
//		System.out.println(getMacAddress());
//		System.out.println(getCurrTime());
//		JSONArray array = new JSONArray();
//		JSONArray array2 = new JSONArray();
//		for(int i=0; i<3; i++) {
//			JSONObject t = new JSONObject();
//			t.put("name", "lisi"+i);
//			array.add(t);
//		}
//		System.out.println(array.toString());
//		array2.addAll(array);
//		array.clear();
//		System.out.println(array.toString());
//		System.out.println(array2.toString());
		
		JSONArray rtArray = new JSONArray();
		//for(int i=0; i<3; i++) {
			JSONObject rtJson = new JSONObject();
			rtJson.put("FROMSYS", "PMP");
			rtJson.put("NUMBER", "PR20160800736");
			rtJson.put("COMPLETED", "0");
			rtJson.put("MESSAGE", "dddd");
			rtArray.add(rtJson);
		//}
		try {
			HttpUtils.httpPostWithJSON(rtArray);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static String getCurrTime() {
		return new UFDateTime().toString();
	}
	
	public static void printErrMsg() {
		synchronized(lock) {
			Iterator<String> it = set.iterator(); 
			while(it.hasNext()) {
				String str = it.next();
				System.out.println(str);
				it.remove();
			}
		}
	}
	
	public static void writeErrMsg() {
		synchronized(lock) {
			if(set.size() > 0 && set.size() < 3) {
				System.out.println("ERROR");
			} else if(set.size() == 0) {
				for(int i=0; i<3; i++) {
					set.add(String.valueOf(i));
				}
			}
		}
	}
	
	public static void printErrMsg2() {
		synchronized(lock) {
			for(String str : set) {
				System.out.println(str);
			}
		}
	}
	
	
	
	static class  MyThread1 extends Thread {  
		public void run() {  
			printErrMsg();
		}  
	}
	
	static class MyThread2 extends Thread {  
		public void run() {  
			printErrMsg2();
		}  
	}
	
	static class MyThread3 extends Thread {  
		public void run() {  
			writeErrMsg();
		}  
	}

	/**
	 * post方式
	 * 
	 * @param url
	 * @param code
	 * @param type
	 * @author www.yoodb.com
	 * @return
	 */
	public static String postHttp(String url, String mac, String time,
			String lst_bill_status) {
		String responseMsg = "";
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setContentCharset("GBK");
		PostMethod postMethod = new PostMethod(url);
		postMethod.addParameter("MAC", mac);
		postMethod.addParameter("TIME", time);
		postMethod.addParameter("LST_BILL_STATUS", lst_bill_status);
		// postMethod.addParameter("client_id", DUOSHUO_SHORTNAME);
		// postMethod.addParameter("client_secret", DUOSHUO_SECRET);
		try {
			httpClient.executeMethod(postMethod);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = postMethod.getResponseBodyAsStream();
			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
			}
			responseMsg = out.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}
		return responseMsg;
	}
	
	/** 
     * 按照"XXXXXXXXXXXX"格式，获取本机MAC地址 
     * @return 
     * @throws Exception 
     */  
    public static String getMacAddress() {  
		try {
			Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
			while(ni.hasMoreElements()){  
				NetworkInterface netI = ni.nextElement();  
				byte[] bytes = netI.getHardwareAddress();  
				if(netI.isUp() && netI != null && bytes != null && bytes.length == 6){  
					StringBuffer sb = new StringBuffer();  
					for(byte b:bytes){  
						//与11110000作按位与运算以便读取当前字节高4位  
						sb.append(Integer.toHexString((b&240)>>4));  
						//与00001111作按位与运算以便读取当前字节低4位  
						sb.append(Integer.toHexString(b&15));  
						//sb.append("-");  
					}  
					//sb.deleteCharAt(sb.length()-1);  
					return sb.toString().toUpperCase();   
				}  
			}  
		} catch (Exception e) {
			return "XXXXXXXXXXXX";
		}  
        return "XXXXXXXXXXXX";  
    }  
}
