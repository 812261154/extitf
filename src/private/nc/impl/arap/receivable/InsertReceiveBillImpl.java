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
			//��Ҫ��Ӧ��
			if(StringUtils.equals((String)vo.getParentVO().getAttributeValue("def14"), "Y")) {
				AggPayableBillVO[] payVos = recToPayBill(vo);
				IArapPayableBillPubService payService = (IArapPayableBillPubService) NCLocator.getInstance().lookup(IArapPayableBillPubService.class.getName());
				AggPayableBillVO[] payRtVO = payService.save(payVos);
				initApproveInfo(payRtVO);
				payService.approve(payRtVO);
			}
			
			//�Ź�ϵͳ����ǵķ����������˵��������ű�������ǵ�Ӧ�գ���ǶԼ��ű�����Ӧ������ǶԿͻ���Ӧ��
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
	
	//��ʼ�������Ϣ
	private void initApproveInfo(AggReceivableBillVO vo) throws BusinessException {
		ReceivableBillVO parentVO = (ReceivableBillVO)vo.getParentVO();
		parentVO.setApprovestatus(1);	//����״̬:1=ͨ��̬
		parentVO.setBillstatus(1);		//����״̬:1=����ͨ��
		parentVO.setApprover(parentVO.getBillmaker());		//�����
		parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//�������
		parentVO.setEffectstatus(10);						//��Ч״̬��10=����Ч��0=δ��Ч
		parentVO.setEffectdate(new UFDate());				//��Ч����
		parentVO.setEffectuser(parentVO.getBillmaker());		//��Ч��
	}
	
	//��ʼ�������Ϣ
	private void initApproveInfo(AggPayableBillVO[] vos) throws BusinessException {
		for(AggPayableBillVO vo : vos) {
			PayableBillVO parentVO = (PayableBillVO)vo.getParentVO();
			parentVO.setApprovestatus(1);	//����״̬:1=ͨ��̬
			parentVO.setBillstatus(1);		//����״̬:1=����ͨ��
			parentVO.setApprover(parentVO.getBillmaker());		//�����
			parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//�������
			parentVO.setEffectstatus(10);						//��Ч״̬��10=����Ч��0=δ��Ч
			parentVO.setEffectdate(new UFDate());				//��Ч����
			parentVO.setEffectuser(parentVO.getBillmaker());		//��Ч��
		}
	}
	
	//Ӧ�յ�voת����Ӧ����vo
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
			
			payParentVO.setApprovestatus(3);	//����״̬:1=ͨ��̬,3=�ύ̬
			payParentVO.setBillstatus(-1);		//����״̬:1=����ͨ����-1=����
			payParentVO.setEffectstatus(0);  	//��Ч״̬��10=����Ч��0=δ��Ч
			
			//��ϵͳ���ݺ�
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
			//����ʵ��
			payParentVO.setLocal_money(recChildVO.getLocal_money_de());
			//��������
			payParentVO.setDef2(recChildVO.getDef2());
			//?
			payParentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
			//ԭ��ʵ��
			payParentVO.setMoney(recChildVO.getMoney_de());	
			//ԭ������
			payParentVO.setDef3(recChildVO.getDef1());
			//��������0���ͻ�
			payParentVO.setObjtype(0);	
			//�������ͱ���
			payParentVO.setPk_billtype(defaultBillType);	
			//����
			payParentVO.setPk_currtype(recChildVO.getPk_currtype());	
			//����������֯
			payParentVO.setPk_fiorg(pk_org);	
			payParentVO.setPk_group(pk_group);
			//������֯
			payParentVO.setPk_org(pk_org);	
			payParentVO.setPk_tradetype(defaultTradeType);	//Ӧ������code
			payParentVO.setPk_tradetypeid(tradetypeid);
			//��֯���һ���
			payParentVO.setRate(recChildVO.getRate());	
			//ժҪ
			payParentVO.setScomment(recParentVO.getScomment());	
			//��Ӧ��
			payParentVO.setSupplier(pk_supplier);	
			//�ջ���
			payParentVO.setRececountryid(recChildVO.getRececountryid());
			payParentVO.setSrc_syscode(17);	//������Դϵͳ:17,�ⲿ����ƽ̨
			//��˰��
			payParentVO.setTaxcountryid(recChildVO.getRececountryid());
			payParentVO.setSyscode(1);	//0=Ӧ��ϵͳ��1=Ӧ��ϵͳ
			//��Դϵͳ
			payParentVO.setDef4(recParentVO.getDef4());
			//��Ӧ��嵥�ݺ�
			payParentVO.setDef5(recParentVO.getDef5());
			
			
			PayableBillItemVO payChildVO = new PayableBillItemVO();
			payChildVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			payChildVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			payChildVO.setBillclass("yf");
			payChildVO.setBilldate(billdate);
			payChildVO.setBusidate(billdate);
			//�������ͣ�2=���ڲɹ���4=���ڲɹ���
			payChildVO.setBuysellflag(2);
			//��˰���
			payChildVO.setCaltaxmny(new UFDouble(1401L));	
			//����1=�跽��-1=����
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
			//��֯�������,ʵ��
			payChildVO.setLocal_money_bal(recChildVO.getLocal_money_bal());
			//������֯���ң�ʵ��
			payChildVO.setLocal_money_cr(recChildVO.getLocal_money_de());
			//��������
			payChildVO.setDef2(recChildVO.getDef2());
			payChildVO.setLocal_money_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			payChildVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_price(UFDouble.ZERO_DBL);
			payChildVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			payChildVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			payChildVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//ԭ������ʵ��
			payChildVO.setMoney_bal(recChildVO.getMoney_bal());	
			//����ԭ�ң�ʵ��
			payChildVO.setMoney_cr(recChildVO.getMoney_de());
			//ԭ������
			payChildVO.setDef1(recChildVO.getDef1());
			payChildVO.setMoney_de(UFDouble.ZERO_DBL);	
			//����
			payChildVO.setMaterial(recChildVO.getMaterial());
			payChildVO.setMaterial_src(recChildVO.getMaterial_src());
			payChildVO.setNosubtax(UFDouble.ZERO_DBL);
			payChildVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			payChildVO.setNotax_cr(recChildVO.getNotax_de());
			payChildVO.setNotax_de(UFDouble.ZERO_DBL);
			//��������Ĭ���ǹ�Ӧ��
			payChildVO.setObjtype(1);
			//��Ӧ��
			payChildVO.setSupplier(pk_supplier);
			//�ֺ�
			payChildVO.setDef3(payParentVO.getDef7());
			payChildVO.setOccupationmny(recChildVO.getOccupationmny());
			payChildVO.setPausetransact(UFBoolean.FALSE);	//	�����־
			payChildVO.setPk_billtype(defaultBillType);
			payChildVO.setPk_currtype(recChildVO.getPk_currtype());
			//����
			payChildVO.setPk_deptid(deptVO.getPk_dept());
			payChildVO.setPk_deptid_v(deptVO.getPk_vid());
			payChildVO.setPk_group(pk_group);
			payChildVO.setPk_org(pk_org);
			payChildVO.setPk_tradetype(defaultTradeType);
			payChildVO.setPk_tradetypeid(tradetypeid);
			payChildVO.setPrepay(0);
			payChildVO.setQuantity_bal(UFDouble.ZERO_DBL);
			payChildVO.setRate(recChildVO.getRate());
			//������
			payChildVO.setSendcountryid(payParentVO.getSendcountryid());
			//ժҪ
			payChildVO.setScomment(recChildVO.getScomment());
			payChildVO.setSett_org(pk_org);	//���������֯
			payChildVO.setStatus(2);
			payChildVO.setTaxcodeid(recChildVO.getTaxcodeid());	//˰��
			payChildVO.setTaxprice(UFDouble.ZERO_DBL);	//��˰����
			//˰��
			payChildVO.setTaxrate(UFDouble.ZERO_DBL);	
			//��˰���0=Ӧ˰�ں���1=Ӧ˰���
			payChildVO.setTaxtype(0);	
			payChildVO.setTriatradeflag(UFBoolean.FALSE);	//	����ó��
			
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
