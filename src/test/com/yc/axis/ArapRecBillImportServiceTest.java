package com.yc.axis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import nc.bs.arap.util.BillUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ArapRecBillImportServiceTest {
	public static void main(String[] args) {
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.receivable.IArapRecBillImportService?wsdl";
//		WSDL2Java.main(new String[] { "-o", "src","-p","com.yc.axis", wsdl });
		try {
			IArapRecBillImportService service = new IArapRecBillImportServiceLocator();
			IArapRecBillImportServicePortType port = (IArapRecBillImportServicePortType)service.getIArapRecBillImportServiceSOAP11port_http();
			System.out.println("远程调用执行结果！");
			System.out.println(port.arRecBillImport(initJSONObject()));
//			String str = "[{\"parent\":{\"def1\":\"RP20160811196\",\"billdate\":\"2016-09-01 10:35:44\",\"billyear\":\"2016\",\"billperiod\":\"09\",\"local_money\":\"242.00\",\"def2\":\"242.00\",\"MONEY\":\"242.00\",\"def3\":\"242.00\",\"pk_currtype\":\"CNY\",\"pk_org\":\"010001\",\"scomment\":\"发行集团沙井书城WS20160800043 /SL20160800106\",\"def4\":\"PMP\",\"def5\":\"\",\"def6\":\"X01\",\"def7\":\"2\"},\"children\":[{\"material\":\"0102\",\"objtype\":\"0\",\"customer\":\"010020\",\"pk_currtype\":\"CNY\",\"rate\":\"1.0000\",\"money_de\":\"242.00\",\"def1\":\"242.00\",\"def3\":\"1030\",\"local_money_de\":\"242.00\",\"def2\":\"242.00\",\"taxrate\":\"0.00\",\"taxtype\":\"0\"}],\"needarap\":\"Y\"},{\"parent\":{\"def1\":\"RP20160812082\",\"billdate\":\"2016-08-31 16:38:47\",\"billyear\":\"2016\",\"billperiod\":\"08\",\"local_money\":\"1236.00\",\"def2\":\"1236.00\",\"MONEY\":\"1236.00\",\"def3\":\"1236.00\",\"pk_currtype\":\"CNY\",\"pk_org\":\"010001\",\"scomment\":\"\",\"def4\":\"PMP\",\"def5\":\"\",\"def6\":\"X01\",\"def7\":\"2\"},\"children\":[{\"material\":\"0101\",\"objtype\":\"0\",\"customer\":\"010020\",\"pk_currtype\":\"CNY\",\"rate\":\"1.0000\",\"money_de\":\"1236.00\",\"def1\":\"1236.00\",\"def3\":\"1030\",\"local_money_de\":\"1236.00\",\"def2\":\"1236.00\",\"taxrate\":\"0.00\",\"taxtype\":\"0\"}],\"needarap\":\"Y\"}]";
//			System.out.println(port.arRecBillImport(str));
			
//			File file=new File("D:/json.txt");
//	         if(!file.exists()||file.isDirectory())
//	             throw new FileNotFoundException();
//	         BufferedReader br=new BufferedReader(new FileReader(file));
//	         InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "utf-8");
//	         BufferedReader br = new BufferedReader(isr);
//	         String temp=null;
//	         StringBuffer sb=new StringBuffer();
//	         temp=br.readLine();
//	         while(temp!=null){
//	             sb.append(temp);
//	             temp=br.readLine();
//	         }
//	         System.out.println(port.arRecBillImport(sb.toString()));
//	         System.out.println(getTxtType(file));
//	         System.out.println(new String(sb.substring(66300, 66370).getBytes(), "utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getTxtType(File file) throws IOException {
		// TODO Auto-generated method stub
		InputStream inputStream = new FileInputStream(file);
		byte[] head = new byte[3];
		inputStream.read(head);
		String code = "";
		code = "gb2312";
		if (head[0] == -1 && head[2] == -2) {
			code = "UTF-16";
		}
		if (head[0] == -2 && head[2] == -1) {
			code = "Unicode";
		}
		if (head[0] == -17 && head[2] == -69) {
			code = "UTF-8";
		}
		return code;
	}
	
	
	private static String initJSONObject() {
		JSONArray array = new JSONArray();
		for(int i=0; i<2; i++) {
			JSONObject bill = new JSONObject();
			JSONObject parent = new JSONObject();
			JSONArray children = new JSONArray();
			
			//外系统单据号
			parent.put("def1", BillUtils.getUUID());
			parent.put("billdate", "2016-09-07 00:00:00");
			parent.put("billyear", "2016");
			parent.put("billperiod", "07");
			parent.put("local_money", "333");	
			parent.put("def2", "444");
			parent.put("money", "333");
			parent.put("def3", "333");
			parent.put("pk_currtype", "CNY");
			parent.put("pk_org", "10100");
			parent.put("scomment", "pmp测试数据");
			parent.put("def4", "PMP");
			//对应红冲单据号
			parent.put("def5", "");
			parent.put("def6", "04");
			parent.put("def7", "1101");
			
			JSONObject child1 = new JSONObject();
			//物料：9,13,17,17+
			child1.put("material", "6");
			child1.put("objtype", "0");
			child1.put("customer", "k0001");
			child1.put("pk_currtype", "CNY");
			child1.put("rate", "1");
			child1.put("money_de", "111");
			child1.put("def1", "222");
			child1.put("local_money_de", "111");
			child1.put("def2", "222");
			child1.put("taxrate", "0");
			child1.put("taxtype", "0");
			children.add(child1);
			
			JSONObject child2 = new JSONObject();
			//物料：9,13,17,17+
			child2.put("material", "6");
			child2.put("objtype", "0");
			child2.put("customer", "k0001");
			child2.put("pk_currtype", "CNY");
			child2.put("rate", "1");
			child2.put("money_de", "222");
			child2.put("def1", "222");
			child2.put("local_money_de", "222");
			child2.put("def2", "222");
			child2.put("taxrate", "0");
			child2.put("taxtype", "0");
			children.add(child2);
			
			bill.put("parent", parent);
			bill.put("children", children);
			bill.put("needarap", "N");
			array.add(bill);
		}
		
		return array.toString();
	}
}
