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
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],总共接受数据" +billsArray.size()+ "条。");
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
					Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],单据" +BillUtils.getArapBillSourceNo(bill) + "接收失败，问题原因：" + checkRtMsg);
				}
			}
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "]," +billsArray.size()+ "条数据正在往本地缓存...");
			FileUtils.serializeJSONArrayToFile(billsArray, RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar/");
			Logger.info("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],缓存成功" +billsArray.size()+ "条数据。");
		} catch (Exception e) {
			Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],JSONArray字符串格式错误或实例化到本地失败,原因:", e);
			Logger.error("[SDPG][" + ArapRecBillImportServiceImpl.class.getName() + "],缓存失败的JSONArray字符串：" + jsonStrBills);
		}
		rt.put("LST_MESSAGE", rtArray);
		return rt.toString();
	}
	
	//校验传入的JSON格式的数据必录字段是否都存在
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		if(!bill.containsKey("needarap")) {
			errMsg.append("“是否传应付”标志不存在；");
		}
		
		ImportBillAggVO aggvo = this.getImportBillAggVO(BillUtils.getSourceSysFromJson(bill));
		
		//单据表头
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
				//业务来源系统校验
				if(isLegal && StringUtils.equals(key, "def4") && !BillUtils.isSourceSysLegal(parent.getString(key))) {
					isLegal = false;
				}
				if(!isLegal) {
					headErrMsg.append("[" + field.getName() + "]");
				}
			}
			if(StringUtils.isNotBlank(headErrMsg.toString())) {
				errMsg.append("单据表头字段" + headErrMsg +"不存在或非法；");
			}
		} else {
			errMsg.append("单据表头不存在或非法；");
		}
		
		//单据表体
		if(bill.containsKey("children") && bill.get("children") instanceof JSONArray && bill.getJSONArray("children").size() > 0) {
			JSONArray children = bill.getJSONArray("children");
			StringBuilder bodyErrMsg = new StringBuilder();
			for(int i=0; i<children.size(); i++) {
				if(!(children.get(i) instanceof JSONObject)) {
					bodyErrMsg.append("第" + (i+1) + "条表体行非法；");
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
					bodyErrMsg.append("第" + (i+1) + "条表体行中的字段" + bodyChildErrMsg +"不存在或非法；");
				}
			}
			if(StringUtils.isNotBlank(bodyErrMsg.toString())) {
				errMsg.append(bodyErrMsg);
			}
		} else {
			errMsg.append("单据表体不存在或非法；");
		}
		return errMsg.toString();
	}
	
	private ImportBillAggVO getImportBillAggVO(String type) {
		ImportBillAggVO aggvo = new ImportBillAggVO();
		
		//资产租赁单独使用一套规则
		if(StringUtils.equals(type, "ZCZL")) {
			aggvo.addHeadField(new ImportFeildVO("billdate", "单据日期", false, ImportFeildVO.DATETIME));
			aggvo.addHeadField(new ImportFeildVO("local_money", "组织本币金额", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("money", "原币金额", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("pk_currtype", "币种", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("pk_org", "应收财务组织", false, ImportFeildVO.VARCHAR));
//			aggvo.addHeadField(new ImportFeildVO("scomment", "摘要", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def1", "来源单据号", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def4", "来源业务系统", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def7", "仓号对应虚拟部门", false, ImportFeildVO.VARCHAR));
			
			aggvo.addBodyFields(new ImportFeildVO("objtype", "往来对象", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("customer", "客户", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_currtype", "币种", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("rate", "组织本币汇率", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("money_de", "借方原币金额", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("local_money_de", "组织本币金额", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxrate", "税率", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxtype", "扣税类别", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_subjcode", "收支项目", false, ImportFeildVO.VARCHAR));
//			aggvo.addBodyFields(new ImportFeildVO("def10", "结算单号", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("scomment", "摘要", true, ImportFeildVO.VARCHAR));
		
		} else {
			aggvo.addHeadField(new ImportFeildVO("billdate", "单据日期", false, ImportFeildVO.DATETIME));
			aggvo.addHeadField(new ImportFeildVO("local_money", "组织本币金额（实洋）", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("def2", "组织本币金额（码洋）", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("money", "原币金额（实洋）", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("def3", "原币金额（码洋）", false, ImportFeildVO.NUMBER));
			aggvo.addHeadField(new ImportFeildVO("pk_currtype", "币种", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("pk_org", "应收财务组织", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("scomment", "摘要", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def4", "来源业务系统", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def5", "对应红冲单据号", true, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def6", "仓号", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def7", "仓号对应虚拟部门", false, ImportFeildVO.VARCHAR));
			aggvo.addHeadField(new ImportFeildVO("def1", "来源单据号", false, ImportFeildVO.VARCHAR));
			
			aggvo.addBodyFields(new ImportFeildVO("material", "物料", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("objtype", "往来对象", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("customer", "客户", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("pk_currtype", "币种", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("rate", "组织本币汇率", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("money_de", "借方原币金额（实洋）", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("def1", "借方原币金额（码洋）", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("local_money_de", "组织本币金额（实洋）", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("def2", "组织本币金额（码洋）", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxrate", "税率", false, ImportFeildVO.NUMBER));
			aggvo.addBodyFields(new ImportFeildVO("taxtype", "扣税类别", false, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("def3", "客户对应仓号", true, ImportFeildVO.VARCHAR));
			aggvo.addBodyFields(new ImportFeildVO("def4", "仓号对应虚拟部门", true, ImportFeildVO.VARCHAR));
		}
		return aggvo;
	}
	
}
