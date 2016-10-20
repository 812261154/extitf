package nc.impl.arap.pay;

import nc.bs.framework.common.NCLocator;
import nc.itf.arap.payable.IInsertPayableBill;
import nc.pubitf.arap.payable.IArapPayableBillPubService;
import nc.vo.arap.payable.AggPayableBillVO;
import nc.vo.arap.payable.PayableBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

public class InsertPayableBillImpl implements
		IInsertPayableBill {

	@Override
	public String insertPayableBill_RequiresNew(AggPayableBillVO vo) {
		String rt = "";
		IArapPayableBillPubService service = (IArapPayableBillPubService) NCLocator.getInstance().lookup(IArapPayableBillPubService.class.getName());
		try {
			AggPayableBillVO rtVO = service.save(vo);
			//�������ʧ�ܣ������ɾ������
			this.initApproveInfo(vo);
			service.approve(rtVO);
		} catch (Exception e2) {
			rt = e2.getMessage();
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

}
