package com.yc.axis;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AxisTest {
	public static void main(String[] args) {
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.receivable.IArapRecBillImportService?wsdl";
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.cust.IArapCustSynchService?wsdl";
//		WSDL2Java.main(new String[] { "-o", "src","-p","com.yc.axis", wsdl });
		try {
//			com.yc.axis.IArapRecBillImportService service = new com.yc.axis.IArapRecBillImportServiceLocator();
//			com.yc.axis.IArapRecBillImportServicePortType port = (com.yc.axis.IArapRecBillImportServicePortType)service.getIArapRecBillImportServiceSOAP11port_http();
//			port.arRecBillImport(new String[] { initJSONObject() });
//			com.yc.axis.IArapCustSynchService service = new com.yc.axis.IArapCustSynchServiceLocator();
//			com.yc.axis.IArapCustSynchServicePortType port = (com.yc.axis.IArapCustSynchServicePortType)service.getIArapCustSynchServiceSOAP11port_http();
//			port.synchArapCust("");
			
			
			
			JSONObject json = new JSONObject();
			json.put("code", "ceshi4567");
			json.put("name", "ceshi6789");
			json.put("pk_org", "1");
			//操作类型，0=删除，1=新增，2=修改
			json.put("op_type", "0");
//			System.out.println(json.toString());
			System.out.println(initJSONObject());
			
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		
		
	}
	
	private static String initJSONObject() {
		JSONObject bill = new JSONObject();
		JSONObject parent = new JSONObject();
		JSONArray children = new JSONArray();
		
		//外系统单据号
		parent.put("def1", "201898099");
		parent.put("billdate", "2016-08-29 11:30:00");
		parent.put("billyear", "2016");
		parent.put("billperiod", "07");
		parent.put("local_money", "333");	
		parent.put("def2", "444");
		parent.put("money", "333");
		parent.put("def3", "333");
		parent.put("pk_currtype", "CNY");
		parent.put("pk_org", "101");
		parent.put("scomment", "pmp测试数据");
		parent.put("def4", "PMP");
		//对应红冲单据号
		parent.put("def5", "");
		parent.put("def6", "04");
		
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
		
		return bill.toString();
	}
}
