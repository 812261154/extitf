package nc.impl.arap.pay;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.framework.common.NCLocator;
import nc.itf.arap.payable.IInsertPayableBill;
import nc.pubitf.arap.payable.IArapPayableBillPubService;
import nc.pubitf.arap.receivable.IArapReceivableBillPubService;
import nc.vo.arap.payable.AggPayableBillVO;
import nc.vo.arap.payable.PayableBillItemVO;
import nc.vo.arap.payable.PayableBillVO;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillItemVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.org.DeptVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;

import org.apache.commons.lang.StringUtils;

public class InsertPayableBillImpl implements
		IInsertPayableBill {
	
	private BillUtils importUtils = new BillUtils();

	@Override
	public String insertPayableBill_RequiresNew(AggPayableBillVO vo) {
		String rt = "";
		IArapPayableBillPubService service = (IArapPayableBillPubService) NCLocator.getInstance().lookup(IArapPayableBillPubService.class.getName());
		try {
			AggPayableBillVO rtVO = service.save(vo);
//			initApproveInfo(vo);
//			service.approve(rtVO);
			
			//需要传应收
			if(StringUtils.equals((String)vo.getParentVO().getAttributeValue("def14"), "Y")) {
				AggReceivableBillVO[] recVOs = payToRecVO(vo);
				IArapReceivableBillPubService recService = (IArapReceivableBillPubService) NCLocator.getInstance().lookup(IArapReceivableBillPubService.class.getName());
				AggReceivableBillVO[] recRtVOs = recService.save(recVOs);
//				initApproveInfo(recRtVOs);
//				recService.approve(recRtVOs);
			}
			
		} catch (Exception e) {
			rt = e.getMessage();
		}
		return rt;		
	}
	
	//初始化审核信息
	private void initApproveInfo(AggPayableBillVO vo) throws BusinessException {
		PayableBillVO parentVO = (PayableBillVO)vo.getParentVO();
		parentVO.setApprovestatus(1);	//审批状态:1=通过态
		parentVO.setBillstatus(1);		//单据状态:1=审批通过
		parentVO.setApprover(parentVO.getBillmaker());		//审核人
		parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//审核日期
		parentVO.setEffectstatus(10);						//生效状态：10=已生效，0=未生效
		parentVO.setEffectdate(new UFDate());				//生效日期
		parentVO.setEffectuser(parentVO.getBillmaker());		//生效人
	}
	
	//初始化审核信息
	private void initApproveInfo(AggReceivableBillVO[] vos) throws BusinessException {
		for(AggReceivableBillVO vo : vos) {
			ReceivableBillVO parentVO = (ReceivableBillVO)vo.getParentVO();
			parentVO.setApprovestatus(1);	//审批状态:1=通过态
			parentVO.setBillstatus(1);		//单据状态:1=审批通过
			parentVO.setApprover(parentVO.getBillmaker());		//审核人
			parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//审核日期
			parentVO.setEffectstatus(10);						//生效状态：10=已生效，0=未生效
			parentVO.setEffectdate(new UFDate());				//生效日期
			parentVO.setEffectuser(parentVO.getBillmaker());		//生效人
		}
	}
	
	//应付单转应收单
	private AggReceivableBillVO[] payToRecVO(AggPayableBillVO vo) throws BusinessException {
		
		List<AggReceivableBillVO> list = new ArrayList<AggReceivableBillVO>();
		PayableBillVO payParentVO = (PayableBillVO) vo.getParentVO();
		PayableBillItemVO[] payChildrenVO = (PayableBillItemVO[]) vo.getChildrenVO();
		
		Properties p = FileUtils.getProperties("nc/bs/arap/properties/ArapWsPrams.properties");
		String defaultTradeType = p.getProperty("defaultRecBilltype");
		String defaultBillType = p.getProperty("defaultRecTradetype");
		String pk_tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, payParentVO.getPk_group());
		UFDate billdate = payParentVO.getBilldate();
		String billmaker = payParentVO.getBillmaker();
		String pk_group = payParentVO.getPk_group();
		
		for(PayableBillItemVO payChildVO : payChildrenVO) {
			
			AggReceivableBillVO aggReceivableBillVO = new AggReceivableBillVO();
			ReceivableBillVO recParentVO = new ReceivableBillVO();
			String pk_customer = importUtils.getCustomerByOrg(payParentVO.getPk_org());
			String pk_supplierFinanceOrg = importUtils.getOrgBySupplier(payChildVO.getSupplier());
			DeptVO deptVO = importUtils.getDeptVOByCode(pk_group, pk_supplierFinanceOrg, payChildVO.getDef4());
			
			UFDouble local_money = payChildVO.getLocal_money_cr();
			UFDouble money = payChildVO.getMoney_cr();
			
			
			recParentVO.setApprovestatus(3);	//审批状态:1=通过态,3=提交态
			recParentVO.setBillstatus(-1);		//单据状态:1=审批通过，-1=保存
			recParentVO.setEffectstatus(0);  	//生效状态：10=已生效，0=未生效
			
			recParentVO.setBillclass("ys");
			recParentVO.setBilldate(billdate);
			recParentVO.setBillmaker(billmaker);
			recParentVO.setCreationtime(new UFDateTime());
			recParentVO.setCreator(billmaker);
			recParentVO.setGloballocal(UFDouble.ZERO_DBL);
			recParentVO.setGlobalnotax(UFDouble.ZERO_DBL);
			recParentVO.setGlobalrate(UFDouble.ZERO_DBL);
			recParentVO.setGlobaltax(UFDouble.ZERO_DBL);
			recParentVO.setGloballocal(UFDouble.ZERO_DBL);
			recParentVO.setGroupnotax(UFDouble.ZERO_DBL);
			recParentVO.setGrouprate(UFDouble.ZERO_DBL);
			recParentVO.setGrouptax(UFDouble.ZERO_DBL);
			recParentVO.setIsflowbill(UFBoolean.FALSE);
			recParentVO.setIsinit(UFBoolean.FALSE);
			recParentVO.setIsreded(UFBoolean.FALSE);
			//本币实洋
			recParentVO.setLocal_money(local_money);
			//?
			recParentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
			//原币实洋
			recParentVO.setMoney(money);	
			//往来对象：0，客户
			recParentVO.setObjtype(0);	
			//单据类型
			recParentVO.setPk_billtype(defaultBillType);	
			//币种
			recParentVO.setPk_currtype(payChildVO.getPk_currtype());	
			//废弃财务组织
			recParentVO.setPk_fiorg(pk_supplierFinanceOrg);	
			recParentVO.setPk_group(pk_group);
			//财务组织
			recParentVO.setPk_org(pk_supplierFinanceOrg);	
			recParentVO.setPk_tradetype(defaultTradeType);	//应收类型code
			recParentVO.setPk_tradetypeid(pk_tradetypeid);
			recParentVO.setPk_deptid(deptVO.getPk_dept());
			recParentVO.setPk_deptid_v(deptVO.getPk_vid());
			//摘要
			recParentVO.setScomment(payParentVO.getScomment());	
			//发货国
			recParentVO.setSendcountryid(payChildVO.getRececountryid());	
			recParentVO.setSrc_syscode(17);	//单据来源系统:17,外部交换平台
			//报税国
			recParentVO.setTaxcountryid(payChildVO.getRececountryid());
			recParentVO.setSyscode(0);	//0=应收系统，1=应付系统
			//客户
			recParentVO.setCustomer(pk_customer);	
			//组织本币汇率
			recParentVO.setRate(payChildVO.getRate());
			recParentVO.setDef1(payParentVO.getDef1());		//外系统单据号
			recParentVO.setDef2(payChildVO.getDef2());		//组织本币码洋
			recParentVO.setDef3(payChildVO.getDef1());		//原币码洋
			recParentVO.setDef4(payParentVO.getDef4());		//来源系统
			recParentVO.setDef5(payParentVO.getDef5());		//对应红冲单据号
			recParentVO.setDef6(payChildVO.getDef3());		//仓号
			recParentVO.setDef7(payChildVO.getDef4());		//仓号对应虚拟部门
			
			/**  --表体--  **/
			ReceivableBillItemVO[] recChildrenVO = new ReceivableBillItemVO[1];
			ReceivableBillItemVO recChildVO = new ReceivableBillItemVO();

			recChildVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			recChildVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			recChildVO.setBillclass("ys");
			recChildVO.setBilldate(billdate);
			recChildVO.setBusidate(billdate);
			//购销类型：1，国内销售；3出口销售
			recChildVO.setBuysellflag(1);	
			//计税金额
			recChildVO.setCaltaxmny(new UFDouble(1401L));	
			recChildVO.setCustomer(pk_customer);	//客户
			recChildVO.setDirection(1);	//方向：1=借方，-1=贷方
			recChildVO.setForcemoney(UFDouble.ZERO_DBL);
			recChildVO.setGlobalagentreceivelocal(UFDouble.ZERO_DBL);
			recChildVO.setGlobalbalance(UFDouble.ZERO_DBL);
			recChildVO.setGlobalcrebit(UFDouble.ZERO_DBL);
			recChildVO.setGlobaldebit(UFDouble.ZERO_DBL);
			recChildVO.setGlobalnotax_cre(UFDouble.ZERO_DBL);
			recChildVO.setGlobalnotax_de(UFDouble.ZERO_DBL);
			recChildVO.setGlobalrate(null);
			recChildVO.setGlobaltax_cre(UFDouble.ZERO_DBL);
			recChildVO.setGlobaltax_de(UFDouble.ZERO_DBL);
			recChildVO.setGroupagentreceivelocal(UFDouble.ZERO_DBL);
			recChildVO.setGroupbalance(UFDouble.ZERO_DBL);
			recChildVO.setGroupcrebit(null);
			recChildVO.setGroupdebit(null);
			recChildVO.setGroupnotax_cre(UFDouble.ZERO_DBL);
			recChildVO.setGroupnotax_de(UFDouble.ZERO_DBL);
			recChildVO.setGrouprate(null);
			recChildVO.setGrouptax_cre(UFDouble.ZERO_DBL);
			recChildVO.setGrouptax_de(UFDouble.ZERO_DBL);
			recChildVO.setIsrefused(UFBoolean.FALSE);
			//组织本币余额,实洋
			recChildVO.setLocal_money_bal(local_money);
			recChildVO.setLocal_money_cr(UFDouble.ZERO_DBL);
			//组织本币，实洋
			recChildVO.setLocal_money_de(local_money);
			recChildVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			recChildVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			recChildVO.setLocal_price(UFDouble.ZERO_DBL);
			recChildVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			recChildVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			recChildVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//原币余额金额，实洋
			recChildVO.setMoney_bal(money);	
			recChildVO.setMoney_cr(UFDouble.ZERO_DBL);
			//借方原币，实洋
			recChildVO.setMoney_de(money);	
			//物料
			recChildVO.setMaterial(payChildVO.getMaterial());
			recChildVO.setMaterial_src(payChildVO.getMaterial_src());
			recChildVO.setNosubtax(UFDouble.ZERO_DBL);
			recChildVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			recChildVO.setNotax_cr(UFDouble.ZERO_DBL);
			recChildVO.setNotax_de(money);
			//往来对象，默认是客户
			recChildVO.setObjtype(0);
			recChildVO.setOccupationmny(money);
			recChildVO.setPausetransact(UFBoolean.FALSE);	//	挂起标志
			recChildVO.setPk_billtype(defaultBillType);
			recChildVO.setPk_currtype(payChildVO.getPk_currtype());
			recChildVO.setPk_group(pk_group);
			recChildVO.setPk_org(pk_supplierFinanceOrg);
			recChildVO.setPk_tradetype(defaultTradeType);
			recChildVO.setPk_tradetypeid(pk_tradetypeid);
			recChildVO.setPk_deptid(deptVO.getPk_dept());
			recChildVO.setPk_deptid_v(deptVO.getPk_vid());
			recChildVO.setPrepay(0);
			recChildVO.setQuantity_bal(UFDouble.ZERO_DBL);
			recChildVO.setRate(payChildVO.getRate());
			recChildVO.setRececountryid(payParentVO.getRececountryid());	//收货国
			//摘要
			recChildVO.setScomment(payChildVO.getScomment());
			recChildVO.setSett_org(pk_supplierFinanceOrg);	//结算财务组织
			recChildVO.setStatus(2);
			recChildVO.setTaxcodeid(payChildVO.getTaxcodeid());	//税码
			recChildVO.setTaxprice(UFDouble.ZERO_DBL);	//含税单价
			//税率
			recChildVO.setTaxrate(UFDouble.ZERO_DBL);	
			//扣税类别：0=应税内含，1=应税外加
			recChildVO.setTaxtype(0);	
			recChildVO.setTriatradeflag(UFBoolean.FALSE);	//	三级贸易
			recChildVO.setDef1(payChildVO.getDef1());	//码洋
			recChildVO.setDef2(payChildVO.getDef2());	//组织本币码洋
			recChildVO.setDef3(payParentVO.getDef6());	//客户对应仓号
			recChildVO.setDef4(payParentVO.getDef7());	//仓号对应虚拟部门
			
			recChildrenVO[0] = recChildVO;
			aggReceivableBillVO.setParentVO(recParentVO);
			aggReceivableBillVO.setChildrenVO(recChildrenVO);
			list.add(aggReceivableBillVO);
		}
		
		
		return (AggReceivableBillVO[])list.toArray(new AggReceivableBillVO[0]);
	}

}
