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
			rt.append("应收单据导入出错，问题原因：[" + e.getMessage() + "]");
		}
		
		try {
			ArapPayBillImportBO payBillBO = new ArapPayBillImportBO();
			payBillBO.payBillImport();
		} catch(Exception e) {
			rt.append("应付单据导入出错，问题原因：[" + e.getMessage() + "]");
		}
		
		try {
			VoucherImportBO voucherBO = new VoucherImportBO();
			voucherBO.voucherImort();
		} catch(Exception e) {
			rt.append("凭证导入出错，问题原因：[" + e.getMessage() + "]");
		}
		
		PreAlertObject alert = new PreAlertObject();
		alert.setReturnType(PreAlertReturnType.RETURNMESSAGE);
		alert.setReturnObj(StringUtils.isBlank(rt.toString()) ? "执行成功" : rt.toString());
		return alert;
	}

}
