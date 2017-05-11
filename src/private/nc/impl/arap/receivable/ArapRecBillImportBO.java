package nc.impl.arap.receivable;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import nc.bs.arap.util.BillUtils;
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
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.inoutbusiclass.InoutBusiClassVO;
import nc.vo.org.DeptVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapRecBillImportBO {

	private BillUtils importUtils = new BillUtils();
	
	public void recBillImport() throws BusinessException {
		
		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar");
		AggReceivableBillVO[] vos = arrayListToVos(list);
		Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],成功转换本地数据到VO" +vos.length+ "条。");
		
		//导入结果
		JSONArray rtArray = new JSONArray();
		String outterBillno = "";
		IInsertReceiveBill service = NCLocator.getInstance().lookup(IInsertReceiveBill.class);
		int successCount = 0;
		for(AggReceivableBillVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			outterBillno = (String) vo.getParentVO().getAttributeValue("def1");
			rtJson.put("FROMSYS", (String) vo.getParentVO().getAttributeValue("def4"));
			rtJson.put("NUMBER", outterBillno);
			try {
				if(isBillExit(vo)) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "该单据已存在，不可重复录入!");
					Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],单据" +outterBillno+ "已存在，不可重复录入!");
				} else {
					String rt = service.insertReceiveBill_RequiresNew(vo);
					if(StringUtils.isBlank(rt)) {
						rtJson.put("COMPLETED", "1");
						rtJson.put("MESSAGE", "");
						successCount++;
					} else {
						rtJson.put("COMPLETED", "0");
						rtJson.put("MESSAGE", rt);
						Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],单据" +outterBillno+ "导入失败，问题原因：" + rt);
					}
				}
			} catch(Exception e3) {
				rtJson.put("COMPLETED", "0");
				rtJson.put("MESSAGE", e3.getMessage());
				Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],单据" +outterBillno+ "导入失败，问题原因：" + e3.getMessage());
			}
			rtArray.add(rtJson);
		}
		
		Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],导入成功" +successCount+ "条。");
		importUtils.insertImportResult(rtArray);
		rtArray.clear();
		try {
			rtArray = BillUtils.queryImportResult();
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
			Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],取本地文件数据" +billsArray.size()+ "条。");
			//存放翻译成功的单据
			String outterBillno = "";
			JSONObject bill = null;
			//
			JSONArray rtArray = new JSONArray();
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = BillUtils.getArapBillSourceNo(bill);
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
					Logger.error("[SDPG][" + ArapRecBillImportBO.class.getName() + "],单据[" +outterBillno+ "]导入出错，问题原因：" + e3.getMessage());
				}
			}
			if(rtArray.size() > 0) {
				importUtils.insertImportResult(rtArray);
				Logger.info("[SDPG][" + ArapRecBillImportBO.class.getName() + "],失败" +billsArray.size()+ "条。");
			}
		}
		return (AggReceivableBillVO[])voList.toArray(new AggReceivableBillVO[0]);
	}
	
	//将json格式的参数转换成vo
	private AggReceivableBillVO jsonToVO(JSONObject bill) throws BusinessException {
		
		String sourceSys = BillUtils.getSourceSysFromJson(bill);
		
		Properties p = FileUtils.getProperties("nc/bs/arap/properties/ArapWsPrams.properties");
		String defaultGroup = p.getProperty("defaultGroup");
		String defaultUser = p.getProperty("defaultUser");
		String defaultTradeType = p.getProperty(sourceSys+"RecTradetype");
		String defaultBillType = p.getProperty("defaultRecBilltype");
		
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
		String outterSysNum = parent.getString("def1");
		String scomment = "";
		
		
		parentVO.setApprovestatus(3);	//审批状态:1=通过态,3=提交态
		parentVO.setBillstatus(-1);		//单据状态:1=审批通过，-1=保存
		parentVO.setEffectstatus(0);  	//生效状态：10=已生效，0=未生效
		
		parentVO.setBillclass("ys");
		parentVO.setBilldate(billdate);
		parentVO.setBillmaker(pk_user);
		parentVO.setCreationtime(new UFDateTime());
		parentVO.setCreator(pk_user);
		parentVO.setIsflowbill(UFBoolean.FALSE);
		parentVO.setIsinit(UFBoolean.FALSE);
		parentVO.setIsreded(UFBoolean.FALSE);
		//本币实洋
		parentVO.setLocal_money(new UFDouble(parent.getDouble("local_money")));
		//原币实洋
		parentVO.setMoney(new UFDouble(parent.getDouble("money")));	
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
		parentVO.setPk_tradetype(defaultTradeType);	//应收类型code
		parentVO.setPk_tradetypeid(pk_tradetypeid);
		//发货国
		parentVO.setSendcountryid(sendCountryId);	
		parentVO.setSrc_syscode(17);	//单据来源系统:17,外部交换平台
		//报税国
		parentVO.setTaxcountryid(sendCountryId);
		parentVO.setSyscode(0);	//0=应收系统，1=应付系统
		parentVO.setDef1(outterSysNum);				//外系统单据号
		parentVO.setDef4(sourceSys);				//来源系统
		
		if(StringUtils.equals(sourceSys, "ZCZL")) {
			
		} else {
			//摘要
			scomment = getJSONValue(parent, "scomment");
			parentVO.setScomment(scomment);	
			parentVO.setDef2(parent.getString("def2"));	//本币码洋
			parentVO.setDef3(parent.getString("def3"));	//原币码洋
			parentVO.setDef5(parent.getString("def5"));	//对应红冲单据号
			parentVO.setDef6(parent.getString("def6"));	//仓号
			//仓号对应虚拟部门
			parentVO.setDef7(parent.getString("def7"));	
			parentVO.setDef14(StringUtils.equalsIgnoreCase(bill.getString("needarap"), "Y") ? "Y" : "N");	//使用def14表示是否传应付
		}
		
		/**  --表体--  **/
		JSONArray children = bill.getJSONArray("children");
		
		ReceivableBillItemVO[] childrenVO = new ReceivableBillItemVO[children.size()];
		for(int i=0; i<children.size(); i++) {
			
			JSONObject child = children.getJSONObject(i);
			UFDouble money_de = new UFDouble(child.getString("money_de"));
			UFDouble local_money_de = new UFDouble(child.getString("local_money_de"));
			CustomerVO customerVO = importUtils.getCustomerVOByCode(sourceSys, child.getString("customer"), pk_group, pk_org);
			String taxcodeid = importUtils.getTaxcodePkByCode(customerVO.getPk_country(), child.getString("material"));
			UFDouble childRate = new UFDouble(child.getString("rate"));
			
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
			childVO.setCustomer(customerVO.getPk_customer());	//客户
			childVO.setDirection(1);	//方向：1=借方，-1=贷方
			//组织本币余额,实洋
			childVO.setLocal_money_bal(local_money_de);
			childVO.setLocal_money_cr(UFDouble.ZERO_DBL);
			//组织本币，实洋
			childVO.setLocal_money_de(local_money_de);
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
			childVO.setRececountryid(customerVO.getPk_country());	//收货国
			childVO.setSett_org(pk_org);	//结算财务组织
			childVO.setStatus(2);
			childVO.setTaxcodeid(taxcodeid);	//税码
			childVO.setTaxprice(UFDouble.ZERO_DBL);	//含税单价
			//税率
			childVO.setTaxrate(UFDouble.ZERO_DBL);	
			//扣税类别：0=应税内含，1=应税外加
			childVO.setTaxtype(0);	
			childVO.setTriatradeflag(UFBoolean.FALSE);	//三级贸易
			
			//部门
			DeptVO deptVO = importUtils.getDeptVOByCode(pk_group, pk_org, parent.getString("def7"));
			childVO.setPk_deptid(deptVO.getPk_dept());
			childVO.setPk_deptid_v(deptVO.getPk_vid());
			
			if(StringUtils.equals(sourceSys, "ZCZL")) {
				InoutBusiClassVO inoutBusiClassVO = importUtils.getInoutBusiClassVO(child.getString("pk_subjcode"), pk_group, pk_org);
				childVO.setPk_subjcode(inoutBusiClassVO.getPk_inoutbusiclass());
				//结算单号
//				childVO.setDef10(child.getString("def10"));
				childVO.setScomment(getJSONValue(child, "scomment"));
				
			} else {
				
				//摘要
				childVO.setScomment(scomment);
				//物料
				String pk_material = importUtils.getMaterialPkByCode(child.getString("material"), pk_group, pk_org);
				childVO.setMaterial(pk_material);
				childVO.setMaterial_src(pk_material);
				childVO.setDef1(child.getString("def1"));	//原币码洋
				childVO.setDef2(child.getString("def2"));	//组织本币码洋
				childVO.setDef3(getJSONValue(child, "def3"));	//客户对应仓号
				childVO.setDef4(getJSONValue(child, "def4"));	//仓号对应虚拟部门
			}
			
			childrenVO[i] = childVO;
		}
		
		aggReceivableBillVO.setParentVO(parentVO);
		aggReceivableBillVO.setChildrenVO(childrenVO);
		return aggReceivableBillVO;
	}
	
	private String getJSONValue(JSONObject json, String key) {
		return json.containsKey(key) ? json.getString(key) : "";
	}
	
}
