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
			"外系统单据号", "单据日期", "单据会计期间", "单据会计年度", "组织本币金额（实洋）", "组织本币金额（码洋）", 
			"原币金额（实洋）", "原币金额（码洋）", "币种", "应付财务组织", "摘要", "外系统", "对应红冲单据号", "仓号" 
	};
	
	
	private String[] headDateKeys = { 
			"def1", "billdate", "billyear", "billperiod", "local_money", "def2", 
			"money", "def3", "pk_currtype", "pk_org", "scomment", "def4", "def5", "def6"
	};
	
	private String[] bodyDataFields = {
			"物料", "往来对象", "供应商", "币种", "组织本币汇率", "贷方原币金额（实洋）", "贷方原币金额（码洋）", 
			"组织本币金额（实洋）", "组织本币金额（码洋）", "税率", "扣税类别", "供应商对应仓号", "供应商发货单号"
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
				rt.put("message", "JSONArray字符串格式错误：" + errMsg.toString());
				Logger.error("JSONArray字符串格式错误：" + errMsg.toString());
			}
			
		} catch (Exception e) {
			rt.put("result", "fail");
			rt.put("message", "JSONArray字符串格式错误或实例化到本地失败:" + e.getMessage());
			Logger.error("JSONArray字符串格式错误或实例化到本地失败:", e);
		}
		return rt.toString();
	}
	//校验传入的JSON格式的数据必录字段是否都存在
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		//单据表头
		if(bill.containsKey("parent") && bill.get("parent") instanceof JSONObject) {
			JSONObject parent = bill.getJSONObject("parent");
			StringBuilder headErrMsg = new StringBuilder();
			for(int i=0; i<headDateKeys.length; i++) {
				String key = headDateKeys[i];
				//对应红冲单据号可为空
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
				//单据日期必须是日期型字符串
				if(StringUtils.equals(key, "billdate") && !DateUtils.isFormatDateString(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
				//金额必须是数字型字符串
				if((StringUtils.equals(key, "local_money") || StringUtils.equals(key, "def2") || StringUtils.equals(key, "money") || StringUtils.equals(key, "def3"))
						&& !BillUtils.isNumberic(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
				//外部来源系统校验
				if((StringUtils.equals(key, "def4"))
						&& !BillUtils.isSysNameLegal(parent.getString(key))) {
					headErrMsg.append("[" + headDateFields[i] + "]");
					continue;
				}
			}
			if(StringUtils.isNotBlank(headErrMsg.toString())) {
				errMsg.append("单据表头字段" + headErrMsg +"不存在或非法；");
			}
		} else {
			errMsg.append("单据表头不存在或非法；");
		}
		
		//单据表体
		if(bill.containsKey("children") && bill.get("children") instanceof JSONArray) {
			JSONArray children = bill.getJSONArray("children");
			StringBuilder bodyErrMsg = new StringBuilder();
			for(int i=0; i<children.size(); i++) {
				if(!(children.get(i) instanceof JSONObject)) {
					bodyErrMsg.append("第" + (i+1) + "条表体行非法；");
					continue;
				}
				JSONObject child = children.getJSONObject(i);
				StringBuilder bodyChildErrMsg = new StringBuilder();
				for(int j=0; j<bodyDataKeys.length; j++) {
					String key = bodyDataKeys[j];
					//供应商对应仓号、供应商发货单号可为空
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
					//金额必须是数字型字符串
					if((StringUtils.equals(key, "money_de") || StringUtils.equals(key, "def1") || StringUtils.equals(key, "local_money_de") 
							|| StringUtils.equals(key, "def2") || StringUtils.equals(key, "rate") || StringUtils.equals(key, "taxrate"))
							&& !BillUtils.isNumberic(child.getString(key))) {
						bodyChildErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
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
		if(StringUtils.isNotBlank(errMsg.toString())) {
			errMsg.insert(0, "单据" + TransUtils.getExBillNo(bill) + "格式错误：");
		}
		return errMsg.toString();
	}
	
}
