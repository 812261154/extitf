package nc.impl.arap.pay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.arap.util.TransUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.HttpUtils;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.payable.IInsertPayableBill;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.arap.payable.AggPayableBillVO;
import nc.vo.arap.payable.PayableBillItemVO;
import nc.vo.arap.payable.PayableBillVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

public class ArapPayBillImportBO {
	
	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "3";
	private String defaultTradeType = "D1";
	private String defaultBillType = "F1";
	
	public void payBillImport() throws BusinessException {
		Map<String, String> defaultParam = importUtils.getDefaultParam("ap");
		defaultGroup = defaultParam.get("default_group");
		defaultUser = defaultParam.get("default_user");
		defaultTradeType = defaultParam.get("default_tradetype");
		defaultBillType = defaultParam.get("default_billtype");

		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ap");
		AggPayableBillVO[] vos = arrayListToVos(list);
		
		//导入结果
		JSONArray rtArray = new JSONArray();
		String outterBillno = "";
		IInsertPayableBill service = (IInsertPayableBill) NCLocator.getInstance().lookup(IInsertPayableBill.class.getName());
		for(AggPayableBillVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			outterBillno = (String) vo.getParentVO().getAttributeValue("def1");
			rtJson.put("FROMSYS", (String) vo.getParentVO().getAttributeValue("def4"));
			rtJson.put("NUMBER", outterBillno);
			try {
				if(isBillExit(vo)) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "该单据已存在，不可重复录入!");
				} else {
					String rt = service.insertPayableBill_RequiresNew(vo);
					if(StringUtils.isBlank(rt)) {
						rtJson.put("COMPLETED", "1");
						rtJson.put("MESSAGE", "");
					} else {
						rtJson.put("COMPLETED", "0");
						rtJson.put("MESSAGE", rt);
					}
				}
			} catch(Exception e3) {
				rtJson.put("COMPLETED", "0");
				rtJson.put("MESSAGE", e3.getMessage());
			}
			rtArray.add(rtJson);
		}
		importUtils.insertImportResult(rtArray);
		rtArray.clear();
		try {
			rtArray = TransUtils.queryImportResult();
			if(rtArray.size() > 0) {
				String rt = HttpUtils.httpPostWithJSON(rtArray);
				if(rt.trim().startsWith("FAIL")) {
					importUtils.insertImportResult(rtArray);
				}
			}
		} catch (Exception e) {
			importUtils.insertImportResult(rtArray);
		}
	}
	
	//单据保存前查重
	private boolean isBillExit(AggPayableBillVO vo) throws BusinessException {
		String sql = "select count(*) from AP_PAYABLEBILL t where t.dr=0 and t.def1 = '" +vo.getParentVO().getAttributeValue("def1")+ "' and t.def4 = '" +vo.getParentVO().getAttributeValue("def4")+ "'";
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer)baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (DAOException e) {
			throw new BusinessException("单据是否重复验证时出错！");
		}
		return count > 0;
	}
	
	private AggPayableBillVO[] arrayListToVos(List<JSONArray> arrayList) {
		List<AggPayableBillVO> voList = new ArrayList<AggPayableBillVO>();
		JSONArray rtArray = new JSONArray();
		for(JSONArray billsArray : arrayList) {
			//存放翻译成功的单据
			String outterBillno = "";
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = TransUtils.getExBillNo(bill);
				try {
					AggPayableBillVO vo = jsonToVO(bill);
					voList.add(vo);
				} catch(Exception e3) {
					/************************************************************************/
					/**************************调用PMP接口返回********************************/
					/************************************************************************/
					JSONObject rtJson = new JSONObject();
					rtJson.put("FROMSYS", bill.getJSONObject("parent").getString("def4"));
					rtJson.put("NUMBER", bill.getJSONObject("parent").getString("def1"));
					rtJson.put("COMPLETED", "0");
					rtJson.put("MESSAGE", e3.getMessage());
					rtArray.add(rtJson);
					Logger.error("单据[" +outterBillno+ "]导入出错，问题原因：" + e3.getMessage() + "\n");
				}
			}
		}
		if(rtArray.size() > 0) {
			importUtils.insertImportResult(rtArray);
		}
		return (AggPayableBillVO[])voList.toArray(new AggPayableBillVO[0]);
	}
	
	//将json格式的参数转换成vo
	private AggPayableBillVO jsonToVO(JSONObject bill) throws BusinessException {
		
		AggPayableBillVO aggPayableBillVO = new AggPayableBillVO();
		PayableBillVO parentVO = new PayableBillVO();
		
		/**  --表头--  **/
		JSONObject parent = bill.getJSONObject("parent");
		UFDate billdate = new UFDate(parent.getString("billdate"));
		String pk_group = importUtils.getCurrGroupPk(defaultGroup);
		String pk_org = importUtils.getOrgByCode(pk_group, parent.getString("pk_org")).getPrimaryKey();
		String pk_user = importUtils.getInitUserPkByCode(defaultUser);
		String pk_tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, pk_group);
		String pk_currtype = importUtils.getCurrtypeByCode(parent.getString("pk_currtype")).getPrimaryKey();
		//收货国
		String rececountryid = importUtils.getCountryId(pk_org);
		String scomment = parent.getString("scomment");
		String outterSysNum = parent.getString("def1");
		String outterSys = parent.getString("def4");
		String supplier = "";
		UFDouble rate = null;
		
		
		parentVO.setApprovestatus(3);	//审批状态:1=通过态,3=提交态
		parentVO.setBillstatus(-1);		//单据状态:1=审批通过，-1=保存
		parentVO.setEffectstatus(0);  	//生效状态：10=已生效，0=未生效
		
		//外系统单据号
		parentVO.setDef1(outterSysNum);
		parentVO.setBillclass("yf");
		parentVO.setBilldate(billdate);
		parentVO.setBillmaker(pk_user);
		parentVO.setCreationtime(new UFDateTime());
		parentVO.setCreator(pk_user);
		parentVO.setGloballocal(UFDouble.ZERO_DBL);
		parentVO.setGlobalnotax(UFDouble.ZERO_DBL);
		parentVO.setGlobalrate(UFDouble.ZERO_DBL);
		parentVO.setGlobaltax(UFDouble.ZERO_DBL);
		parentVO.setGloballocal(UFDouble.ZERO_DBL);
		parentVO.setGroupnotax(UFDouble.ZERO_DBL);
		parentVO.setGrouprate(UFDouble.ZERO_DBL);
		parentVO.setGrouptax(UFDouble.ZERO_DBL);
		parentVO.setIsflowbill(UFBoolean.FALSE);
		parentVO.setIsinit(UFBoolean.FALSE);
		parentVO.setIsreded(UFBoolean.FALSE);
		//本币实洋
		parentVO.setLocal_money(new UFDouble(parent.getDouble("local_money")));
		//本币码洋
		parentVO.setDef2(parent.getString("def2"));
		//?
		parentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
		//原币实洋
		parentVO.setMoney(new UFDouble(parent.getDouble("money")));	
		//原币码洋
		parentVO.setDef3(parent.getString("def3"));
		//往来对象：0，客户
		parentVO.setObjtype(0);	
		//单据类型编码
		parentVO.setPk_billtype(defaultBillType);	
		//币种
		parentVO.setPk_currtype(pk_currtype);	
		//废弃财务组织
		parentVO.setPk_fiorg(pk_org);	
		parentVO.setPk_group(pk_group);
		//财务组织
		parentVO.setPk_org(pk_org);	
		//仓号
		parentVO.setDef6(parent.getString("def6"));
		parentVO.setPk_tradetype(defaultTradeType);	//应收类型code
		parentVO.setPk_tradetypeid(pk_tradetypeid);
		//摘要
		parentVO.setScomment(scomment);	
		//收货国
		parentVO.setRececountryid(rececountryid);
		parentVO.setSrc_syscode(17);	//单据来源系统:17,外部交换平台
		//报税国
		parentVO.setTaxcountryid(rececountryid);
		parentVO.setSyscode(1);	//0=应收系统，1=应付系统
		//来源系统
		parentVO.setDef4(outterSys);
		//对应红冲单据号
		parentVO.setDef5(parent.getString("def5"));
		
		/**  --表体--  **/
		JSONArray children = bill.getJSONArray("children");
		
		PayableBillItemVO[] childrenVO = new PayableBillItemVO[children.size()];
		for(int i=0; i<children.size(); i++) {
			
			JSONObject child = children.getJSONObject(i);
			UFDouble money_cr = new UFDouble(child.getString("money_cr"));
			UFDouble local_money_cr = new UFDouble(child.getString("local_money_cr"));
			SupplierVO supplierVO = importUtils.getSupplierPkByCode(outterSys, child.getString("supplier"), pk_group, pk_org);
			//发货国
			//String sendcountryid = importUtils.getCountryBySupplierID(pk_supplier);
			String pk_material = importUtils.getMaterialPkByCode(child.getString("material"), pk_group, pk_org);
			String taxcodeid = importUtils.getTaxcodePkByCode(supplierVO.getPk_country(), child.getString("material"));
			UFDouble childRate = new UFDouble(child.getString("rate"));
			if(i == 0) {
				supplier = supplierVO.getPrimaryKey();
				rate = childRate;
			}
			
			PayableBillItemVO childVO = new PayableBillItemVO();
			childVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			childVO.setBillclass("yf");
			childVO.setBilldate(billdate);
			childVO.setBusidate(billdate);
			//购销类型：1，国内销售；3出口销售
			childVO.setBuysellflag(1);	
			//计税金额
			childVO.setCaltaxmny(new UFDouble(1401L));	
			//方向：1=借方，-1=贷方
			childVO.setDirection(-1);	
			childVO.setForcemoney(UFDouble.ZERO_DBL);
			childVO.setGlobalagentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setGlobalbalance(UFDouble.ZERO_DBL);
			childVO.setGlobalcrebit(UFDouble.ZERO_DBL);
			childVO.setGlobaldebit(UFDouble.ZERO_DBL);
			childVO.setGlobalnotax_cre(UFDouble.ZERO_DBL);
			childVO.setGlobalnotax_de(UFDouble.ZERO_DBL);
			childVO.setGlobalrate(null);
			childVO.setGlobaltax_cre(UFDouble.ZERO_DBL);
			childVO.setGlobaltax_de(UFDouble.ZERO_DBL);
			childVO.setGroupagentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setGroupbalance(UFDouble.ZERO_DBL);
			childVO.setGroupcrebit(null);
			childVO.setGroupdebit(null);
			childVO.setGroupnotax_cre(UFDouble.ZERO_DBL);
			childVO.setGroupnotax_de(UFDouble.ZERO_DBL);
			childVO.setGrouprate(null);
			childVO.setGrouptax_cre(UFDouble.ZERO_DBL);
			childVO.setGrouptax_de(UFDouble.ZERO_DBL);
			childVO.setIsrefused(UFBoolean.FALSE);
			//组织本币余额,实洋
			childVO.setLocal_money_bal(local_money_cr);
			//贷方组织本币，实洋
			childVO.setLocal_money_cr(local_money_cr);
			//码洋
			childVO.setDef2(child.getString("def2"));
			childVO.setLocal_money_de(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_price(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//原币余额金额，实洋
			childVO.setMoney_bal(money_cr);	
			//贷方原币，实洋
			childVO.setMoney_cr(money_cr);
			//码洋
			childVO.setDef1(child.getString("def1"));
			childVO.setMoney_de(UFDouble.ZERO_DBL);	
			//物料
			childVO.setMaterial(pk_material);
			childVO.setMaterial_src(pk_material);
			childVO.setNosubtax(UFDouble.ZERO_DBL);
			childVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			childVO.setNotax_cr(money_cr);
			childVO.setNotax_de(UFDouble.ZERO_DBL);
			//往来对象，默认是供应商
			childVO.setObjtype(1);
			//供应商
			childVO.setSupplier(supplierVO.getPrimaryKey());
			childVO.setOccupationmny(money_cr);
			childVO.setPausetransact(UFBoolean.FALSE);	//	挂起标志
			childVO.setPk_billtype(defaultBillType);
			childVO.setPk_currtype(pk_currtype);
			childVO.setPk_group(pk_group);
			childVO.setPk_org(pk_org);
			childVO.setPk_tradetype(defaultTradeType);
			childVO.setPk_tradetypeid(pk_tradetypeid);
			childVO.setPrepay(0);
			childVO.setQuantity_bal(UFDouble.ZERO_DBL);
			childVO.setRate(childRate);
			//发货国
			childVO.setSendcountryid(supplierVO.getPk_country());
			//摘要
			childVO.setScomment(scomment);
			childVO.setSett_org(pk_org);	//结算财务组织
			childVO.setStatus(2);
			childVO.setTaxcodeid(taxcodeid);	//税码
			childVO.setTaxprice(UFDouble.ZERO_DBL);	//含税单价
			//税率
			childVO.setTaxrate(UFDouble.ZERO_DBL);	
			//扣税类别：0=应税内含，1=应税外加
			childVO.setTaxtype(0);	
			childVO.setTriatradeflag(UFBoolean.FALSE);	//	三级贸易
			
			childrenVO[i] = childVO;
		}
		
		/** 表头回填的数据  **/
		//客户
		parentVO.setSupplier(supplier);
		//组织本币汇率
		parentVO.setRate(rate);	
		
		aggPayableBillVO.setParentVO(parentVO);
		aggPayableBillVO.setChildrenVO(childrenVO);
		return aggPayableBillVO;
	}

}
