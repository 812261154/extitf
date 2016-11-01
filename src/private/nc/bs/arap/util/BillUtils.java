package nc.bs.arap.util;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class BillUtils {
	
	//������Դϵͳ���ͻ������ǰ׺
	public static String getCustomerCode(String sysName, String code) {
		if(StringUtils.isBlank(code)) {
			return "";
		}
		if(StringUtils.isBlank(sysName)) {
			return code;
		}
		if(StringUtils.equalsIgnoreCase(sysName, "PMP")) {
			return "PMPC_" + code;
		} else if(StringUtils.equalsIgnoreCase(sysName, "PMPYW")) {
			return "YWC_" + code;
		} else if(StringUtils.equalsIgnoreCase(sysName, "PMPTG")) {
			return "TGC_" + code;
		} else {
			return code;
		}
	}
	
	//������Դϵͳ����Ӧ�̱����ǰ׺
	public static String getSupplierCode(String sysName, String code) {
		if(StringUtils.isBlank(code)) {
			return "";
		}
		if(StringUtils.isBlank(sysName)) {
			return code;
		}
		if(StringUtils.equalsIgnoreCase(sysName, "PMP")) {
			return "PMPS_" + code;
		} else if(StringUtils.equalsIgnoreCase(sysName, "PMPYW")) {
			return "YWS_" + code;
		} else if(StringUtils.equalsIgnoreCase(sysName, "PMPTG")) {
			return "TGS_" + code;
		} else {
			return code;
		}
	}
	
	//��ȡΨһID
	public static String getUUID(){ 
		String s = UUID.randomUUID().toString(); 
		//ȥ��"-"���� 
		return s.replace("-", "");
	}
	
	//�ⲿϵͳ�����Ƿ�Ϸ�
	public static boolean isSysNameLegal(String name) {
		return (StringUtils.equalsIgnoreCase(name, "PMP")
				||StringUtils.equalsIgnoreCase(name, "PMPYW")
				||StringUtils.equalsIgnoreCase(name, "PMPTG"));
	}
	
	/** 
     * ����"XXXXXXXXXXXX"��ʽ����ȡ����MAC��ַ 
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
						//��11110000����λ�������Ա��ȡ��ǰ�ֽڸ�4λ  
						sb.append(Integer.toHexString((b&240)>>4));  
						//��00001111����λ�������Ա��ȡ��ǰ�ֽڵ�4λ  
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
    
    //�ж��ַ����Ƿ�������
  	public static boolean isNumberic(String str) {
  	   try {   
  	    Double.parseDouble(str);
  	    return true;
  	   } catch (NumberFormatException e) {
  	    return false;
  	   }
  	}
  	
  	//ȡӦ��Ӧ���������ϵͳ���ݺ�
  	public static String getArapBillSourceNo(JSONObject  bill) {
  		try {
  			return bill.getJSONObject("parent").getString("def1");
  		} catch(Exception e) {
  			return "[δ֪���ݺ�]";
  		}
  	}
  	
  	//ȡӦ��Ӧ���������Դϵͳ
  	public static String getArapBillSourceSys(JSONObject  bill) {
  		try {
  			return bill.getJSONObject("parent").getString("def4");
  		} catch(Exception e) {
  			return "[δ֪��Դϵͳ]";
  		}
  	}
  	
  	//ȡƾ֤��ĵ��ݺ�
  	public static String getVoucherSourceNo(JSONObject  voucher) {
  		try {
  			return voucher.getString("free4");
  		} catch(Exception e) {
  			return "[δ֪��Դ���ݺ�]";
  		}
  	}
  	
  	//ȡƾ֤�����Դϵͳ
  	public static String getVoucherSourceSys (JSONObject  voucher) {
  		try {
  			return voucher.getString("free3");
  		} catch(Exception e) {
  			return "[δ֪��Դϵͳ]";
  		}
  	}
  	
}
