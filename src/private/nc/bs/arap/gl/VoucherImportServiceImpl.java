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
			"凭证日期", "财务组织", "凭证类型", "来源系统", "来源系统单据号"
	};
	private String[] headDateKeys = { 
			"prepareddate", "org", "vouchertype", "free3", "free4"
	};
	
	private String[] bodyDataFields = {
			"分录摘要", "会计科目", "币种", "借贷方向", "金额", "辅助核算"
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
	private String checkFieldExist(JSONObject voucher) {
		StringBuilder errMsg = new StringBuilder();
		//凭证主题
		StringBuilder headErrMsg = new StringBuilder();
		for(int i=0; i<headDateKeys.length; i++) {
			String key = headDateKeys[i];
			if(!(voucher.containsKey(key) && voucher.get(key) instanceof String 
					&& StringUtils.isNotBlank(voucher.getString(key)))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
			//单据日期必须是日期型字符串
			if(StringUtils.equals(key, "prepareddate") && !DateUtils.isFormatDateString(voucher.getString(key))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
			//外部来源系统校验
			if((StringUtils.equals(key, "free3"))
					&& !BillUtils.isSysNameLegal(voucher.getString(key))) {
				headErrMsg.append("[" + headDateFields[i] + "]");
				continue;
			}
		}
		if(StringUtils.isNotBlank(headErrMsg.toString())) {
			errMsg.append("单据表头字段" + headErrMsg +"不存在或非法；");
		}
		
		//凭证分录
		if(voucher.containsKey("details") && voucher.get("details") instanceof JSONArray) {
			JSONArray details = voucher.getJSONArray("details");
			StringBuilder detailErrMsg = new StringBuilder();
			for(int i=0; i<details.size(); i++) {
				if(!(details.get(i) instanceof JSONObject)) {
					detailErrMsg.append("第" + (i+1) + "条表体行非法；");
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
					//金额必须是数字型字符串
					if((StringUtils.equals(key, "amount"))
							&& !BillUtils.isNumberic(child.getString(key))) {
						detailBodyErrMsg.append("[" + bodyDataFields[j] + "]");
						continue;
					}
				}
				if(StringUtils.isNotBlank(detailBodyErrMsg.toString())) {
					detailErrMsg.append("第" + (i+1) + "分录中的字段" + detailBodyErrMsg +"不存在或非法；");
				}
			}
			if(StringUtils.isNotBlank(detailErrMsg.toString())) {
				errMsg.append(detailErrMsg);
			}
		} else {
			errMsg.append("分录不存在或非法；");
		}
		if(StringUtils.isNotBlank(errMsg.toString())) {
			errMsg.insert(0, "单据[" + TransUtils.getVoucherBillNo(voucher) + "]对应的凭证格式错误;");
		}
		return errMsg.toString();
	}
	
}
