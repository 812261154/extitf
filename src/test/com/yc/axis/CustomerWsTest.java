package com.yc.axis;

import javax.xml.rpc.ParameterMode;

import net.sf.json.JSONObject;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

public class CustomerWsTest {
	
	
	public static void main(String[] args) {
		try {
			String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.customer.IArapCustomerSynchService";
			Service service = new Service();  
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(wsdl);
            call.setOperationName("synchArapCustomer");
            call.addParameter("string", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);
            Object[] params = new Object[]{ initJSONObject() };
            String val = (String)call.invoke(params);
            System.out.println("远程调用执行结果！");
            System.out.println(val);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String initJSONObject() {
		JSONObject json = new JSONObject();
		json.put("code", "LGSD008");
		json.put("name", "LGSD008");
		json.put("pk_org", "1");
		json.put("op_type", "1");
		json.put("def1", "ZCZL");
		return json.toString();
	}
}
