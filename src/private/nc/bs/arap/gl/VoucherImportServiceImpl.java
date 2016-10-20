package nc.bs.arap.gl;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.DateUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.TransUtils;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.gl.IVoucherImportService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class VoucherImportServiceImpl implements IVoucherImportService {

	private String[] headDateFields = { 
			"ƾ֤����", "������֯", "ƾ֤����", "��Դϵͳ", "��Դϵͳ���ݺ�"
	};
	private String[] headDateKeys = { 
			"prepareddate", "org", "vouchertype", "free3", "free4"
	};
	
	private String[] bodyDataFields = {
			"��¼ժҪ", "��ƿ�Ŀ", "����", "�������", "���", "��������"
	};
	private String[] bodyDataKeys = {
			"explanation", "accsubj", "currtype", "direction", "amount", "ass"
	};
	
	
	@Override
	public String voucherImport(String jsonVouchers) {
		
		JSONObject rt = new JSONObject();
		rt.put("result", "success");
		rt.put("message", "");
		try {
			JSONArray billsArray = JSONArray.fromObject(jsonVouchers);
			StringBuffer errMsg = new StringBuffer();
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				errMsg.append(this.checkFieldExist(bill));
			}
			if(StringUtils.isBlank(errMsg.toString())) {
				FileUtils.serializeJSONArrayToFile(billsArray, RuntimeEnv.getNCHome() + "/modules/arap/outterdata/gl/");
			} else {
				rt.put("result", "fail");
				rt.put("message", "JSONArray�ַ�����ʽ����" + errMsg.toString());
				Logger.error("JSONArray�ַ�����ʽ����" + errMsg.toString());
			}
		} catch (Exception e) {
			rt.put("result", "fail");
			rt.put("message", "JSONArray�ַ�����ʽ�����ʵ����������ʧ��:" + e.getMessage());
			Logger.error("JSONArray�ַ�����ʽ�����ʵ����������ʧ��:", e);
		}
		return rt.toString();
	}
	
	//У�鴫���JSON��ʽ�����ݱ�¼�ֶ��Ƿ񶼴���
	private String checkFieldExist(JSONObject voucher) {
		StringBuilder errMsg = new StringBuilder();
		//ƾ֤����
		StringBuilder headErrMsg = new StringBuilder();
		for(int i=0; i<headDateKeys.length; i++) {
			String key = headDateKeys[i];
			if(!(voucher.containsKey(key) && voucher.get(key) instanceof String 
					&& StringUtils.isNotBlank(voucher.getString(key)))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
			//�������ڱ������������ַ���
			if(StringUtils.equals(key, "prepareddate") && !DateUtils.isFormatDateString(voucher.getString(key))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
			//�ⲿ��ԴϵͳУ��
			if((StringUtils.equals(key, "free3"))
					&& !BillUtils.isSysNameLegal(voucher.getString(key))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
		}
		if(StringUtils.isNotBlank(headErrMsg.toString())) {
			errMsg.append("���ݱ�ͷ�ֶ�" + headErrMsg +"�����ڻ�Ƿ���");
		}
		
		//ƾ֤��¼
		if(voucher.containsKey("details") && voucher.get("details") instanceof JSONArray) {
			JSONArray details = voucher.getJSONArray("details");
			StringBuilder detailErrMsg = new StringBuilder();
			for(int i=0; i<details.size(); i++) {
				if(!(details.get(i) instanceof JSONObject)) {
					detailErrMsg.append("��" + (i+1) + "�������зǷ���");
					continue;
				}
				JSONObject child = details.getJSONObject(i);
				StringBuilder detailBodyErrMsg = new StringBuilder();
				for(int j=0; j<bodyDataKeys.length; j++) {
					String key = bodyDataKeys[j];
					if(StringUtils.equals(key, "ass")) {
						if(!(child.containsKey(key) && child.get(key) instanceof JSONObject)) {
							detailBodyErrMsg.append("[" + bodyDataFields[j] + "]");
							continue;
						}
					} else if(!(child.containsKey(key) && child.get(key) instanceof String 
							&& StringUtils.isNotEmpty(child.getString(key)))) {
						detailBodyErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
					}
					//���������������ַ���
					if((StringUtils.equals(key, "amount"))
							&& !BillUtils.isNumberic(child.getString(key))) {
						detailBodyErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
					}
				}
				if(StringUtils.isNotBlank(detailBodyErrMsg.toString())) {
					detailErrMsg.append("��" + (i+1) + "��¼�е��ֶ�" + detailBodyErrMsg +"�����ڻ�Ƿ���");
				}
			}
			if(StringUtils.isNotBlank(detailErrMsg.toString())) {
				errMsg.append(detailErrMsg);
			}
		} else {
			errMsg.append("��¼�����ڻ�Ƿ���");
		}
		if(StringUtils.isNotBlank(errMsg.toString())) {
			errMsg.insert(0, "����[" + TransUtils.getVoucherBillNo(voucher) + "]��Ӧ��ƾ֤��ʽ����;");
		}
		return errMsg.toString();
	}
	
}
