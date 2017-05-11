package nc.impl.arap.receivable;

import java.util.List;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.parm.ImportBillAggVO;
import nc.itf.arap.parm.ImportFeildVO;
import nc.itf.arap.receivable.IArapRecBillImportService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapRecBillImportServiceImpl implements IArapRecBillImportService {
	
	@Override
	public String arRecBillImport(String jsonStrBills) {
		JSONObject rt = new JSONObject();
		JSONArray rtArray = new JSONArray();
		try {
			JSONArray billsArray = JSONArray.fromObject(jsonStrBills);
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],�ܹ���������" +billsArray.size()+ "����");
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				String checkRtMsg = this.checkFieldExist(bill);
				if(StringUtils.isNotBlank(checkRtMsg)) {
					billsArray.remove(i);
					JSONObject rtJson = new JSONObject();
					rtJson.put("FROMSYS", BillUtils.getArapBillSourceSys(bill));
					rtJson.put("NUMBER", BillUtils.getArapBillSourceNo(bill));
					rtJson.put("COMPLETED", "0");
					rtJson.put("MESSAGE", checkRtMsg);
					rtArray.add(rtJson);
					Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],����" +BillUtils.getArapBillSourceNo(bill) + "����ʧ�ܣ�����ԭ��" + checkRtMsg);
				}
			}
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "]," +billsArray.size()+ "���������������ػ���...");
			FileUtils.serializeJSONArrayToFile(billsArray, RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar/");
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],����ɹ�" +billsArray.size()+ "�����ݡ�");
		} catch (Exception e) {
			Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],JSONArray�ַ�����ʽ�����ʵ����������ʧ��,ԭ��:", e);
			Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],����ʧ�ܵ�JSONArray�ַ�����" + jsonStrBills);
		}
		rt.put("LST_MESSAGE", rtArray);
		return rt.toString();
	}
	
	//У�鴫���JSON��ʽ�����ݱ�¼�ֶ��Ƿ񶼴���
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		if(!bill.containsKey("needarap")) {
			errMsg.append("���Ƿ�Ӧ������־�����ڣ�");
		}
		
		ImportBillAggVO aggvo = this.getImportBillAggVO(BillUtils.getSourceSysFromJson(bill));
		
		//���ݱ�ͷ
		if(bill.containsKey("parent") && bill.get("parent") instanceof JSONObject) {
			JSONObject parent = bill.getJSONObject("parent");
			StringBuilder headErrMsg = new StringBuilder();
			List<ImportFeildVO> headFields = aggvo.getHeadFields();
			for(ImportFeildVO field : headFields) {
				boolean isLegal = true;
				String key = field.getCode();
				if(parent.containsKey(key) && parent.get(key) instanceof String) {
					isLegal = BillUtils.isFieldLegal(field, parent.getString(key));
				} else {
					isLegal = false;
				}
				//ҵ����ԴϵͳУ��
				if(isLegal && StringUtils.equals(key, "def4") && !BillUtils.isSourceSysLegal(parent.getString(key))) {
					isLegal = false;
				}
				if(!isLegal) {
					headErrMsg.append("[" + field.getName() + "]");
				}
			}
			if(StringUtils.isNotBlank(headErrMsg.toString())) {
				errMsg.append("���ݱ�ͷ�ֶ�" + headErrMsg +"�����ڻ�Ƿ���");
			}
		} else {
			errMsg.append("���ݱ�ͷ�����ڻ�Ƿ���");
		}
		
		//���ݱ���
		if(bill.containsKey("children") && bill.get("children") instanceof JSONArray && bill.getJSONArray("children").size() > 0) {
			JSONArray children = bill.getJSONArray("children");
			StringBuilder bodyErrMsg = new StringBuilder();
			for(int i=0; i<children.size(); i++) {
				if(!(children.get(i) instanceof JSONObject)) {
					bodyErrMsg.append("��" + (i+1) + "�������зǷ���");
					continue;
				}
				JSONObject child = children.getJSONObject(i);
				StringBuilder bodyChildErrMsg = new StringBuilder();
				List<ImportFeildVO> bodyFields = aggvo.getBodyFields();
				for(ImportFeildVO field : bodyFields) {
					boolean isLegal = true;
					String key = field.getCode();
					if(child.containsKey(key) && child.get(key) instanceof String) {
						isLegal = BillUtils.isFieldLegal(field, child.getString(key));
					} else {
						isLegal = false;
					}
					if(!isLegal) {
						bodyChildErrMsg.append("[" + field.getName() + "]");
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
		return errMsg.toString();
	}
	
	private ImportBillAggVO getImportBillAggVO(String type) {
		ImportBillAggVO aggvo = new ImportBillAggVO();
		
		//�ʲ����޵���ʹ��һ�׹���
		if(StringUtils.equals(type, "ZCZL")) {
			aggvo.addHeadField(new ImportFeildVO("billdate", "��������", false, ImportFeildVO.DATETIME));
			aggvo.addHeadField(new ImportFeildVO("local_money", "��֯���ҽ��", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("money", "ԭ�ҽ��", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("pk_currtype", "����", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("pk_org", "Ӧ�ղ�����֯", false, ImportFeildVO.VARCHAR));
//			aggvo.addHeadField(new ImportFeildVO("scomment", "ժҪ", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def1", "��Դ���ݺ�", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def4", "��Դҵ��ϵͳ", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def7", "�ֺŶ�Ӧ���ⲿ��", false, ImportFeildVO.VARCHAR));
			
			aggvo.addBodyFields(new ImportFeildVO("objtype", "��������", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("customer", "�ͻ�", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_currtype", "����", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("rate", "��֯���һ���", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("money_de", "�跽ԭ�ҽ��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("local_money_de", "��֯���ҽ��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxrate", "˰��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxtype", "��˰���", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_subjcode", "��֧��Ŀ", false, ImportFeildVO.VARCHAR));
//			aggvo.addBodyFields(new ImportFeildVO("def10", "���㵥��", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("scomment", "ժҪ", true, ImportFeildVO.VARCHAR));
		
		} else {
			aggvo.addHeadField(new ImportFeildVO("billdate", "��������", false, ImportFeildVO.DATETIME));
			aggvo.addHeadField(new ImportFeildVO("local_money", "��֯���ҽ�ʵ��", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("def2", "��֯���ҽ�����", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("money", "ԭ�ҽ�ʵ��", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("def3", "ԭ�ҽ�����", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("pk_currtype", "����", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("pk_org", "Ӧ�ղ�����֯", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("scomment", "ժҪ", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def4", "��Դҵ��ϵͳ", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def5", "��Ӧ��嵥�ݺ�", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def6", "�ֺ�", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def7", "�ֺŶ�Ӧ���ⲿ��", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def1", "��Դ���ݺ�", false, ImportFeildVO.VARCHAR));
			
			aggvo.addBodyFields(new ImportFeildVO("material", "����", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("objtype", "��������", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("customer", "�ͻ�", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_currtype", "����", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("rate", "��֯���һ���", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("money_de", "�跽ԭ�ҽ�ʵ��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("def1", "�跽ԭ�ҽ�����", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("local_money_de", "��֯���ҽ�ʵ��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("def2", "��֯���ҽ�����", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxrate", "˰��", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxtype", "��˰���", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("def3", "�ͻ���Ӧ�ֺ�", true, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("def4", "�ֺŶ�Ӧ���ⲿ��", true, ImportFeildVO.VARCHAR));
		}
		return aggvo;
	}
	
}
