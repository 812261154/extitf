package nc.impl.arap.supplier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.TransUtils;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.supplier.IArapSupplierSynchService;
import nc.itf.bd.supplier.baseinfo.ISupBaseInfoExtendService;
import nc.ui.bd.pub.extend.PrivateServiceContext;
import nc.vo.bd.countryzone.CountryZoneVO;
import nc.vo.bd.cust.CustbankVO;
import nc.vo.bd.supplier.SupCountryTaxesVO;
import nc.vo.bd.supplier.SupLinkmanVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapSupplierSynchServiceImpl implements IArapSupplierSynchService {
	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "2";
	private String defaultCountry = "CN";
	private String defaultCurrtype = "CNY";
	private String defaultSupplierclass = "2";
	
	private String[] dateFields = { 
			"操作类型", "公司", "编码", "名称" 
	};
	private String[] dateKeys = { 
			"op_type", "pk_org", "code", "name"
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
	public String synchArapSupplier(String jsonStrCust) {
		JSONObject rt = new JSONObject();
		rt.put("result", "success");
		rt.put("message", "");
		try {
			Map<String, String> defaultParam = importUtils.getDefaultParam("supplier");
			defaultGroup = defaultParam.get("default_group");
			defaultUser = defaultParam.get("default_user");
			defaultSupplierclass = defaultParam.get("default_supplierclass");
			
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
			
			ISupBaseInfoExtendService service = NCLocator.getInstance().lookup(ISupBaseInfoExtendService.class);
			String opType = jsonCust.getString("op_type");
			String code = BillUtils.getSupplierCode(jsonCust.getString("def1"), jsonCust.getString("code"));
			if(StringUtils.equals(opType, "1")) {
				SupplierVO vo = new SupplierVO();
				vo.setCode(code);
				vo.setDataoriginflag(0);
				vo.setDef1(jsonCust.getString("def1"));
				vo.setDef2(jsonCust.getString("code"));
				vo.setDr(0);
				vo.setEnablestate(2);
				vo.setExbeanname_tabvo_map(new HashMap<String, List<SuperVO>>());
				vo.setIscustomer(UFBoolean.FALSE);
				vo.setIsfreecust(UFBoolean.FALSE);
				vo.setIsoutcheck(UFBoolean.FALSE);
				vo.setIsvat(UFBoolean.FALSE);
				vo.setName(jsonCust.getString("name"));
				vo.setPk_country(countryZone.getPk_country());
				vo.setPk_currtype(importUtils.getCurrtypeByCode(defaultCurrtype).getPrimaryKey());
				vo.setPk_format(countryZone.getPk_format());
				vo.setPk_group(pk_group);
				vo.setPk_oldsupplier("~");
				vo.setPk_org(pk_org);
				vo.setPk_supplierclass(importUtils.getSupplierclassPkByCode(defaultSupplierclass));
				vo.setPk_timezone(countryZone.getPk_timezone());
				vo.setStatus(2);
				vo.setSupbankacc(new CustbankVO[]{});
				vo.setSupcountrytaxes(new SupCountryTaxesVO[]{});
				vo.setSuplinkman(new SupLinkmanVO[]{});
				vo.setSupprop(0);
				vo.setSupstate(1);
				vo.setCreationtime(new UFDateTime());
				vo.setCreator(pk_user);
				
				service.insertSupplierWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext(), false);
			} else if(StringUtils.equals(opType, "0")) {
				//删除
				SupplierVO vo = importUtils.getSupplierVOByCode(code, pk_group, pk_org);
				service.deleteSupplierWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext());
			} else {
				//修改
				SupplierVO vo = importUtils.getSupplierVOByCode(code, pk_group, pk_org);
				vo.setName(jsonCust.getString("name"));
				service.updateSupplierWithExtendVO(vo, new HashMap<String, Object>(), new PrivateServiceContext(), false);
			}
			
			
		} catch (Exception e) {
			rt.put("result", "fail");
			rt.put("message", e.getMessage());
		}
		return rt.toString();
	}

}
