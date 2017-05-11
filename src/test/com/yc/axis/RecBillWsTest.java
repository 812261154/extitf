package com.yc.axis;

import javax.xml.rpc.ParameterMode;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

public class RecBillWsTest {
	
	
	public static void main(String[] args) {
		try {
			String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.receivable.IArapRecBillImportService";
			Service service = new Service();  
            Call call = (Call) service.createCall();
            call.setTargetEndpointAddress(wsdl);
            call.setOperationName("arRecBillImport");
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
		return "[{\"parent\":{\"def1\":\"1040_20161121\",\"billdate\":\"2016-11-21 00:00:00\",\"billyear\":\"2016\",\"billperiod\":\"11\",\"local_money\":\"600.0000\",\"def2\":\"600.0000\",\"money\":\"600.0000\",\"def3\":\"600.0000\",\"pk_currtype\":\"CNY\",\"pk_org\":\"10114\",\"scomment\":\"-\",\"def4\":\"ZCZL\",\"def5\":\"\",\"def6\":\"1040\",\"def7\":\"XHX01\"},\"children\":[{\"material\":\"010201\",\"objtype\":\"0\",\"customer\":\"LGSD008\",\"pk_currtype\":\"CNY\",\"rate\":\"1.0000\",\"money_de\":\"600.0000\",\"def3\":\"\",\"local_money_de\":\"600.0000\",\"def4\":\"\",\"taxrate\":\"0.0000\",\"pk_subjcode\":\"01\",\"def10\":\"uuuuuuuuuuuuu\",\"taxtype\":\"0\"}],\"needarap\":\"N\"}]";
//		return "[{\"parent\":{\"def1\":\"1040_20161121\",\"billdate\":\"2016-11-21 00:00:00\",\"billyear\":\"2016\",\"billperiod\":\"11\",\"local_money\":\"600.0000\",\"def2\":\"600.0000\",\"money\":\"600.0000\",\"def3\":\"600.0000\",\"pk_currtype\":\"CNY\",\"pk_org\":\"10114\",\"scomment\":\"-\",\"def4\":\"PMP\",\"def5\":\"\",\"def6\":\"1040\",\"def7\":\"XHX01\"},\"children\":[{\"material\":\"010201\",\"objtype\":\"0\",\"customer\":\"LGSD008\",\"pk_currtype\":\"CNY\",\"rate\":\"1.0000\",\"money_de\":\"600.0000\",\"def1\":\"600.0000\",\"def3\":\"\",\"local_money_de\":\"600.0000\",\"def2\":\"600.0000\",\"def4\":\"\",\"taxrate\":\"0.0000\",\"taxtype\":\"0\"}],\"needarap\":\"N\"}]";
	}
}
