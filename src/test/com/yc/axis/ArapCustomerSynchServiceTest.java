package com.yc.axis;

import nc.bs.arap.util.TransUtils;

import org.apache.axis.wsdl.WSDL2Java;

public class ArapCustomerSynchServiceTest {
	public static void main(String[] args) {
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.customer.IArapCustomerSynchService?wsdl";
//		WSDL2Java.main(new String[] { "-o", "src","-p","com.yc.axis", wsdl });
//		System.out.println("ִ����ϣ�");
		
		try {
			IArapCustomerSynchService service = new IArapCustomerSynchServiceLocator();
			IArapCustomerSynchServicePortType port = (IArapCustomerSynchServicePortType)service.getIArapCustomerSynchServiceSOAP11port_http();
			System.out.println("Զ�̵���ִ�н����");
			//System.out.println(port.synchArapCustomer("{\"code\":\""+ArapBillImportUtils.getUUID()+"\",\"name\":\"�ڲ�����\",\"pk_org\":\"101\",\"op_type\":\"1\"}"));
			System.out.println(port.synchArapCustomer("{\"code\":\"-\",\"name\":\"test\",\"pk_org\":\"1\",\"op_type\":\"1\"}"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
