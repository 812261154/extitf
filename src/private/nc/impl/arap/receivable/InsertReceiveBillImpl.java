package nc.impl.arap.receivable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.bs.arap.util.TransUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.receivable.IInsertReceiveBill;
import nc.pubitf.arap.payable.IArapPayableBillPubService;
import nc.pubitf.arap.receivable.IArapReceivableBillPubService;
import nc.vo.arap.payable.AggPayableBillVO;
import nc.vo.arap.payable.PayableBillItemVO;
import nc.vo.arap.payable.PayableBillVO;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillItemVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.org.DeptVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;

public class InsertReceiveBillImpl implements IInsertReceiveBill {

	private TransUtils importUtils = new TransUtils();

	@Override
	public String insertReceiveBill_RequiresNew(AggReceivableBillVO vo) {
		String rt = "";
		AggReceivableBillVO pmptgVO = vo.clone();
		IArapReceivableBillPubService service = (IArapReceivableBillPubService) NCLocator.getInstance().lookup(IArapReceivableBillPubService.class.getName());
		try{
			AggReceivableBillVO rtVO = service.save(vo);
			initApproveInfo(rtVO);
			service.approve(rtVO);
			//需要传应付
			if(StringUtils.equals((String)vo.getParentVO().getAttributeValue("def14"), "Y")) {
				AggPayableBillVO[] payVos = recToPayBill(vo);
				IArapPayableBillPubService payService = (IArapPayableBillPubService) NCLocator.getInstance().lookup(IArapPayableBillPubService.class.getName());
				AggPayableBillVO[] payRtVO = payService.save(payVos);
				initApproveInfo(payRtVO);
				payService.approve(payRtVO);
			}
			
			//团购系统：书城的发货单、客退单，做集团本部对书城的应收，书城对集团本部的应付，书城对客户的应收
			Set<String> set = TransUtils.arrayToSet(FileUtils.getProperties("nc/bs/arap/properties/PMPTGPram.properties", "BookstoreOrg").split(","));
			OrgVO orgVO = importUtils.getOrgByPk((String)vo.getParentVO().getAttributeValue("pk_org"));
			if(StringUtils.equals((String)vo.getParentVO().getAttributeValue("def4"), "PMPTG")
					&& set.contains(orgVO.getCode())) {
				transTGRecBill(pmptgVO);
				AggReceivableBillVO rtTGVO = service.save(pmptgVO);
				initApproveInfo(rtTGVO);
				service.approve(rtTGVO);
				
				AggPayableBillVO[] payTGVos = recToPayBill(pmptgVO);
				IArapPayableBillPubService payService = (IArapPayableBillPubService) NCLocator.getInstance().lookup(IArapPayableBillPubService.class.getName());
				AggPayableBillVO[] payTGRtVO = payService.save(payTGVos);
				initApproveInfo(payTGRtVO);
				payService.approve(payTGRtVO);
			}
			
		} catch(Exception e) {
			rt = e.getMessage();
		}
		return rt;
	}
	
	//初始化审核信息
	private void initApproveInfo(AggReceivableBillVO vo) throws BusinessException {
		ReceivableBillVO parentVO = (ReceivableBillVO)vo.getParentVO();
		parentVO.setApprovestatus(1);	//审批状态:1=通过态
		parentVO.setBillstatus(1);		//单据状态:1=审批通过
		parentVO.setApprover(parentVO.getBillmaker());		//审核人
		parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//审核日期
		parentVO.setEffectstatus(10);						//生效状态：10=已生效，0=未生效
		parentVO.setEffectdate(new UFDate());				//生效日期
		parentVO.setEffectuser(parentVO.getBillmaker());		//生效人
	}
	
	//初始化审核信息
	private void initApproveInfo(AggPayableBillVO[] vos) throws BusinessException {
		for(AggPayableBillVO vo : vos) {
			PayableBillVO parentVO = (PayableBillVO)vo.getParentVO();
			parentVO.setApprovestatus(1);	//审批状态:1=通过态
			parentVO.setBillstatus(1);		//单据状态:1=审批通过
			parentVO.setApprover(parentVO.getBillmaker());		//审核人
			parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//审核日期
			parentVO.setEffectstatus(10);						//生效状态：10=已生效，0=未生效
			parentVO.setEffectdate(new UFDate());				//生效日期
			parentVO.setEffectuser(parentVO.getBillmaker());		//生效人
		}
	}
	
	//应收单vo转化成应付单vo
	private AggPayableBillVO[] recToPayBill(AggReceivableBillVO recVO) throws BusinessException {
		
		List<AggPayableBillVO> list = new ArrayList<AggPayableBillVO>();
		
		ReceivableBillVO recParentVO = (ReceivableBillVO)recVO.getParentVO();
		ReceivableBillItemVO[] recChilrenVO = (ReceivableBillItemVO[])recVO.getChildrenVO();
		String pk_group = recParentVO.getPk_group();
		UFDate billdate = recParentVO.getBilldate();
		Map<String, String> defaultParam = importUtils.getDefaultParam("ap");
		String defaultTradeType = defaultParam.get("default_tradetype");
		String defaultBillType = defaultParam.get("default_billtype");
		
		String tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, pk_group);
		DeptVO deptVO = importUtils.getDeptPkByCode(pk_group, recParentVO.getPk_org(), recParentVO.getDef6());
		for(ReceivableBillItemVO recChildVO : recChilrenVO) {
			
			String pk_org = importUtils.getOrgByCustomer(recChildVO.getCustomer());
			String pk_supplier = importUtils.getSupplierByOrg(recParentVO.getPk_org());
			
			AggPayableBillVO aggPayableBillVO = new AggPayableBillVO();
			PayableBillVO payParentVO = new PayableBillVO();
			
			payParentVO.setApprovestatus(3);	//审批状态:1=通过态,3=提交态
			payParentVO.setBillstatus(-1);		//单据状态:1=审批通过，-1=保存
			payParentVO.setEffectstatus(0);  	//生效状态：10=已生效，0=未生效
			
			//外系统单据号
			payParentVO.setDef1(recParentVO.getDef1());
			payParentVO.setBillclass("yf");
			payParentVO.setBilldate(billdate);
			payParentVO.setBillmaker(recParentVO.getBillmaker());
			payParentVO.setCreationtime(recParentVO.getCreationtime());
			payParentVO.setCreator(recParentVO.getCreator());
			payParentVO.setGloballocal(UFDouble.ZERO_DBL);
			payParentVO.setGlobalnotax(UFDouble.ZERO_DBL);
			payParentVO.setGlobalrate(UFDouble.ZERO_DBL);
			payParentVO.setGlobaltax(UFDouble.ZERO_DBL);
			payParentVO.setGloballocal(UFDouble.ZERO_DBL);
			payParentVO.setGroupnotax(UFDouble.ZERO_DBL);
			payParentVO.setGrouprate(UFDouble.ZERO_DBL);
			payParentVO.setGrouptax(UFDouble.ZERO_DBL);
			payParentVO.setIsflowbill(UFBoolean.FALSE);
			payParentVO.setIsinit(UFBoolean.FALSE);
			payParentVO.setIsreded(UFBoolean.FALSE);
			//本币实洋
			payParentVO.setLocal_money(recChildVO.getLocal_money_de());
			//本币码洋
			payParentVO.setDef2(recChildVO.getDef2());
			//?
			payParentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
			//原币实洋
			payParentVO.setMoney(recChildVO.getMoney_de());	
			//原币码洋
			payParentVO.setDef3(recChildVO.getDef1());
			//往来对象：0，客户
			payParentVO.setObjtype(0);	
			//单据类型编码
			payParentVO.setPk_billtype(defaultBillType);	
			//币种
			payParentVO.setPk_currtype(recChildVO.getPk_currtype());	
			//废弃财务组织
			payParentVO.setPk_fiorg(pk_org);	
			payParentVO.setPk_group(pk_group);
			//财务组织
			payParentVO.setPk_org(pk_org);	
			payParentVO.setPk_tradetype(defaultTradeType);	//应收类型code
			payParentVO.setPk_tradetypeid(tradetypeid);
			//组织本币汇率
			payParentVO.setRate(recChildVO.getRate());	
			//摘要
			payParentVO.setScomment(recParentVO.getScomment());	
			//供应商
			payParentVO.setSupplier(pk_supplier);	
			//收货国
			payParentVO.setRececountryid(recChildVO.getRececountryid());
			payParentVO.setSrc_syscode(17);	//单据来源系统:17,外部交换平台
			//报税国
			payParentVO.setTaxcountryid(recChildVO.getRececountryid());
			payParentVO.setSyscode(1);	//0=应收系统，1=应付系统
			//来源系统
			payParentVO.setDef4(recParentVO.getDef4());
			//对应红冲单据号
			payParentVO.setDef5(recParentVO.getDef5());
			
			
			PayableBillItemVO payChildVO = new PayableBillItemVO();
			payChildVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			payChildVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			payChildVO.setBillclass("yf");
			payChildVO.setBilldate(billdate);
			payChildVO.setBusidate(billdate);
			//购销类型：2=国内采购，4=进口采购，
			payChildVO.setBuysellflag(2);
			//计税金额
			payChildVO.setCaltaxmny(new UFDouble(1401L));	
			//方向：1=借方，-1=贷方
			payChildVO.setDirection(-1);	
			payChildVO.setForcemoney(UFDouble.ZERO_DBL);
			payChildVO.setGlobalagentreceivelocal(UFDouble.ZERO_DBL);
			payChildVO.setGlobalbalance(UFDouble.ZERO_DBL);
			payChildVO.setGlobalcrebit(UFDouble.ZERO_DBL);
			payChildVO.setGlobaldebit(UFDouble.ZERO_DBL);
			payChildVO.setGlobalnotax_cre(UFDouble.ZERO_DBL);
			payChildVO.setGlobalnotax_de(UFDouble.ZERO_DBL);
			payChildVO.setGlobalrate(null);
			payChildVO.setGlobaltax_cre(UFDouble.ZERO_DBL);
			payChildVO.setGlobaltax_de(UFDouble.ZERO_DBL);
			payChildVO.setGroupagentreceivelocal(UFDouble.ZERO_DBL);
			payChildVO.setGroupbalance(UFDouble.ZERO_DBL);
			payChildVO.setGroupcrebit(null);
			payChildVO.setGroupdebit(null);
			payChildVO.setGroupnotax_cre(UFDouble.ZERO_DBL);
			payChildVO.setGroupnotax_de(UFDouble.ZERO_DBL);
			payChildVO.setGrouprate(null);
			payChildVO.setGrouptax_cre(UFDouble.ZERO_DBL);
			payChildVO.setGrouptax_de(UFDouble.ZERO_DBL);
			payChildVO.setIsrefused(UFBoolean.FALSE);
			//组织本币余额,实洋
			payChildVO.setLocal_money_bal(recChildVO.getLocal_money_bal());
			//贷方组织本币，实洋
			payChildVO.setLocal_money_cr(recChildVO.getLocal_money_de());
			//本币码洋
			payChildVO.setDef2(recChildVO.getDef2());
			payChildVO.setLocal_money_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			payChildVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_price(UFDouble.ZERO_DBL);
			payChildVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			payChildVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//原币余额金额，实洋
			payChildVO.setMoney_bal(recChildVO.getMoney_bal());	
			//贷方原币，实洋
			payChildVO.setMoney_cr(recChildVO.getMoney_de());
			//原币码洋
			payChildVO.setDef1(recChildVO.getDef1());
			payChildVO.setMoney_de(UFDouble.ZERO_DBL);	
			//物料
			payChildVO.setMaterial(recChildVO.getMaterial());
			payChildVO.setMaterial_src(recChildVO.getMaterial_src());
			payChildVO.setNosubtax(UFDouble.ZERO_DBL);
			payChildVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			payChildVO.setNotax_cr(recChildVO.getNotax_de());
			payChildVO.setNotax_de(UFDouble.ZERO_DBL);
			//往来对象，默认是供应商
			payChildVO.setObjtype(1);
			//供应商
			payChildVO.setSupplier(pk_supplier);
			//仓号
			payChildVO.setDef3(payParentVO.getDef7());
			payChildVO.setOccupationmny(recChildVO.getOccupationmny());
			payChildVO.setPausetransact(UFBoolean.FALSE);	//	挂起标志
			payChildVO.setPk_billtype(defaultBillType);
			payChildVO.setPk_currtype(recChildVO.getPk_currtype());
			//部门
			payChildVO.setPk_deptid(deptVO.getPk_dept());
			payChildVO.setPk_deptid_v(deptVO.getPk_vid());
			payChildVO.setPk_group(pk_group);
			payChildVO.setPk_org(pk_org);
			payChildVO.setPk_tradetype(defaultTradeType);
			payChildVO.setPk_tradetypeid(tradetypeid);
			payChildVO.setPrepay(0);
			payChildVO.setQuantity_bal(UFDouble.ZERO_DBL);
			payChildVO.setRate(recChildVO.getRate());
			//发货国
			payChildVO.setSendcountryid(payParentVO.getSendcountryid());
			//摘要
			payChildVO.setScomment(recChildVO.getScomment());
			payChildVO.setSett_org(pk_org);	//结算财务组织
			payChildVO.setStatus(2);
			payChildVO.setTaxcodeid(recChildVO.getTaxcodeid());	//税码
			payChildVO.setTaxprice(UFDouble.ZERO_DBL);	//含税单价
			//税率
			payChildVO.setTaxrate(UFDouble.ZERO_DBL);	
			//扣税类别：0=应税内含，1=应税外加
			payChildVO.setTaxtype(0);	
			payChildVO.setTriatradeflag(UFBoolean.FALSE);	//	三级贸易
			
			aggPayableBillVO.setParentVO(payParentVO);
			aggPayableBillVO.setChildrenVO(new PayableBillItemVO[]{ payChildVO });
			list.add(aggPayableBillVO);
		}
		return (AggPayableBillVO[])list.toArray(new AggPayableBillVO[0]);		
	}
	
	private void transTGRecBill(AggReceivableBillVO vo) throws BusinessException {
		
		ReceivableBillVO parentVO = (ReceivableBillVO) vo.getParentVO();
		OrgVO orgVO = importUtils.getOrgByCode(parentVO.getPk_group(), FileUtils.getProperties("nc/bs/arap/properties/PMPTGPram.properties", "Headquarters"));
		String oldOrg = parentVO.getPk_org();
		parentVO.setPk_org(orgVO.getPk_org());
		parentVO.setPk_fiorg(orgVO.getPk_org());
		
		ReceivableBillItemVO[] childrenVO = (ReceivableBillItemVO[]) vo.getChildrenVO();
		for(ReceivableBillItemVO childVO : childrenVO) {
			childVO.setPk_org(orgVO.getPk_org());
			childVO.setSett_org(orgVO.getPk_org());
			childVO.setCustomer(importUtils.getCustomerByOrg(oldOrg));
		}
	}

}
