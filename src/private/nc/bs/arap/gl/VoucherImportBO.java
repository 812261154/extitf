package nc.bs.arap.gl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.bs.arap.util.TransUtils;
import nc.bs.arap.util.DateUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.HttpUtils;
import nc.bs.framework.common.NCLocator;
import nc.bs.gl.voucher.VoucherExtendDMO;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.gl.IInsertVoucher;
import nc.itf.gl.pub.IFreevaluePub;
import nc.itf.gl.voucher.IVoucherNo;
import nc.vo.bd.accassitem.AccAssItemVO;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccAssVO;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.bd.vouchertype.VoucherTypeVO;
import nc.vo.fipub.freevalue.Module;
import nc.vo.fipub.freevalue.util.BigAssMD5Util;
import nc.vo.gl.pubvoucher.DetailVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.glcom.ass.AssVO;
import nc.vo.org.AccountingBookVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

public class VoucherImportBO {
	
	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "group1";
	
	public void voucherImort() throws BusinessException {
		Map<String, String> defaultParam = importUtils.getDefaultParam("gl");
		defaultGroup = defaultParam.get("default_group");
		defaultUser = defaultParam.get("default_user");
		
		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/gl");
		VoucherVO[] vos = arrayListToVos(list);
		
		//导入结果
		JSONArray rtArray = new JSONArray();
		IInsertVoucher service = NCLocator.getInstance().lookup(IInsertVoucher.class);
		for(VoucherVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			rtJson.put("FROMSYS", vo.getFree3());
			rtJson.put("NUMBER", vo.getFree4());
			try {
				if(importUtils.isBillExit(getBillExitSql(vo))) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "该单据已存在，不可重复录入!");
				} else {
					String rt = service.insertVoucher_RequiresNew(vo);
					if(StringUtils.isBlank(rt)) {
						rtJson.put("COMPLETED", "1");
						rtJson.put("MESSAGE", "");
					} else {
						rtJson.put("COMPLETED", "0");
						rtJson.put("MESSAGE", rt);
					}
				}
			} catch (Exception e) {
				rtJson.put("COMPLETED", "0");
				rtJson.put("MESSAGE", e.getMessage());
			}
			rtArray.add(rtJson);
		}
		
		importUtils.insertImportResult(rtArray);
		rtArray.clear();
		try {
			rtArray = TransUtils.queryImportResult();
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
	
	private VoucherVO[] arrayListToVos(List<JSONArray> arrayList) {
		List<VoucherVO> voList = new ArrayList<VoucherVO>();
		JSONArray rtArray = new JSONArray();
		for(JSONArray billsArray : arrayList) {
			//存放翻译失败的单据
			String outterBillno = "";
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = TransUtils.getVoucherBillNo(bill);
				try {
					VoucherVO vo = jsonToVO(bill);
					voList.add(vo);
				} catch(Exception e3) {
					/************************************************************************/
					/**************************调用PMP接口返回********************************/
					/************************************************************************/
					JSONObject rtJson = new JSONObject();
					rtJson.put("FROMSYS", bill.getString("free3"));
					rtJson.put("NUMBER", outterBillno);
					rtJson.put("COMPLETED", "0");
					rtJson.put("MESSAGE", e3.getMessage());
					rtArray.add(rtJson);
					Logger.error("单据[" +outterBillno+ "]导入出错，问题原因：" + e3.getMessage() + "\n");
				}
			}
		}
		if(rtArray.size() > 0) {
			importUtils.insertImportResult(rtArray);
		}
		return (VoucherVO[])voList.toArray(new VoucherVO[0]);
	}
	
	private VoucherVO jsonToVO(JSONObject json) throws BusinessException, JSONException {
		VoucherVO vo = new VoucherVO();
		String pk_group = importUtils.getCurrGroupPk(defaultGroup);
		String pk_user = importUtils.getInitUserPkByCode(defaultUser);
		OrgVO orgVO = importUtils.getOrgByCode(pk_group, json.getString("org"));
		AccountingBookVO accountingBookVO = importUtils.getAccountingBook(pk_group, orgVO.getPk_org());
		VoucherTypeVO voucherTypeVO = importUtils.getVoucherType(pk_group, orgVO.getPk_org(), json.getString("vouchertype"));
		String prepareddate = json.getString("prepareddate");
		
		vo.setBillmaker(pk_user);
		vo.setCreationtime(new UFDateTime());
		vo.setCreator(pk_user);
		vo.setHasCashflowModified(false);
		vo.setHasRefVerify(false);
		vo.setIsdifflag(UFBoolean.FALSE);
		vo.setIsmodelrecflag(UFBoolean.FALSE);
		vo.setIsOffer(UFBoolean.FALSE);
		vo.setAttachment(0);
		vo.setDetailmodflag(UFBoolean.TRUE);
		vo.setDiscardflag(UFBoolean.FALSE);
		vo.setDirty(false);
		vo.setModifyflag("YYY");
		vo.setPrepareddate(new UFDate(prepareddate));
		vo.setYear(DateUtils.getYearFromStr(prepareddate));
		vo.setPeriod(DateUtils.getMonthFromStr(prepareddate));
		vo.setPk_accountingbook(accountingBookVO.getPk_accountingbook());
		vo.setPk_group(pk_group);
		vo.setPk_org(orgVO.getPk_org());
		vo.setPk_org_v(orgVO.getPk_vid());
		vo.setPk_prepared(pk_user);
		vo.setPk_system("GL");
		vo.setPk_vouchertype(voucherTypeVO.getPk_vouchertype());
		vo.setVouchertypename(voucherTypeVO.getName());
		vo.setVoucherkind(0);
		vo.setTempsaveflag(UFBoolean.FALSE);
		vo.setFree3(json.getString("free3"));
		vo.setFree4(json.getString("free4"));
		
		Vector<DetailVO> details = new Vector<DetailVO>();
		JSONArray detailsArray = json.getJSONArray("details");
		for(int i=0; i<detailsArray.size(); i++) {
			JSONObject detailJson = detailsArray.getJSONObject(i);
			CurrtypeVO currtypeVO = importUtils.getCurrtypeByCode(detailJson.getString("currtype"));
			AccountVO accountVO = importUtils.getAccountVOByCode(accountingBookVO.getPk_accountingbook(), detailJson.getString("accsubj"), prepareddate);
			UFDouble amount = new UFDouble(detailJson.getString("amount"));
			DetailVO dvo = new DetailVO();
			dvo.setAccsubjcode(accountVO.getCode());
			dvo.setPk_accasoa(accountVO.getPk_accasoa());
			dvo.setAccsubjname(accountVO.getName());
			dvo.setCurrtypecode(currtypeVO.getCode());
			dvo.setPk_currtype(currtypeVO.getPk_currtype());
			dvo.setDetailindex(i+1);
			dvo.setExcrate2(new UFDouble(1));
			dvo.setExplanation(detailJson.getString("explanation"));
			dvo.setModifyflag("YYYYYYYYYYYYYYYYYYYYYYY");
			dvo.setPk_accountingbook(accountingBookVO.getPk_accountingbook());
			dvo.setPk_glbook(accountingBookVO.getPk_setofbook());
			dvo.setTempsaveflag(UFBoolean.FALSE);
			dvo.setPk_group(pk_group);
			dvo.setPk_glorg(orgVO.getPk_org());
			dvo.setPk_org(orgVO.getPk_org());
			dvo.setPk_org_v(orgVO.getPk_vid());
			dvo.setPk_unit(orgVO.getPk_org());
			dvo.setPk_unit_v(orgVO.getPk_vid());
			dvo.setPk_vouchertype(voucherTypeVO.getPk_vouchertype());
			dvo.setPrepareddate(new UFDate(prepareddate));
			dvo.setStatus(0);
			dvo.setAss(getAssVO(accountVO.getAccass(), detailJson.getJSONObject("ass"), pk_group, orgVO.getPk_org()));
			if(detailJson.getInt("direction") == 0) {
				dvo.setDebitamount(amount);
				dvo.setLocaldebitamount(amount);
			} else {
				dvo.setCreditamount(amount);
				dvo.setLocalcreditamount(amount);
			}
			String smd5 = new BigAssMD5Util().getMD5ByAssvos(dvo.getAss(), pk_group);
			String assid = NCLocator.getInstance().lookup(IFreevaluePub.class).getAssID_RequiresNew(dvo.getAss(), true, smd5, pk_group, Module.GL);
			dvo.setAssid(assid);
			details.add(dvo);
			
			if(i == 0) {
				vo.setExplanation(dvo.getExplanation());
			} 
		}
		vo.setDetail(details);
		
		return vo;
	} 
	
	private AssVO[] getAssVO(AccAssVO[] accAssVOs, JSONObject ass, String pk_group, String pk_org) throws BusinessException {
		AssVO[] assVOs = new AssVO[accAssVOs.length];
		for(int i=0; i<accAssVOs.length; i++) {
			assVOs[i] = new AssVO();
			assVOs[i].setUserData(true);
			assVOs[i].setPk_Checktype(accAssVOs[i].getPk_entity());
			AccAssItemVO accAssItemVO = importUtils.getAccassItemVO(accAssVOs[i].getPk_entity());
			IBDData ibdata = importUtils.getDocByCode(accAssItemVO.getClassid(), pk_group, pk_org, ass.getString(accAssItemVO.getCode()));
			assVOs[i].setPk_Checkvalue(ibdata.getPk());
		}
		return assVOs;
	}
	
	private String getBillExitSql(VoucherVO vo) {
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(*) from gl_voucher t ");
		sql.append(" where t.dr=0 ");
		sql.append(" and t.free3 = '" +vo.getFree3()+ "' ");
		sql.append(" and t.free4 = '" +vo.getFree4()+ "' ");
		return sql.toString();
	}
}
