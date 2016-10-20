package nc.impl.arap.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.TransUtils;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.customer.IArapCustomerSynchService;
import nc.itf.bd.cust.baseinfo.ICustBaseInfoExtendService;
import nc.ui.bd.pub.extend.PrivateServiceContext;
import nc.vo.bd.countryzone.CountryZoneVO;
import nc.vo.bd.cust.CustCountrytaxesVO;
import nc.vo.bd.cust.CustbankVO;
import nc.vo.bd.cust.CustlinkmanVO;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;



public class ArapCustomerSynchServiceImpl implements IArapCustomerSynchService {
	
	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "2";
	private String defaultCountry = "CN";
	private String defaultCurrtype = "CNY";
	private String defaultCustclass = "02";

	private String[] dateFields = { 
			"操作类型", "公司", "编码", "名称", "外部来源系统" 
	};
	private String[] dateKeys = { 
			"op_type", "pk_org", "code", "name", "def1"
	};
	
	private String checkFieldExist(JSONObject bill) {
		StringBuilder errMsg = new StringBuilder();
		StringBuilder headErrMsg = new StringBuilder();
		for(int i=0; i<dateKeys.length; i++) {
			String key = dateKeys[i];
			if(!(bill.containsKey(key) && bill.get(key) instanceof String 
					&& StringUtils.isNotBlank(bill.getString(key)))){
				headErrMsg.append("[" + dateFields[i] + "]");
				continue;
			}
		}
		if(StringUtils.isNotBlank(headErrMsg.toString())) {
			errMsg.append("单据字段" + headErrMsg +"不存在或非法；");
		}
		return errMsg.toString();
	}
	
	@Override
	public String synchArapCustomer(String jsonStrCust) {
		JSONObject rt = new JSONObject();
		rt.put("result", "success");
		rt.put("message", "");
		try {
			Map<String, String> defaultParam = importUtils.getDefaultParam("cust");
			defaultGroup = defaultParam.get("default_group");
			defaultUser = defaultParam.get("default_user");
			defaultCustclass = defaultParam.get("default_custclass");
			
			JSONObject jsonCust = JSONObject.fromObject(jsonStrCust);
			String errMsg = this.checkFieldExist(jsonCust);
			if(StringUtils.isNotBlank(errMsg)) {
				throw new BusinessException(errMsg);
			}
			
			String pk_group = importUtils.getCurrGroupPk(defaultGroup);
			String pk_org = importUtils.getOrgByCode(pk_group, jsonCust.getString("pk_org")).getPrimaryKey();
			CountryZoneVO countryZone = importUtils.getCountryZoneFormatPkByCode(defaultCountry);
			String pk_user = importUtils.getInitUserPkByCode(defaultUser);
			InvocationInfoProxy.getInstance().setGroupId(pk_group);
			InvocationInfoProxy.getInstance().setUserId(pk_user);
			
			ICustBaseInfoExtendService service = NCLocator.getInstance().lookup(ICustBaseInfoExtendService.class);
			String opType = jsonCust.getString("op_type");
			String code = BillUtils.getCustomerCode(jsonCust.getString("def1"), jsonCust.getString("code"));
			if(StringUtils.equals(opType, "1")) {
				CustomerVO vo = new CustomerVO();
				vo.setCode(code);
				vo.setCustbanks(new CustbankVO[]{});
				vo.setCustcontacts(new CustlinkmanVO[]{});
				vo.setCustprop(0);
				vo.setCuststate(1);
				vo.setCusttaxtypes(new CustCountrytaxesVO[] {});
				vo.setDataoriginflag(0);
				vo.setDef1(jsonCust.getString("def1"));
				vo.setDef2(jsonCust.getString("code"));
				vo.setDr(0);
				vo.setEnablestate(2);
				vo.setExbeanname_tabvo_map(new HashMap<String, List<SuperVO>>());
				vo.setFrozenflag(UFBoolean.FALSE);
				vo.setIsfreecust(UFBoolean.FALSE);
				vo.setIsretailstore(UFBoolean.FALSE);
				vo.setIssupplier(UFBoolean.FALSE);
				vo.setIsvat(UFBoolean.FALSE);
				vo.setName(jsonCust.getString("name"));
				vo.setPk_country(countryZone.getPk_country());
				vo.setPk_currtype(importUtils.getCurrtypeByCode(defaultCurrtype).getPrimaryKey());
				vo.setPk_custclass(importUtils.getCustclassPkByCode(defaultCustclass));
				vo.setPk_format(countryZone.getPk_format());
				vo.setPk_group(pk_group);
				vo.setPk_org(pk_org);
				vo.setPk_timezone(countryZone.getPk_timezone());
				vo.setStatus(2);
				//保存时会自动补充
				//vo.setCreationtime(new UFDateTime());
				//vo.setCreator(pk_user);
				
				service.insertCustomerWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext(), false);
			} else if(StringUtils.equals(opType, "0")) {
				//删除
				CustomerVO vo = importUtils.getCustomerVOByCode(code, pk_group, pk_org);
				service.deleteCustomerWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext());
			} else {
				//修改
				CustomerVO vo = importUtils.getCustomerVOByCode(code, pk_group, pk_org);
				//vo.setCode(code);
				vo.setName(jsonCust.getString("name"));
				//vo.setDef1(jsonCust.getString("def1"));
				service.updateCustomerWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext(), false);
			}
			
			
		} catch (Exception e) {
			//返回处理结果
			rt.put("result", "fail");
			rt.put("message", e.getMessage());
		}
		return rt.toString();
	}

}
