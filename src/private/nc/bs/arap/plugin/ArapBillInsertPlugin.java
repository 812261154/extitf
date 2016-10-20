package nc.bs.arap.plugin;

import org.apache.commons.lang3.StringUtils;

import nc.bs.arap.gl.VoucherImportBO;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.pa.PreAlertReturnType;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
import nc.impl.arap.pay.ArapPayBillImportBO;
import nc.impl.arap.receivable.ArapRecBillImportBO;
import nc.vo.pub.BusinessException;

public class ArapBillInsertPlugin implements IBackgroundWorkPlugin {

	@Override
	public PreAlertObject executeTask(BgWorkingContext bgwc)
			throws BusinessException {
		StringBuffer rt = new StringBuffer();
		
		try {
			ArapRecBillImportBO recBillBO = new ArapRecBillImportBO();
			recBillBO.recBillImport();
		} catch(Exception e) {
			rt.append("Ӧ�յ��ݵ����������ԭ��[" + e.getMessage() + "]");
		}
		
		try {
			ArapPayBillImportBO payBillBO = new ArapPayBillImportBO();
			payBillBO.payBillImport();
		} catch(Exception e) {
			rt.append("Ӧ�����ݵ����������ԭ��[" + e.getMessage() + "]");
		}
		
		try {
			VoucherImportBO voucherBO = new VoucherImportBO();
			voucherBO.voucherImort();
		} catch(Exception e) {
			rt.append("ƾ֤�����������ԭ��[" + e.getMessage() + "]");
		}
		
		PreAlertObject alert = new PreAlertObject();
		alert.setReturnType(PreAlertReturnType.RETURNMESSAGE);
		alert.setReturnObj(StringUtils.isBlank(rt.toString()) ? "ִ�гɹ�" : rt.toString());
		return alert;
	}

}
