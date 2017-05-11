package nc.bs.arap.gl;

import nc.bs.framework.common.NCLocator;
import nc.itf.arap.gl.IInsertVoucher;
import nc.itf.gl.voucher.IVoucher;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.pub.BusinessException;

public class InsertVoucherImpl implements IInsertVoucher {

	@Override
	public String insertVoucher_RequiresNew(VoucherVO vo) {
		String rt = "";
		try {
			IVoucher voucherService = NCLocator.getInstance().lookup(IVoucher.class);
			voucherService.save(vo, true);
		} catch (BusinessException e) {
			rt = e.getMessage();
		}
		return rt;
	}

}
