package nc.bs.arap.util;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

import nc.bs.pub.DataManageObject;

import org.apache.commons.lang.StringUtils;

public class BillUtils {
	
	//根据来源系统给客户编码加前缀
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
	
	//根据来源系统给供应商编码加前缀
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
	
	//获取唯一ID
	public static String getUUID(){ 
		String s = UUID.randomUUID().toString(); 
		//去掉"-"符号 
		return s.replace("-", "");
	}
	
	//外部系统编码是否合法
	public static boolean isSysNameLegal(String name) {
		return (StringUtils.equalsIgnoreCase(name, "PMP")
				||StringUtils.equalsIgnoreCase(name, "PMPYW")
				||StringUtils.equalsIgnoreCase(name, "PMPTG"));
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
    
    //判断字符串是否是数字
  	public static boolean isNumberic(String str) {
  	   try {   
  	    Double.parseDouble(str);
  	    return true;
  	   } catch (NumberFormatException e) {
  	    return false;
  	   }
  	}
  	
}
