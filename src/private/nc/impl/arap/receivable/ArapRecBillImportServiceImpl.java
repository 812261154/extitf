package nc.impl.arap.receivable;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.DateUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.TransUtils;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.receivable.IArapRecBillImportService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapRecBillImportServiceImpl implements IArapRecBillImportService {
	
	private String[] headDateFields = { 
			"��ϵͳ���ݺ�", "��������", "���ݻ���ڼ�", "���ݻ�����", "��֯���ҽ�ʵ��", "��֯���ҽ�����", 
			"ԭ�ҽ�ʵ��", "ԭ�ҽ�����", "����", "Ӧ�ղ�����֯", "ժҪ", "��ϵͳ", "��Ӧ��嵥�ݺ�", "�ֺŶ�Ӧ����", "�ֺ�" 
	};
	
	private String[] headDateKeys = { 
			"def1", "billdate", "billyear", "billperiod", "local_money", "def2", 
			"money", "def3", "pk_currtype", "pk_org", "scomment", "def4", "def5", "def6", "def7"
	};
	
	private String[] bodyDataFields = {
			"����", "��������", "�ͻ�", "����", "��֯���һ���", "�跽ԭ�ҽ�ʵ��", "�跽ԭ�ҽ�����", 
			"��֯���ҽ�ʵ��", "��֯���ҽ�����", "˰��", "��˰���"
	};
	
	private String[] bodyDataKeys = {
			"material", "objtype", "customer", "pk_currtype", "rate", "money_de", "def1", 
			"local_money_de", "def2", "taxrate", "taxtype"
	};
	
	@Override
	public String arRecBillImport(String jsonStrBill) {
		JSONObject rt = new JSONObject();
		rt.put("result", "success");
		rt.put("message", "");
		try {
			JSONArray billsArray = JSONArray.fromObject(jsonStrBill);
			StringBuffer errMsg = new StringBuffer();
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				errMsg.append(this.checkFieldExist(bill));
			}
			if(StringUtils.isBlank(errMsg.toString())) {
				FileUtils.serializeJSONArrayToFile(billsArray, RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar/");
			} else {
				rt.put("result", "fail");
				rt.put("message", "JSONArray�ַ�����ʽ����" + errMsg.toString());
				Logger.error("JSONArray�ַ�����ʽ����" + errMsg.toString());
			}
		} catch (Exception e1) {
			rt.put("result", "fail");
			rt.put("message", "JSONArray�ַ�����ʽ�����ʵ����������ʧ��:" + e1.getMessage());
			Logger.error("JSONArray�ַ�����ʽ�����ʵ����������ʧ��:", e1);
		}
		return rt.toString();
	}
	
	//У�鴫���JSON��ʽ�����ݱ�¼�ֶ��Ƿ񶼴���
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		if(!bill.containsKey("needarap")) {
			errMsg.append("���Ƿ�Ӧ������־�����ڣ�");
		}
		
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
				} else if(!(parent.containsKey(key) && parent.get(key) instanceof String 
						&& StringUtils.isNotEmpty(parent.getString(key)))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
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
					if(!(child.containsKey(key) && child.get(key) instanceof String 
							&& StringUtils.isNotEmpty(child.getString(key)))) {
						bodyChildErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
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
