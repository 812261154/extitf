package com.yc.axis;

import nc.bs.arap.util.TransUtils;

import org.apache.axis.wsdl.WSDL2Java;

public class ArapSupplierSynchServiceTest {
	public static void main(String[] args) {
//		String wsdl = "http://127.0.0.1:65/uapws/service/nc.itf.arap.supplier.IArapSupplierSynchService?wsdl";
//		WSDL2Java.main(new String[] { "-o", "src","-p","com.yc.axis", wsdl });
//		System.out.println("ִ����ϣ�");
		
		try {
			IArapSupplierSynchService service = new IArapSupplierSynchServiceLocator();
			IArapSupplierSynchServicePortType port = (IArapSupplierSynchServicePortType)service.getIArapSupplierSynchServiceSOAP11port_http();
			System.out.println("Զ�̵���ִ�н����");
			//System.out.println(port.synchArapCustomer("{\"code\":\""+ArapBillImportUtils.getUUID()+"\",\"name\":\"�ڲ�����\",\"pk_org\":\"101\",\"op_type\":\"1\"}"));
			System.out.println(port.synchArapSupplier("{\"code\":\"-\",\"name\":\"test\",\"pk_org\":\"101\",\"op_type\":\"1\"}"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
