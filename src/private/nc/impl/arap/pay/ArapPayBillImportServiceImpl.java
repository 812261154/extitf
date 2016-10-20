package nc.impl.arap.pay;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.DateUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.TransUtils;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.payable.IArapPayBillImportService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapPayBillImportServiceImpl implements IArapPayBillImportService {

	
	private String[] headDateFields = { 
			"��ϵͳ���ݺ�", "��������", "���ݻ���ڼ�", "���ݻ�����", "��֯���ҽ�ʵ��", "��֯���ҽ�����", 
			"ԭ�ҽ�ʵ��", "ԭ�ҽ�����", "����", "Ӧ��������֯", "ժҪ", "��ϵͳ", "��Ӧ��嵥�ݺ�", "�ֺ�" 
	};
	
	
	private String[] headDateKeys = { 
			"def1", "billdate", "billyear", "billperiod", "local_money", "def2", 
			"money", "def3", "pk_currtype", "pk_org", "scomment", "def4", "def5", "def6"
	};
	
	private String[] bodyDataFields = {
			"����", "��������", "��Ӧ��", "����", "��֯���һ���", "����ԭ�ҽ�ʵ��", "����ԭ�ҽ�����", 
			"��֯���ҽ�ʵ��", "��֯���ҽ�����", "˰��", "��˰���", "��Ӧ�̶�Ӧ�ֺ�", "��Ӧ�̷�������"
	};
	private String[] bodyDataKeys = {
			"material", "objtype", "supplier", "pk_currtype", "rate", "money_cr", "def1", 
			"local_money_cr", "def2", "taxrate", "taxtype", "def3", "def4"
	};
	
	
	@Override
	public String apPayBillImport(String jsonStrBills) {
		JSONObject rt = new JSONObject();
		rt.put("result", "success");
		rt.put("message", "");
		try {
			StringBuffer errMsg = new StringBuffer();
			JSONArray billsArray = JSONArray.fromObject(jsonStrBills);
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				errMsg.append(this.checkFieldExist(bill));
			}
			if(StringUtils.isBlank(errMsg.toString())) {
				FileUtils.serializeJSONArrayToFile(billsArray, RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ap/");
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
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		//���ݱ�ͷ
		if(bill.containsKey("parent") && bill.get("parent") instanceof JSONObject) {
			JSONObject parent = bill.getJSONObject("parent");
			StringBuilder headErrMsg = new StringBuilder();
			for(int i=0; i<headDateKeys.length; i++) {
				String key = headDateKeys[i];
				//��Ӧ��嵥�ݺſ�Ϊ��
				if(StringUtils.equals(key, "def5") || StringUtils.equals(key, "scomment")) {
					if(!(parent.containsKey(key) && parent.get(key) instanceof String)) {
						headErrMsg.append("[" + headDateFields[i] + "]");
						continue;
					}
				} else {
					if(!(parent.containsKey(key) && parent.get(key) instanceof String 
							&& StringUtils.isNotEmpty(parent.getString(key)))) {
						headErrMsg.append("[" + headDateFields[i] + "]");
						continue;
					}
				}
				//�������ڱ������������ַ���
				if(StringUtils.equals(key, "billdate") && !DateUtils.isFormatDateString(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
				//���������������ַ���
				if((StringUtils.equals(key, "local_money") || StringUtils.equals(key, "def2") || StringUtils.equals(key, "money") || StringUtils.equals(key, "def3"))
						&& !BillUtils.isNumberic(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
				//�ⲿ��ԴϵͳУ��
				if((StringUtils.equals(key, "def4"))
						&& !BillUtils.isSysNameLegal(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
			}
			if(StringUtils.isNotBlank(headErrMsg.toString())) {
				errMsg.append("���ݱ�ͷ�ֶ�" + headErrMsg +"�����ڻ�Ƿ���");
			}
		} else {
			errMsg.append("���ݱ�ͷ�����ڻ�Ƿ���");
		}
		
		//���ݱ���
		if(bill.containsKey("children") && bill.get("children") instanceof JSONArray) {
			JSONArray children = bill.getJSONArray("children");
			StringBuilder bodyErrMsg = new StringBuilder();
			for(int i=0; i<children.size(); i++) {
				if(!(children.get(i) instanceof JSONObject)) {
					bodyErrMsg.append("��" + (i+1) + "�������зǷ���");
					continue;
				}
				JSONObject child = children.getJSONObject(i);
				StringBuilder bodyChildErrMsg = new StringBuilder();
				for(int j=0; j<bodyDataKeys.length; j++) {
					String key = bodyDataKeys[j];
					//��Ӧ�̶�Ӧ�ֺš���Ӧ�̷������ſ�Ϊ��
					if(StringUtils.equals(key, "def3") || StringUtils.equals(key, "def4")) {
						if(!(child.containsKey(key) && child.get(key) instanceof String)) {
							bodyChildErrMsg.append("[" + bodyDataFields[j] + "]");
							continue;
						}
					} else {
						if(!(child.containsKey(key) && child.get(key) instanceof String 
								&& StringUtils.isNotEmpty(child.getString(key)))) {
							bodyChildErrMsg.append("[" + bodyDataFields[j] + "]");
							continue;
						}
					} 
					//���������������ַ���
					if((StringUtils.equals(key, "money_de") || StringUtils.equals(key, "def1") || StringUtils.equals(key, "local_money_de") 
							|| StringUtils.equals(key, "def2") || StringUtils.equals(key, "rate") || StringUtils.equals(key, "taxrate"))
							&& !BillUtils.isNumberic(child.getString(key))) {
						bodyChildErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
					}
				}
				if(StringUtils.isNotBlank(bodyChildErrMsg.toString())) {
					bodyErrMsg.append("��" + (i+1) + "���������е��ֶ�" + bodyChildErrMsg +"�����ڻ�Ƿ���");
				}
			}
			if(StringUtils.isNotBlank(bodyErrMsg.toString())) {
				errMsg.append(bodyErrMsg);
			}
		} else {
			errMsg.append("���ݱ��岻���ڻ�Ƿ���");
		}
		if(StringUtils.isNotBlank(errMsg.toString())) {
			errMsg.insert(0, "����" + TransUtils.getExBillNo(bill) + "��ʽ����");
		}
		return errMsg.toString();
	}
	
}
