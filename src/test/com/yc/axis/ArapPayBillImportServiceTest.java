package com.yc.axis;

import nc.bs.arap.util.BillUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class ArapPayBillImportServiceTest {
	public static void main(String[] args) {
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.payable.IArapPayBillImportService?wsdl";
//		WSDL2Java.main(new String[] { "-o", "src","-p","com.yc.axis", wsdl });
//		System.out.println("执行完毕！");
		try {
			IArapPayBillImportService service = new IArapPayBillImportServiceLocator();
			IArapPayBillImportServicePortType port = (IArapPayBillImportServicePortType)service.getIArapPayBillImportServiceSOAP11port_http();
			System.out.println("远程调用执行结果！");
			System.out.println(port.apPayBillImport(initJSONObject()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String initJSONObject() {
		
		JSONArray array = new JSONArray();
		for(int i=0; i<2; i++) {
			JSONObject bill = new JSONObject();
			JSONObject parent = new JSONObject();
			JSONArray children = new JSONArray();
			
			//外系统单据号
			parent.put("def1", BillUtils.getUUID());
			parent.put("billdate", "2016-07-28 11:30:00");
			parent.put("billyear", "2016");
			parent.put("billperiod", "07");
			parent.put("local_money", "333");	
			parent.put("def2", "444");
			parent.put("money", "333");
			parent.put("def3", "333");
			parent.put("pk_currtype", "CNY");
			parent.put("pk_org", "101");
			parent.put("def6", "dddd");
			parent.put("scomment", "pmp测试数据");
			parent.put("def4", "PMP");
			//对应红冲单据号
			parent.put("def5", "");
			
			JSONObject child1 = new JSONObject();
			//物料：9,13,17,17+
			child1.put("material", "6");
			child1.put("objtype", "0");
			child1.put("supplier", "0001");
			child1.put("pk_currtype", "CNY");
			child1.put("rate", "1");
			child1.put("money_cr", "111");
			child1.put("def1", "222");
			child1.put("local_money_cr", "111");
			child1.put("def2", "222");
			child1.put("taxrate", "0");
			child1.put("taxtype", "0");
			children.add(child1);
			
			JSONObject child2 = new JSONObject();
			//物料：9,13,17,17+
			child2.put("material", "6");
			child2.put("objtype", "0");
			child2.put("supplier", "0001");
			child2.put("pk_currtype", "CNY");
			child2.put("rate", "1");
			child2.put("money_cr", "222");
			child2.put("def1", "222");
			child2.put("local_money_cr", "222");
			child2.put("def2", "222");
			child2.put("taxrate", "0");
			child2.put("taxtype", "0");
			children.add(child2);
			
			bill.put("parent", parent);
			bill.put("children", children);
			
			array.add(bill);
		}
		
		return array.toString();
	}
}
