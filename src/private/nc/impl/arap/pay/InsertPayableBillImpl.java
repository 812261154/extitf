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
			//如果审批失败，则回退删除单据
			this.initApproveInfo(vo);
			service.approve(rtVO);
		} catch (Exception e2) {
			rt = e2.getMessage();
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

}
