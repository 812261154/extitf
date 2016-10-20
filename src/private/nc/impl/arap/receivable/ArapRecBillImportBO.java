package nc.impl.arap.receivable;

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
import nc.itf.arap.receivable.IInsertReceiveBill;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillItemVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapRecBillImportBO {

	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "3";
	private String defaultTradeType = "D0";
	private String defaultBillType = "F0";
	
	public void recBillImport() throws BusinessException {
		Map<String, String> defaultParam = importUtils.getDefaultParam("ar");
		defaultGroup = defaultParam.get("default_group");
		defaultUser = defaultParam.get("default_user");
		defaultTradeType = defaultParam.get("default_tradetype");
		defaultBillType = defaultParam.get("default_billtype");
		
		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar");
		AggReceivableBillVO[] vos = arrayListToVos(list);
		
		//导入结果
		JSONArray rtArray = new JSONArray();
		String outterBillno = "";
		IInsertReceiveBill service = NCLocator.getInstance().lookup(IInsertReceiveBill.class);
		for(AggReceivableBillVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			outterBillno = (String) vo.getParentVO().getAttributeValue("def1");
			rtJson.put("FROMSYS", (String) vo.getParentVO().getAttributeValue("def4"));
			rtJson.put("NUMBER", outterBillno);
			try {
				if(isBillExit(vo)) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "该单据已存在，不可重复录入!");
				} else {
					String rt = service.insertReceiveBill_RequiresNew(vo);
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
	private boolean isBillExit(AggReceivableBillVO vo) throws BusinessException {
		String sql = "select count(*) from ar_recbill t where t.dr=0 and t.def1 = '" +vo.getParentVO().getAttributeValue("def1")+ "' and t.def4 = '" +vo.getParentVO().getAttributeValue("def4")+ "'";
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer)baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (DAOException e) {
			throw new BusinessException("单据是否重复验证时出错！");
		}
		return count > 0;
	}
	
	
	
	private AggReceivableBillVO[] arrayListToVos(List<JSONArray> arrayList) {
		List<AggReceivableBillVO> voList = new ArrayList<AggReceivableBillVO>();
		for(JSONArray billsArray : arrayList) {
			//存放翻译成功的单据
			String outterBillno = "";
			JSONObject bill = null;
			//
			JSONArray rtArray = new JSONArray();
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = importUtils.getExBillNo(bill);
				try {
					AggReceivableBillVO vo = jsonToVO(bill);
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
			if(rtArray.size() > 0) {
				importUtils.insertImportResult(rtArray);
			}
		}
		return (AggReceivableBillVO[])voList.toArray(new AggReceivableBillVO[0]);
	}
	
	//将json格式的参数转换成vo
	private AggReceivableBillVO jsonToVO(JSONObject bill) throws BusinessException {
		
		AggReceivableBillVO aggReceivableBillVO = new AggReceivableBillVO();
		ReceivableBillVO parentVO = new ReceivableBillVO();
		
		/**  --表头--  **/
		JSONObject parent = bill.getJSONObject("parent");
		UFDate billdate = new UFDate(parent.getString("billdate"));
		String pk_group = importUtils.getCurrGroupPk(defaultGroup);
		String pk_org = importUtils.getOrgByCode(pk_group, parent.getString("pk_org")).getPrimaryKey();
		String pk_user = importUtils.getInitUserPkByCode(defaultUser);
		String pk_tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, pk_group);
		String pk_currtype = importUtils.getCurrtypeByCode(parent.getString("pk_currtype")).getPrimaryKey();
		String sendCountryId = importUtils.getCountryId(pk_org);
		String scomment = parent.getString("scomment");
		String outterSysNum = parent.getString("def1");
		String outterSys = parent.getString("def4");
		String customer = "";
		UFDouble rate = null;
		
		
		parentVO.setApprovestatus(3);	//审批状态:1=通过态,3=提交态
		parentVO.setBillstatus(-1);		//单据状态:1=审批通过，-1=保存
		parentVO.setEffectstatus(0);  	//生效状态：10=已生效，0=未生效
		
		//外系统单据号
		parentVO.setDef1(outterSysNum);
		parentVO.setBillclass("ys");
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
		//仓号对应虚拟部门
		parentVO.setDef6(parent.getString("def6"));
		//仓号
		parentVO.setDef7(parent.getString("def7"));
		parentVO.setPk_tradetype(defaultTradeType);	//应收类型code
		parentVO.setPk_tradetypeid(pk_tradetypeid);
		//摘要
		parentVO.setScomment(scomment);	
		//发货国
		parentVO.setSendcountryid(sendCountryId);	
		parentVO.setSrc_syscode(17);	//单据来源系统:17,外部交换平台
		//报税国
		parentVO.setTaxcountryid(sendCountryId);
		parentVO.setSyscode(0);	//0=应收系统，1=应付系统
		//来源系统
		parentVO.setDef4(outterSys);
		//对应红冲单据号
		parentVO.setDef5(parent.getString("def5"));
		//使用def14表示是否传应付
		parentVO.setDef14(StringUtils.equalsIgnoreCase(bill.getString("needarap"), "Y") ? "Y" : "N");
		
		/**  --表体--  **/
		JSONArray children = bill.getJSONArray("children");
		
		ReceivableBillItemVO[] childrenVO = new ReceivableBillItemVO[children.size()];
		for(int i=0; i<children.size(); i++) {
			
			JSONObject child = children.getJSONObject(i);
			UFDouble money_de = new UFDouble(child.getString("money_de"));
			UFDouble local_money_de = new UFDouble(child.getString("local_money_de"));
			String pk_customer = importUtils.getCustomerPkByCode(outterSys, child.getString("customer"), pk_group, pk_org);
			String rececountryid = importUtils.getCountryByCustomerID(pk_customer);
			String pk_material = importUtils.getMaterialPkByCode(child.getString("material"), pk_group, pk_org);
			String taxcodeid = importUtils.getTaxcodePkByCode(rececountryid, child.getString("material"));
			UFDouble childRate = new UFDouble(child.getString("rate"));
			if(i == 0) {
				customer = pk_customer;
				rate = childRate;
			}
			
			ReceivableBillItemVO childVO = new ReceivableBillItemVO();
			childVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			childVO.setBillclass("ys");
			childVO.setBilldate(billdate);
			childVO.setBusidate(billdate);
			//购销类型：1，国内销售；3出口销售
			childVO.setBuysellflag(1);	
			//计税金额
			childVO.setCaltaxmny(new UFDouble(1401L));	
			childVO.setCustomer(pk_customer);	//客户
			childVO.setDirection(1);	//方向：1=借方，-1=贷方
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
			childVO.setLocal_money_bal(local_money_de);
			childVO.setLocal_money_cr(UFDouble.ZERO_DBL);
			//组织本币，实洋
			childVO.setLocal_money_de(local_money_de);
			//码洋
			childVO.setDef1(child.getString("def2"));
			childVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_price(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//原币余额金额，实洋
			childVO.setMoney_bal(money_de);	
			childVO.setMoney_cr(UFDouble.ZERO_DBL);
			//借方原币，实洋
			childVO.setMoney_de(money_de);	
			//码洋
			childVO.setDef1(child.getString("def1"));
			//物料
			childVO.setMaterial(pk_material);
			childVO.setMaterial_src(pk_material);
			childVO.setNosubtax(UFDouble.ZERO_DBL);
			childVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			childVO.setNotax_cr(UFDouble.ZERO_DBL);
			childVO.setNotax_de(money_de);
			//往来对象，默认是客户
			childVO.setObjtype(0);
			childVO.setOccupationmny(money_de);
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
			childVO.setRececountryid(rececountryid);	//收货国
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
		parentVO.setCustomer(customer);	
		//组织本币汇率
		parentVO.setRate(rate);
		
		aggReceivableBillVO.setParentVO(parentVO);
		aggReceivableBillVO.setChildrenVO(childrenVO);
		return aggReceivableBillVO;
	}
	
}
