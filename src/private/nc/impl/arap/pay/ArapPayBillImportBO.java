package nc.impl.arap.pay;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import nc.bs.arap.util.BillUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.HttpUtils;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.payable.IInsertPayableBill;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.arap.payable.AggPayableBillVO;
import nc.vo.arap.payable.PayableBillItemVO;
import nc.vo.arap.payable.PayableBillVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.org.DeptVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

public class ArapPayBillImportBO {
	
	private BillUtils importUtils = new BillUtils();
	private String defaultGroup = "1";
	private String defaultUser = "3";
	private String defaultTradeType = "D1";
	private String defaultBillType = "F1";
	
	public void payBillImport() throws BusinessException {
		Properties p = FileUtils.getProperties("nc/bs/arap/properties/ArapWsPrams.properties");
		defaultGroup = p.getProperty("defaultGroup");
		defaultUser = p.getProperty("defaultUser");
		defaultTradeType = p.getProperty("defaultPayTradetype");
		defaultBillType = p.getProperty("defaultPayBilltype");

		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ap");
		AggPayableBillVO[] vos = arrayListToVos(list);
		Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],�ɹ�ת���������ݵ�VO" +vos.length+ "����");
		
		//������
		JSONArray rtArray = new JSONArray();
		String outterBillno = "";
		IInsertPayableBill service = (IInsertPayableBill) NCLocator.getInstance().lookup(IInsertPayableBill.class.getName());
		int successCount = 0;
		for(AggPayableBillVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			outterBillno = (String) vo.getParentVO().getAttributeValue("def1");
			rtJson.put("FROMSYS", (String) vo.getParentVO().getAttributeValue("def4"));
			rtJson.put("NUMBER", outterBillno);
			try {
				if(isBillExit(vo)) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "�õ����Ѵ��ڣ������ظ�¼��!");
					Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],����" +outterBillno+ "�Ѵ��ڣ������ظ�¼��!");
				} else {
					String rt = service.insertPayableBill_RequiresNew(vo);
					if(StringUtils.isBlank(rt)) {
						rtJson.put("COMPLETED", "1");
						rtJson.put("MESSAGE", "");
						successCount++;
					} else {
						rtJson.put("COMPLETED", "0");
						rtJson.put("MESSAGE", rt);
						Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],����" +outterBillno+ "����ʧ�ܣ�����ԭ��" + rt);
					}
				}
			} catch(Exception e3) {
				rtJson.put("COMPLETED", "0");
				rtJson.put("MESSAGE", e3.getMessage());
				Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],����" +outterBillno+ "����ʧ�ܣ�����ԭ��" + e3.getMessage());
			}
			rtArray.add(rtJson);
		}
		
		Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],����ɹ�" +successCount+ "����");
		importUtils.insertImportResult(rtArray);
		rtArray.clear();
		try {
			rtArray = BillUtils.queryImportResult();
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
	
	//���ݱ���ǰ����
	private boolean isBillExit(AggPayableBillVO vo) throws BusinessException {
		String sql = "select count(*) from AP_PAYABLEBILL t where t.dr=0 and t.def1 = '" +vo.getParentVO().getAttributeValue("def1")+ "' and t.def4 = '" +vo.getParentVO().getAttributeValue("def4")+ "'";
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer)baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (DAOException e) {
			throw new BusinessException("�����Ƿ��ظ���֤ʱ����");
		}
		return count > 0;
	}
	
	private AggPayableBillVO[] arrayListToVos(List<JSONArray> arrayList) {
		List<AggPayableBillVO> voList = new ArrayList<AggPayableBillVO>();
		JSONArray rtArray = new JSONArray();
		for(JSONArray billsArray : arrayList) {
			Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],ȡ�����ļ�����" +billsArray.size()+ "����");
			//��ŷ���ɹ��ĵ���
			String outterBillno = "";
			JSONObject bill = null;
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = BillUtils.getArapBillSourceNo(bill);
				try {
					AggPayableBillVO vo = jsonToVO(bill);
					voList.add(vo);
				} catch(Exception e3) {
					/************************************************************************/
					/**************************����PMP�ӿڷ���********************************/
					/************************************************************************/
					JSONObject rtJson = new JSONObject();
					rtJson.put("FROMSYS", bill.getJSONObject("parent").getString("def4"));
					rtJson.put("NUMBER", bill.getJSONObject("parent").getString("def1"));
					rtJson.put("COMPLETED", "0");
					rtJson.put("MESSAGE", e3.getMessage());
					rtArray.add(rtJson);
					Logger.error("[SDPG][" + ArapPayBillImportBO.class.getName() + "],����[" +outterBillno+ "]�����������ԭ��" + e3.getMessage());
				}
			}
		}
		if(rtArray.size() > 0) {
			importUtils.insertImportResult(rtArray);
			Logger.info("[SDPG][" + ArapPayBillImportBO.class.getName() + "],ʧ��" +rtArray.size()+ "����");
		}
		return (AggPayableBillVO[])voList.toArray(new AggPayableBillVO[0]);
	}
	
	//��json��ʽ�Ĳ���ת����vo
	private AggPayableBillVO jsonToVO(JSONObject bill) throws BusinessException {
		
		AggPayableBillVO aggPayableBillVO = new AggPayableBillVO();
		PayableBillVO parentVO = new PayableBillVO();
		
		/**  --��ͷ--  **/
		JSONObject parent = bill.getJSONObject("parent");
		UFDate billdate = new UFDate(parent.getString("billdate"));
		String pk_group = importUtils.getCurrGroupPk(defaultGroup);
		String pk_org = importUtils.getOrgByCode(pk_group, parent.getString("pk_org")).getPrimaryKey();
		String pk_user = importUtils.getInitUserPkByCode(defaultUser);
		String pk_tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, pk_group);
		String pk_currtype = importUtils.getCurrtypeByCode(parent.getString("pk_currtype")).getPrimaryKey();
		//�ջ���
		String rececountryid = importUtils.getCountryId(pk_org);
		String scomment = parent.getString("scomment");
		String outterSysNum = parent.getString("def1");
		String outterSys = parent.getString("def4");
		DeptVO deptVO = importUtils.getDeptVOByCode(pk_group, pk_org, parent.getString("def7"));
		String supplier = "";
		UFDouble rate = null;
		
		
		parentVO.setApprovestatus(3);	//����״̬:1=ͨ��̬,3=�ύ̬
		parentVO.setBillstatus(-1);		//����״̬:1=����ͨ����-1=����
		parentVO.setEffectstatus(0);  	//��Ч״̬��10=����Ч��0=δ��Ч
		
		
		parentVO.setBillclass("yf");
		parentVO.setBilldate(billdate);
		parentVO.setBillmaker(pk_user);
		parentVO.setCreationtime(new UFDateTime());
		parentVO.setCreator(pk_user);
		parentVO.setGloballocal(UFDouble.ZERO_DBL);
		parentVO.setGlobalnotax(UFDouble.ZERO_DBL);
		parentVO.setGlobalrate(UFDouble.ZERO_DBL);
		parentVO.setGlobaltax(UFDouble.ZERO_DBL);
		parentVO.setGloballocal(UFDouble.ZERO_DBL);
		parentVO.setGroupnotax(UFDouble.ZERO_DBL);
		parentVO.setGrouprate(UFDouble.ZERO_DBL);
		parentVO.setGrouptax(UFDouble.ZERO_DBL);
		parentVO.setIsflowbill(UFBoolean.FALSE);
		parentVO.setIsinit(UFBoolean.FALSE);
		parentVO.setIsreded(UFBoolean.FALSE);
		//����ʵ��
		parentVO.setLocal_money(new UFDouble(parent.getDouble("local_money")));
		//?
		parentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
		//ԭ��ʵ��
		parentVO.setMoney(new UFDouble(parent.getDouble("money")));	
		//��������0���ͻ�
		parentVO.setObjtype(0);	
		//�������ͱ���
		parentVO.setPk_billtype(defaultBillType);	
		//����
		parentVO.setPk_currtype(pk_currtype);	
		//����������֯
		parentVO.setPk_fiorg(pk_org);	
		parentVO.setPk_group(pk_group);
		//������֯
		parentVO.setPk_org(pk_org);	
		parentVO.setPk_tradetype(defaultTradeType);	//Ӧ������code
		parentVO.setPk_tradetypeid(pk_tradetypeid);
		parentVO.setPk_deptid(deptVO.getPk_dept());
		parentVO.setPk_deptid_v(deptVO.getPk_vid());
		//ժҪ
		parentVO.setScomment(scomment);	
		//�ջ���
		parentVO.setRececountryid(rececountryid);
		parentVO.setSrc_syscode(17);	//������Դϵͳ:17,�ⲿ����ƽ̨
		//��˰��
		parentVO.setTaxcountryid(rececountryid);
		parentVO.setSyscode(1);	//0=Ӧ��ϵͳ��1=Ӧ��ϵͳ
		parentVO.setDef1(outterSysNum);				//��ϵͳ���ݺ�
		parentVO.setDef2(parent.getString("def2"));	//��������
		parentVO.setDef3(parent.getString("def3"));	//ԭ������
		parentVO.setDef4(outterSys);				//��Դϵͳ
		parentVO.setDef5(parent.getString("def5"));	//��Ӧ��嵥�ݺ�
		parentVO.setDef6(parent.getString("def6"));	//�ֺ�
		parentVO.setDef7(parent.getString("def7"));	//�ֺŶ�Ӧ���ⲿ��
		parentVO.setDef8(parent.getString("def8"));	//��Ӧ�̷�������
		parentVO.setDef14(StringUtils.equalsIgnoreCase(bill.getString("needarap"), "Y") ? "Y" : "N");
		
		/**  --����--  **/
		JSONArray children = bill.getJSONArray("children");
		
		PayableBillItemVO[] childrenVO = new PayableBillItemVO[children.size()];
		for(int i=0; i<children.size(); i++) {
			
			JSONObject child = children.getJSONObject(i);
			UFDouble money_cr = new UFDouble(child.getString("money_cr"));
			UFDouble local_money_cr = new UFDouble(child.getString("local_money_cr"));
			SupplierVO supplierVO = importUtils.getSupplierVOByCode(outterSys, child.getString("supplier"), pk_group, pk_org);
			String pk_material = importUtils.getMaterialPkByCode(child.getString("material"), pk_group, pk_org);
			String taxcodeid = importUtils.getTaxcodePkByCode(supplierVO.getPk_country(), child.getString("material"));
			UFDouble childRate = new UFDouble(child.getString("rate"));
			if(i == 0) {
				supplier = supplierVO.getPrimaryKey();
				rate = childRate;
			}
			
			PayableBillItemVO childVO = new PayableBillItemVO();
			childVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			childVO.setBillclass("yf");
			childVO.setBilldate(billdate);
			childVO.setBusidate(billdate);
			//�������ͣ�1���������ۣ�3��������
			childVO.setBuysellflag(1);	
			//��˰���
			childVO.setCaltaxmny(new UFDouble(1401L));	
			//����1=�跽��-1=����
			childVO.setDirection(-1);	
			childVO.setForcemoney(UFDouble.ZERO_DBL);
			childVO.setGlobalagentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setGlobalbalance(UFDouble.ZERO_DBL);
			childVO.setGlobalcrebit(UFDouble.ZERO_DBL);
			childVO.setGlobaldebit(UFDouble.ZERO_DBL);
			childVO.setGlobalnotax_cre(UFDouble.ZERO_DBL);
			childVO.setGlobalnotax_de(UFDouble.ZERO_DBL);
			childVO.setGlobalrate(null);
			childVO.setGlobaltax_cre(UFDouble.ZERO_DBL);
			childVO.setGlobaltax_de(UFDouble.ZERO_DBL);
			childVO.setGroupagentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setGroupbalance(UFDouble.ZERO_DBL);
			childVO.setGroupcrebit(null);
			childVO.setGroupdebit(null);
			childVO.setGroupnotax_cre(UFDouble.ZERO_DBL);
			childVO.setGroupnotax_de(UFDouble.ZERO_DBL);
			childVO.setGrouprate(null);
			childVO.setGrouptax_cre(UFDouble.ZERO_DBL);
			childVO.setGrouptax_de(UFDouble.ZERO_DBL);
			childVO.setIsrefused(UFBoolean.FALSE);
			//��֯�������,ʵ��
			childVO.setLocal_money_bal(local_money_cr);
			//������֯���ң�ʵ��
			childVO.setLocal_money_cr(local_money_cr);
			childVO.setLocal_money_de(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_price(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//ԭ������ʵ��
			childVO.setMoney_bal(money_cr);	
			//����ԭ�ң�ʵ��
			childVO.setMoney_cr(money_cr);
			childVO.setMoney_de(UFDouble.ZERO_DBL);	
			//����
			childVO.setMaterial(pk_material);
			childVO.setMaterial_src(pk_material);
			childVO.setNosubtax(UFDouble.ZERO_DBL);
			childVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			childVO.setNotax_cr(money_cr);
			childVO.setNotax_de(UFDouble.ZERO_DBL);
			//��������Ĭ���ǹ�Ӧ��
			childVO.setObjtype(1);
			//��Ӧ��
			childVO.setSupplier(supplierVO.getPrimaryKey());
			childVO.setOccupationmny(money_cr);
			childVO.setPausetransact(UFBoolean.FALSE);	//	�����־
			childVO.setPk_billtype(defaultBillType);
			childVO.setPk_currtype(pk_currtype);
			childVO.setPk_group(pk_group);
			childVO.setPk_org(pk_org);
			childVO.setPk_tradetype(defaultTradeType);
			childVO.setPk_tradetypeid(pk_tradetypeid);
			childVO.setPk_deptid(deptVO.getPk_dept());
			childVO.setPk_deptid_v(deptVO.getPk_vid());
			childVO.setPrepay(0);
			childVO.setQuantity_bal(UFDouble.ZERO_DBL);
			childVO.setRate(childRate);
			//������
			childVO.setSendcountryid(supplierVO.getPk_country());
			//ժҪ
			childVO.setScomment(scomment);
			childVO.setSett_org(pk_org);	//���������֯
			childVO.setStatus(2);
			childVO.setTaxcodeid(taxcodeid);	//˰��
			childVO.setTaxprice(UFDouble.ZERO_DBL);	//��˰����
			//˰��
			childVO.setTaxrate(UFDouble.ZERO_DBL);	
			//��˰���0=Ӧ˰�ں���1=Ӧ˰���
			childVO.setTaxtype(0);	
			childVO.setTriatradeflag(UFBoolean.FALSE);	//	����ó��
			childVO.setDef1(child.getString("def1"));	//ԭ������
			childVO.setDef2(child.getString("def2"));	//��֯��������
			childVO.setDef3(child.getString("def3"));	//��Ӧ�̶�Ӧ�ֺ�
			childVO.setDef4(child.getString("def4"));	//�ֺŶ�Ӧ����
			
			childrenVO[i] = childVO;
		}
		
		/** ��ͷ���������  **/
		//�ͻ�
		parentVO.setSupplier(supplier);
		//��֯���һ���
		parentVO.setRate(rate);	
		
		aggPayableBillVO.setParentVO(parentVO);
		aggPayableBillVO.setChildrenVO(childrenVO);
		return aggPayableBillVO;
	}

}
