package nc.impl.arap.receivable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nc.bs.arap.util.TransUtils;
import nc.bs.arap.util.FileUtils;
import nc.bs.arap.util.HttpUtils;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.fi.arap.pubutil.RuntimeEnv;
import nc.itf.arap.receivable.IInsertReceiveBill;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.arap.receivable.AggReceivableBillVO;
import nc.vo.arap.receivable.ReceivableBillItemVO;
import nc.vo.arap.receivable.ReceivableBillVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

public class ArapRecBillImportBO {

	private TransUtils importUtils = new TransUtils();
	private String defaultGroup = "1";
	private String defaultUser = "3";
	private String defaultTradeType = "D0";
	private String defaultBillType = "F0";
	
	public void recBillImport() throws BusinessException {
		Map<String, String> defaultParam = importUtils.getDefaultParam("ar");
		defaultGroup = defaultParam.get("default_group");
		defaultUser = defaultParam.get("default_user");
		defaultTradeType = defaultParam.get("default_tradetype");
		defaultBillType = defaultParam.get("default_billtype");
		
		List<JSONArray> list = FileUtils.deserializeFromFile(RuntimeEnv.getNCHome() + "/modules/arap/outterdata/ar");
		AggReceivableBillVO[] vos = arrayListToVos(list);
		
		//������
		JSONArray rtArray = new JSONArray();
		String outterBillno = "";
		IInsertReceiveBill service = NCLocator.getInstance().lookup(IInsertReceiveBill.class);
		for(AggReceivableBillVO vo : vos) {
			JSONObject rtJson = new JSONObject();
			outterBillno = (String) vo.getParentVO().getAttributeValue("def1");
			rtJson.put("FROMSYS", (String) vo.getParentVO().getAttributeValue("def4"));
			rtJson.put("NUMBER", outterBillno);
			try {
				if(isBillExit(vo)) {
					rtJson.put("COMPLETED", "1");
					rtJson.put("MESSAGE", "�õ����Ѵ��ڣ������ظ�¼��!");
				} else {
					String rt = service.insertReceiveBill_RequiresNew(vo);
					if(StringUtils.isBlank(rt)) {
						rtJson.put("COMPLETED", "1");
						rtJson.put("MESSAGE", "");
					} else {
						rtJson.put("COMPLETED", "0");
						rtJson.put("MESSAGE", rt);
					}
				}
			} catch(Exception e3) {
				rtJson.put("COMPLETED", "0");
				rtJson.put("MESSAGE", e3.getMessage());
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
	
	//���ݱ���ǰ����
	private boolean isBillExit(AggReceivableBillVO vo) throws BusinessException {
		String sql = "select count(*) from ar_recbill t where t.dr=0 and t.def1 = '" +vo.getParentVO().getAttributeValue("def1")+ "' and t.def4 = '" +vo.getParentVO().getAttributeValue("def4")+ "'";
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer)baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (DAOException e) {
			throw new BusinessException("�����Ƿ��ظ���֤ʱ����");
		}
		return count > 0;
	}
	
	
	
	private AggReceivableBillVO[] arrayListToVos(List<JSONArray> arrayList) {
		List<AggReceivableBillVO> voList = new ArrayList<AggReceivableBillVO>();
		for(JSONArray billsArray : arrayList) {
			//��ŷ���ɹ��ĵ���
			String outterBillno = "";
			JSONObject bill = null;
			//
			JSONArray rtArray = new JSONArray();
			for(int i=0; i<billsArray.size(); i++) {
				bill = billsArray.getJSONObject(i);
				outterBillno = importUtils.getExBillNo(bill);
				try {
					AggReceivableBillVO vo = jsonToVO(bill);
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
					Logger.error("����[" +outterBillno+ "]�����������ԭ��" + e3.getMessage() + "\n");
				}
			}
			if(rtArray.size() > 0) {
				importUtils.insertImportResult(rtArray);
			}
		}
		return (AggReceivableBillVO[])voList.toArray(new AggReceivableBillVO[0]);
	}
	
	//��json��ʽ�Ĳ���ת����vo
	private AggReceivableBillVO jsonToVO(JSONObject bill) throws BusinessException {
		
		AggReceivableBillVO aggReceivableBillVO = new AggReceivableBillVO();
		ReceivableBillVO parentVO = new ReceivableBillVO();
		
		/**  --��ͷ--  **/
		JSONObject parent = bill.getJSONObject("parent");
		UFDate billdate = new UFDate(parent.getString("billdate"));
		String pk_group = importUtils.getCurrGroupPk(defaultGroup);
		String pk_org = importUtils.getOrgByCode(pk_group, parent.getString("pk_org")).getPrimaryKey();
		String pk_user = importUtils.getInitUserPkByCode(defaultUser);
		String pk_tradetypeid = importUtils.getTradetypePkByCode(defaultTradeType, pk_group);
		String pk_currtype = importUtils.getCurrtypeByCode(parent.getString("pk_currtype")).getPrimaryKey();
		String sendCountryId = importUtils.getCountryId(pk_org);
		String scomment = parent.getString("scomment");
		String outterSysNum = parent.getString("def1");
		String outterSys = parent.getString("def4");
		String customer = "";
		UFDouble rate = null;
		
		
		parentVO.setApprovestatus(3);	//����״̬:1=ͨ��̬,3=�ύ̬
		parentVO.setBillstatus(-1);		//����״̬:1=����ͨ����-1=����
		parentVO.setEffectstatus(0);  	//��Ч״̬��10=����Ч��0=δ��Ч
		
		//��ϵͳ���ݺ�
		parentVO.setDef1(outterSysNum);
		parentVO.setBillclass("ys");
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
		//��������
		parentVO.setDef2(parent.getString("def2"));
		//?
		parentVO.setM_cooperateMoreTimes(UFBoolean.TRUE);	
		//ԭ��ʵ��
		parentVO.setMoney(new UFDouble(parent.getDouble("money")));	
		//ԭ������
		parentVO.setDef3(parent.getString("def3"));
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
		//�ֺŶ�Ӧ���ⲿ��
		parentVO.setDef6(parent.getString("def6"));
		//�ֺ�
		parentVO.setDef7(parent.getString("def7"));
		parentVO.setPk_tradetype(defaultTradeType);	//Ӧ������code
		parentVO.setPk_tradetypeid(pk_tradetypeid);
		//ժҪ
		parentVO.setScomment(scomment);	
		//������
		parentVO.setSendcountryid(sendCountryId);	
		parentVO.setSrc_syscode(17);	//������Դϵͳ:17,�ⲿ����ƽ̨
		//��˰��
		parentVO.setTaxcountryid(sendCountryId);
		parentVO.setSyscode(0);	//0=Ӧ��ϵͳ��1=Ӧ��ϵͳ
		//��Դϵͳ
		parentVO.setDef4(outterSys);
		//��Ӧ��嵥�ݺ�
		parentVO.setDef5(parent.getString("def5"));
		//ʹ��def14��ʾ�Ƿ�Ӧ��
		parentVO.setDef14(StringUtils.equalsIgnoreCase(bill.getString("needarap"), "Y") ? "Y" : "N");
		
		/**  --����--  **/
		JSONArray children = bill.getJSONArray("children");
		
		ReceivableBillItemVO[] childrenVO = new ReceivableBillItemVO[children.size()];
		for(int i=0; i<children.size(); i++) {
			
			JSONObject child = children.getJSONObject(i);
			UFDouble money_de = new UFDouble(child.getString("money_de"));
			UFDouble local_money_de = new UFDouble(child.getString("local_money_de"));
			String pk_customer = importUtils.getCustomerPkByCode(outterSys, child.getString("customer"), pk_group, pk_org);
			String rececountryid = importUtils.getCountryByCustomerID(pk_customer);
			String pk_material = importUtils.getMaterialPkByCode(child.getString("material"), pk_group, pk_org);
			String taxcodeid = importUtils.getTaxcodePkByCode(rececountryid, child.getString("material"));
			UFDouble childRate = new UFDouble(child.getString("rate"));
			if(i == 0) {
				customer = pk_customer;
				rate = childRate;
			}
			
			ReceivableBillItemVO childVO = new ReceivableBillItemVO();
			childVO.setAgentreceivelocal(UFDouble.ZERO_DBL);
			childVO.setAgentreceiveprimal(UFDouble.ZERO_DBL);
			childVO.setBillclass("ys");
			childVO.setBilldate(billdate);
			childVO.setBusidate(billdate);
			//�������ͣ�1���������ۣ�3��������
			childVO.setBuysellflag(1);	
			//��˰���
			childVO.setCaltaxmny(new UFDouble(1401L));	
			childVO.setCustomer(pk_customer);	//�ͻ�
			childVO.setDirection(1);	//����1=�跽��-1=����
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
			childVO.setLocal_money_bal(local_money_de);
			childVO.setLocal_money_cr(UFDouble.ZERO_DBL);
			//��֯���ң�ʵ��
			childVO.setLocal_money_de(local_money_de);
			//����
			childVO.setDef1(child.getString("def2"));
			childVO.setLocal_notax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_notax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_price(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_cr(UFDouble.ZERO_DBL);
			childVO.setLocal_tax_de(UFDouble.ZERO_DBL);
			childVO.setLocal_taxprice(UFDouble.ZERO_DBL);
			//ԭ������ʵ��
			childVO.setMoney_bal(money_de);	
			childVO.setMoney_cr(UFDouble.ZERO_DBL);
			//�跽ԭ�ң�ʵ��
			childVO.setMoney_de(money_de);	
			//����
			childVO.setDef1(child.getString("def1"));
			//����
			childVO.setMaterial(pk_material);
			childVO.setMaterial_src(pk_material);
			childVO.setNosubtax(UFDouble.ZERO_DBL);
			childVO.setNosubtaxrate(UFDouble.ZERO_DBL);
			childVO.setNotax_cr(UFDouble.ZERO_DBL);
			childVO.setNotax_de(money_de);
			//��������Ĭ���ǿͻ�
			childVO.setObjtype(0);
			childVO.setOccupationmny(money_de);
			childVO.setPausetransact(UFBoolean.FALSE);	//	�����־
			childVO.setPk_billtype(defaultBillType);
			childVO.setPk_currtype(pk_currtype);
			childVO.setPk_group(pk_group);
			childVO.setPk_org(pk_org);
			childVO.setPk_tradetype(defaultTradeType);
			childVO.setPk_tradetypeid(pk_tradetypeid);
			childVO.setPrepay(0);
			childVO.setQuantity_bal(UFDouble.ZERO_DBL);
			childVO.setRate(childRate);
			childVO.setRececountryid(rececountryid);	//�ջ���
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
			
			childrenVO[i] = childVO;
		}
		
		/** ��ͷ���������  **/
		//�ͻ�
		parentVO.setCustomer(customer);	
		//��֯���һ���
		parentVO.setRate(rate);
		
		aggReceivableBillVO.setParentVO(parentVO);
		aggReceivableBillVO.setChildrenVO(childrenVO);
		return aggReceivableBillVO;
	}
	
}
