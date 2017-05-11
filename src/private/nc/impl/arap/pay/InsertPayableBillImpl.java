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
			
			//��Ҫ��Ӧ��
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
	
	//��ʼ�������Ϣ
	private void initApproveInfo(AggPayableBillVO vo) throws BusinessException {
		PayableBillVO parentVO = (PayableBillVO)vo.getParentVO();
		parentVO.setApprovestatus(1);	//����״̬:1=ͨ��̬
		parentVO.setBillstatus(1);		//����״̬:1=����ͨ��
		parentVO.setApprover(parentVO.getBillmaker());		//�����
		parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//�������
		parentVO.setEffectstatus(10);						//��Ч״̬��10=����Ч��0=δ��Ч
		parentVO.setEffectdate(new UFDate());				//��Ч����
		parentVO.setEffectuser(parentVO.getBillmaker());		//��Ч��
	}
	
	//��ʼ�������Ϣ
	private void initApproveInfo(AggReceivableBillVO[] vos) throws BusinessException {
		for(AggReceivableBillVO vo : vos) {
			ReceivableBillVO parentVO = (ReceivableBillVO)vo.getParentVO();
			parentVO.setApprovestatus(1);	//����״̬:1=ͨ��̬
			parentVO.setBillstatus(1);		//����״̬:1=����ͨ��
			parentVO.setApprover(parentVO.getBillmaker());		//�����
			parentVO.setApprovedate(new UFDateTime(parentVO.getBilldate().toDate()));			//�������
			parentVO.setEffectstatus(10);						//��Ч״̬��10=����Ч��0=δ��Ч
			parentVO.setEffectdate(new UFDate());				//��Ч����
			parentVO.setEffectuser(parentVO.getBillmaker());		//��Ч��
		}
	}
	
	//Ӧ����תӦ�յ�
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
			
			
			recParentVO.setApprovestatus(3);	//����״̬:1=ͨ��̬,3=�ύ̬
			recParentVO.setBillstatus(-1);		//����״̬:1=����ͨ����-1=����
			recParentVO.setEffectstatus(0);  	//��Ч״̬��10=����Ч��0=δ��Ч
			
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
			//����ʵ��
			recParentVO.setLocal_money(local_money);
			//?
			recParentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
			//ԭ��ʵ��
			recParentVO.setMoney(money);	
			//��������0���ͻ�
			recParentVO.setObjtype(0);	
			//��������
			recParentVO.setPk_billtype(defaultBillType);	
			//����
			recParentVO.setPk_currtype(payChildVO.getPk_currtype());	
			//����������֯
			recParentVO.setPk_fiorg(pk_supplierFinanceOrg);	
			recParentVO.setPk_group(pk_group);
			//������֯
			recParentVO.setPk_org(pk_supplierFinanceOrg);	
			recParentVO.setPk_tradetype(defaultTradeType);	//Ӧ������code
			recParentVO.setPk_tradetypeid(pk_tradetypeid);
			recParentVO.setPk_deptid(deptVO.getPk_dept());
			recParentVO.setPk_deptid_v(deptVO.getPk_vid());
			//ժҪ
			recParentVO.setScomment(payParentVO.getScomment());	
			//������
			recParentVO.setSendcountryid(payChildVO.getRececountryid());	
			recParentVO.setSrc_syscode(17);	//������Դϵͳ:17,�ⲿ����ƽ̨
			//��˰��
			recParentVO.setTaxcountryid(payChildVO.getRececountryid());
			recParentVO.setSyscode(0);	//0=Ӧ��ϵͳ��1=Ӧ��ϵͳ
			//�ͻ�
			recParentVO.setCustomer(pk_customer);	
			//��֯���һ���
			recParentVO.setRate(payChildVO.getRate());
			recParentVO.setDef1(payParentVO.getDef1());		//��ϵͳ���ݺ�
			recParentVO.setDef2(payChildVO.getDef2());		//��֯��������
			recParentVO.setDef3(payChildVO.getDef1());		//ԭ������
			recParentVO.setDef4(payParentVO.getDef4());		//��Դϵͳ
			recParentVO.setDef5(payParentVO.getDef5());		//��Ӧ��嵥�ݺ�
			recParentVO.setDef6(payChildVO.getDef3());		//�ֺ�
			recParentVO.setDef7(payChildVO.getDef4());		//�ֺŶ�Ӧ���ⲿ��
			
			/**  --����--  **/
			ReceivableBillItemVO[] recChildrenVO = new ReceivableBillItemVO[1];
			ReceivableBillItemVO recChildVO = new ReceivableBillItemVO();

			recChildVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			recChildVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			recChildVO.setBillclass("ys");
			recChildVO.setBilldate(billdate);
			recChildVO.setBusidate(billdate);
			//�������ͣ�1���������ۣ�3��������
			recChildVO.setBuysellflag(1);	
			//��˰���
			recChildVO.setCaltaxmny(new UFDouble(1401L));	
			recChildVO.setCustomer(pk_customer);	//�ͻ�
			recChildVO.setDirection(1);	//����1=�跽��-1=����
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
			//��֯�������,ʵ��
			recChildVO.setLocal_money_bal(local_money);
			recChildVO.setLocal_money_cr(UFDouble.ZERO_DBL);
			//��֯���ң�ʵ��
			recChildVO.setLocal_money_de(local_money);
			recChildVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			recChildVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			recChildVO.setLocal_price(UFDouble.ZERO_DBL);
			recChildVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			recChildVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			recChildVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//ԭ������ʵ��
			recChildVO.setMoney_bal(money);	
			recChildVO.setMoney_cr(UFDouble.ZERO_DBL);
			//�跽ԭ�ң�ʵ��
			recChildVO.setMoney_de(money);	
			//����
			recChildVO.setMaterial(payChildVO.getMaterial());
			recChildVO.setMaterial_src(payChildVO.getMaterial_src());
			recChildVO.setNosubtax(UFDouble.ZERO_DBL);
			recChildVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			recChildVO.setNotax_cr(UFDouble.ZERO_DBL);
			recChildVO.setNotax_de(money);
			//��������Ĭ���ǿͻ�
			recChildVO.setObjtype(0);
			recChildVO.setOccupationmny(money);
			recChildVO.setPausetransact(UFBoolean.FALSE);	//	�����־
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
			recChildVO.setRececountryid(payParentVO.getRececountryid());	//�ջ���
			//ժҪ
			recChildVO.setScomment(payChildVO.getScomment());
			recChildVO.setSett_org(pk_supplierFinanceOrg);	//���������֯
			recChildVO.setStatus(2);
			recChildVO.setTaxcodeid(payChildVO.getTaxcodeid());	//˰��
			recChildVO.setTaxprice(UFDouble.ZERO_DBL);	//��˰����
			//˰��
			recChildVO.setTaxrate(UFDouble.ZERO_DBL);	
			//��˰���0=Ӧ˰�ں���1=Ӧ˰���
			recChildVO.setTaxtype(0);	
			recChildVO.setTriatradeflag(UFBoolean.FALSE);	//	����ó��
			recChildVO.setDef1(payChildVO.getDef1());	//����
			recChildVO.setDef2(payChildVO.getDef2());	//��֯��������
			recChildVO.setDef3(payParentVO.getDef6());	//�ͻ���Ӧ�ֺ�
			recChildVO.setDef4(payParentVO.getDef7());	//�ֺŶ�Ӧ���ⲿ��
			
			recChildrenVO[0] = recChildVO;
			aggReceivableBillVO.setParentVO(recParentVO);
			aggReceivableBillVO.setChildrenVO(recChildrenVO);
			list.add(aggReceivableBillVO);
		}
		
		
		return (AggReceivableBillVO[])list.toArray(new AggReceivableBillVO[0]);
	}

}
