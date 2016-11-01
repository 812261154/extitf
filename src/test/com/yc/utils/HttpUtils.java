package com.yc.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

	private static HttpClient httpClient;
	// Ĭ�ϱ���
	private static String chaset = "UTF-8";
	// �ٶ��������
	public static String baiduSpider = "Baiduspider+(+http://www.baidu.com/search/spider.htm)";
	
	/**
	 * ��ʼ��HttpClientʵ��
	 */
	static {
		httpClient = new DefaultHttpClient();
	}
	
	/**
	 * ��ȡHttpClientʵ��
	 */
	public static HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * get����(��������)
	 * 
	 * @param url ����url
	 * @return html ҳ������
	 */
	public static String get(String url) {
		HttpGet httpGet = new HttpGet(url);
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}
	
	/**
	 * get����(��������)
	 * 
	 * @param url ����url
	 * @param headers ����ͷ
	 * @return html ҳ������
	 */
	public static String get(String url, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(url);
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			httpGet.setHeader(entry.getKey(), entry.getValue());
		}
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}

	/**
	 * get����(������)
	 * 
	 * @param url ����url
	 * @param params get�������
	 * @return html ҳ������
	 */
	public static String getWithParams(String url, Map<String, String> paramsMap) {
		url = url + "?" + parseParams(paramsMap);
		HttpGet httpGet = new HttpGet(url);
		String html = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
		}
		return html;
	}

	/**
	 * post����
	 * 
	 * @param url ����url
	 * @param params get�������
	 * @return html ҳ������
	 */
	public static String post(String url, Map<String, String> paramsMap){
		HttpPost httpPost = new HttpPost(url);
		String html = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			html = EntityUtils.toString(entity, chaset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpPost.releaseConnection();
		}
		return html;
	}
	
	/**
	 * ���ñ���
	 * 
	 * @param chaset
	 * @return
	 */
	public static void setCharset(String chaset){
		HttpUtils.chaset = chaset;
	}
	
	/**
	 * ת�������б�����get����
	 * 
	 * @param paramsMap 
	 * @return 
	 */
	private static String parseParams(Map<String, String> paramsMap) {
		String params = "";
		for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
			params += entry.getKey() + "=" + entry.getValue() + "&";
		}
		return params.substring(0, params.length() - 1);
	}
	
	/**
	 * ת���&#xhhhh;��ʽ
	 * 
	 * @param strInput
	 * @return result
	 */
	public static String parseStr(String strInput) {
        StringBuffer strOutput = new StringBuffer();
        for (int i = 0; i < strInput.length(); i++)
        {
        	strOutput.append("&#x" +Integer.toString(strInput.charAt(i), 16));
        }        
        return strOutput.toString();
	}

}
