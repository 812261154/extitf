package nc.bs.arap.util;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.itf.arap.parm.ArapImportResultVO;
import nc.itf.arap.parm.ArapImportResultVOMeta;
import nc.itf.arap.parm.ImportFeildVO;
import nc.itf.gl.voucher.IVoucherNo;
import nc.itf.org.IOrgConst;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.pubitf.bd.accessor.GeneralAccessorFactory;
import nc.pubitf.bd.accessor.IGeneralAccessor;
import nc.pubitf.uapbd.IAccountPubService;
import nc.pubitf.uapbd.ICustomerPubService;
import nc.pubitf.uapbd.ISupplierPubService;
import nc.vo.arap.djlx.DjLXVO;
import nc.vo.bd.accassitem.AccAssItemVO;
import nc.vo.bd.accessor.IBDData;
import nc.vo.bd.account.AccountVO;
import nc.vo.bd.countryzone.CountryZoneVO;
import nc.vo.bd.currtype.CurrtypeVO;
import nc.vo.bd.cust.CustomerVO;
import nc.vo.bd.cust.custclass.CustClassVO;
import nc.vo.bd.cust.merge.CustmergeVO;
import nc.vo.bd.inoutbusiclass.InoutBusiClassVO;
import nc.vo.bd.supplier.SupplierVO;
import nc.vo.bd.supplier.merge.SupmergeVO;
import nc.vo.bd.supplier.supplierclass.SupplierclassVO;
import nc.vo.bd.vouchertype.VoucherTypeVO;
import nc.vo.gl.pubvoucher.VoucherVO;
import nc.vo.org.AccountingBookVO;
import nc.vo.org.DeptVO;
import nc.vo.org.GroupVO;
import nc.vo.org.OrgVO;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class BillUtils {

	public static Set<String> arrayToSet(String[] array) {
		Set<String> strSet = new HashSet<String>();
		CollectionUtils.addAll(strSet, array);
		return strSet;
	}

	// ȡӦ��Ӧ���������ϵͳ���ݺ�
	public static String getArapBillSourceNo(JSONObject bill) {
		try {
			return bill.getJSONObject("parent").getString("def1");
		} catch (Exception e) {
			return "[δ֪���ݺ�]";
		}
	}

	// ȡӦ��Ӧ���������Դϵͳ
	public static String getArapBillSourceSys(JSONObject bill) {
		try {
			return bill.getJSONObject("parent").getString("def4");
		} catch (Exception e) {
			return "[δ֪��Դϵͳ]";
		}
	}

	// ������Դϵͳ���ͻ������ǰ׺
	public static String getCustomerCode(String sysName, String code) {
		if (StringUtils.isBlank(code)) {
			return "";
		}
		if (StringUtils.isBlank(sysName)) {
			return code;
		}
		if (StringUtils.equalsIgnoreCase(sysName, "PMP")) {
			return "PMPC_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "PMPYW")) {
			return "YWC_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "PMPTG")) {
			return "TGC_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "ZCZL")) {
			return "ZCZLC_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "XHSD")) {
			return "XHSDC_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "HW")) {
			return "HWC_" + code;
		} else {
			return code;
		}
	}

	/**
	 * ����"XXXXXXXXXXXX"��ʽ����ȡ����MAC��ַ
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getMacAddress() {
		try {
			Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
			while (ni.hasMoreElements()) {
				NetworkInterface netI = ni.nextElement();
				byte[] bytes = netI.getHardwareAddress();
				if (netI.isUp() && netI != null && bytes != null && bytes.length == 6) {
					StringBuffer sb = new StringBuffer();
					for (byte b : bytes) {
						// ��11110000����λ�������Ա��ȡ��ǰ�ֽڸ�4λ
						sb.append(Integer.toHexString((b & 240) >> 4));
						// ��00001111����λ�������Ա��ȡ��ǰ�ֽڵ�4λ
						sb.append(Integer.toHexString(b & 15));
						// sb.append("-");
					}
					// sb.deleteCharAt(sb.length()-1);
					return sb.toString().toUpperCase();
				}
			}
		} catch (Exception e) {
			return "XXXXXXXXXXXX";
		}
		return "XXXXXXXXXXXX";
	}

	public static int getNo(VoucherVO vo) throws BusinessException {
		try {
			IVoucherNo voucherno = (IVoucherNo) NCLocator.getInstance().lookup(IVoucherNo.class.getName());
			return Integer.valueOf(voucherno.getVoucherNumber_RequiresNew(vo, null)[1]);
		} catch (Exception e) {
			throw new BusinessException("ƾ֤�Ż�ȡʧ�ܣ�");
		}
	}

	// �ӵ���json�ַ�����ȡ��Դҵ��ϵͳ
	public static String getSourceSysFromJson(JSONObject bill) {
		return bill.getJSONObject("parent").getString("def4");
	}

	// ������Դϵͳ����Ӧ�̱����ǰ׺
	public static String getSupplierCode(String sysName, String code) {
		if (StringUtils.isBlank(code)) {
			return "";
		}
		if (StringUtils.isBlank(sysName)) {
			return code;
		}
		if (StringUtils.equalsIgnoreCase(sysName, "PMP")) {
			return "PMPS_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "PMPYW")) {
			return "YWS_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "PMPTG")) {
			return "TGS_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "ZCZL")) {
			return "ZCZLS_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "XHSD")) {
			return "XHSDS_" + code;
		} else if (StringUtils.equalsIgnoreCase(sysName, "HW")) {
			return "HWS_" + code;
		} else {
			return code;
		}
	}

	// ��ȡΨһID
	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		// ȥ��"-"����
		return s.replace("-", "");
	}

	// ȡƾ֤��ĵ��ݺ�
	public static String getVoucherSourceNo(JSONObject voucher) {
		try {
			return voucher.getString("free4");
		} catch (Exception e) {
			return "[δ֪��Դ���ݺ�]";
		}
	}

	// ȡƾ֤�����Դϵͳ
	public static String getVoucherSourceSys(JSONObject voucher) {
		try {
			return voucher.getString("free3");
		} catch (Exception e) {
			return "[δ֪��Դϵͳ]";
		}
	}

	// �������Ƿ�Ϸ�
	public static boolean isFieldLegal(ImportFeildVO field, String value) {
		if (!field.isNullable() && StringUtils.isBlank(value)) {
			return false;
		}
		// �������ڱ������������ַ���
		if (field.getDataType() == ImportFeildVO.DATETIME && !DateUtils.isFormatDateTimeString(value)) {
			return false;
		}
		if (field.getDataType() == ImportFeildVO.DATE && !DateUtils.isFormatDateString(value)) {
			return false;
		}
		// ���������������ַ���
		if (field.getDataType() == ImportFeildVO.NUMBER && !BillUtils.isNumberic(value)) {
			return false;
		}
		return true;
	}

	// �ж��ַ����Ƿ�������
	public static boolean isNumberic(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// ��Դҵ��ϵͳ�Ƿ�Ϸ�
	public static boolean isSourceSysLegal(String code) {
		try {
			Set<String> set = BillUtils.arrayToSet(FileUtils.getProperties("nc/bs/arap/properties/ArapWsPrams.properties", "sourceSysCodes").split(","));
			return set.contains(code);
		} catch (BusinessException e) {
			return false;
		}
	}

	public static synchronized JSONArray queryImportResult() throws Exception {
		JSONArray array = new JSONArray();
		BaseDAO baseDAO = new BaseDAO();
		Collection<ArapImportResultVO> collections = baseDAO.retrieveAll(ArapImportResultVO.class, new ArapImportResultVOMeta());
		baseDAO.deleteObject((ArapImportResultVO[]) collections.toArray(new ArapImportResultVO[0]), new ArapImportResultVOMeta());
		for (ArapImportResultVO vo : collections) {
			JSONObject json = new JSONObject();
			json.put("FROMSYS", vo.getFromsys());
			json.put("NUMBER", vo.getNumber());
			json.put("COMPLETED", vo.getCompleted());
			json.put("MESSAGE", vo.getMessage());
			array.add(json);
		}
		return array;
	}

	// ����pkȡ����������
	public AccAssItemVO getAccassItemVO(String pk_accassitem) throws BusinessException {
		BaseDAO baseDAO = new BaseDAO();
		try {
			AccAssItemVO vo = (AccAssItemVO) baseDAO.retrieveByPK(AccAssItemVO.class, pk_accassitem);
			return vo;
		} catch (Exception e) {
			throw new BusinessException("����������[" + pk_accassitem + "]�����ڣ�" + e.getMessage());
		}
	}

	// ���ݲ�����֯����ȡĬ�ϵĺ����˲�
	public AccountingBookVO getAccountingBook(String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<AccountingBookVO> vos = baseDAO.retrieveByClause(AccountingBookVO.class, "dr = 0 and accounttype = 1 and pk_group = '" + pk_group + "' and pk_relorg = '" + pk_org + "' ");
			return vos.toArray(new AccountingBookVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("������֯[" + pk_org + "]��Ӧ���������˲������ڣ�" + e.getMessage());
		}
	}

	// ���ݱ�����ƿ�Ŀ
	public AccountVO getAccountVOByCode(String pk_accountingbook, String code, String busiDate) throws BusinessException {
		try {
			IAccountPubService accountservice = (IAccountPubService) NCLocator.getInstance().lookup(IAccountPubService.class.getName());
			AccountVO[] accountVOs = accountservice.queryAccountVOsByCodes(pk_accountingbook, new String[] { code }, busiDate);
			return accountVOs[0];
		} catch (Exception e) {
			throw new BusinessException("��ƿ�Ŀ[" + code + "]�����ڣ�" + e.getMessage());
		}
	}

	// ���ݿͻ�ID��ѯ����
	public String getCountryByCustomerID(String pk_customer) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_customer='" + pk_customer + "' ");
			return vos.toArray(new CustomerVO[vos.size()])[0].getPk_country();
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + pk_customer + "]��Ӧ�Ĺ��Ҳ����ڣ�" + e.getMessage());
		}
	}

	// ���ݹ�Ӧ��ID��ѯ����
	public String getCountryBySupplierID(String pk_supplier) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_supplier='" + pk_supplier + "' ");
			return vos.toArray(new SupplierVO[vos.size()])[0].getPk_country();
		} catch (Exception e) {
			throw new BusinessException("��Ӧ��[" + pk_supplier + "]��Ӧ�Ĺ��Ҳ���ʧ�ܣ�" + e.getMessage());
		}
	}

	// ����pk_orgȡ����
	public String getCountryId(String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<OrgVO> vos = baseDAO.retrieveByClause(OrgVO.class, " dr=0 and pk_org='" + pk_org + "' ");
			return vos.toArray(new OrgVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("������֯[" + pk_org + "]��Ӧ�Ĺ��Ҳ����ڣ�" + e.getMessage());
		}
	}

	// ���ݱ����ȡĬ�ϵĹ���pk�Լ����Ӧ��ʱ��pk
	public CountryZoneVO getCountryZoneFormatPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CountryZoneVO> vos = baseDAO.retrieveByClause(CountryZoneVO.class, " dr=0 and code='" + code + "' ");
			return vos.toArray(new CountryZoneVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("����[" + code + "]�����ڣ�");
		}
	}

	// ��ȡ��ǰ���ŵ�pk
	public String getCurrGroupPk(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<GroupVO> vos = baseDAO.retrieveByClause(GroupVO.class, " dr=0 and code='" + code + "' and enablestate=2 ");
			return vos.toArray(new GroupVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("����[" + code + "]��ȡʧ�ܣ�");
		}
	}

	// ���ݱ��ֱ����ȡpk
	public CurrtypeVO getCurrtypeByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CurrtypeVO> vos = baseDAO.retrieveByClause(CurrtypeVO.class, " dr=0 and code='" + code + "' ");
			return vos.toArray(new CurrtypeVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("����[" + code + "]��ȡʧ�ܣ�");
		}
	}

	// ���ݱ����ȡĬ�ϵĿͻ�����pk
	public String getCustclassPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustClassVO> vos = baseDAO.retrieveByClause(CustClassVO.class, " dr=0 and code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new CustClassVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("�ͻ�����[" + code + "]�����ڣ�");
		}
	}

	/**
	 * ����Դ��Ӧ��PK�������պϲ��Ĺ�Ӧ��
	 * 
	 * @param pk_source
	 * @return
	 * @throws DAOException
	 */
	public String getCustmergeBySourcePk(String pk_source) throws DAOException {
		String pk_target = "";
		BaseDAO baseDAO = new BaseDAO();
		Collection<CustmergeVO> vos = baseDAO.retrieveByClause(CustmergeVO.class, " pk_source = '" + pk_source + "' ");
		if (vos.size() > 0) {
			CustmergeVO custmergeVO = vos.toArray(new CustmergeVO[vos.size()])[0];
			pk_target = getCustmergeBySourcePk(custmergeVO.getPk_target());
		} else {
			pk_target = pk_source;
		}
		return pk_target;
	}

	/**
	 * ȡ�ͻ��ϲ���ϵ
	 * 
	 * @param pk_group
	 * @param pk_org
	 * @param code
	 * @throws DAOException
	 */
	public String getCustmergeTargetPk(String pk_group, String pk_org, String code) throws DAOException {
		String pk_target = "";
		BaseDAO baseDAO = new BaseDAO();
		Collection<CustmergeVO> vos = baseDAO.retrieveByClause(CustmergeVO.class, " source_code = '" + code + "' and source_org in ('" + pk_group + "', '" + pk_org + "') ");
		if (vos.size() > 0) {
			CustmergeVO custmergeVO = vos.toArray(new CustmergeVO[vos.size()])[0];
			pk_target = getCustmergeBySourcePk(custmergeVO.getPk_target());
		}
		return pk_target;
	}
	
	// ����orgȡ�ͻ�
		public String getCustomerByOrg(String pk_org) throws BusinessException {
			try {
				BaseDAO baseDAO = new BaseDAO();
				Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_financeorg='" + pk_org + "' and enablestate = 2 ");
				return vos.toArray(new CustomerVO[vos.size()])[0].getPrimaryKey();
			} catch (Exception e) {
				throw new BusinessException("�ͻ�[" + pk_org + "]�����ڣ�");
			}
		}

	// ȡҵ�����
	// public Map<String, String> getDefaultParam(String module) throws
	// BusinessException {
	// IDBCacheBS idbcachebs = (IDBCacheBS)
	// NCLocator.getInstance().lookup(IDBCacheBS.class.getName());
	// String sql =
	// "select code, value from arap_importparam where module in ('arap', '"
	// +module+ "')";
	// try {
	// BaseDAO baseDAO = new BaseDAO();
	// List<Map<String, String>> list = (List<Map<String,
	// String>>)baseDAO.executeQuery(sql, new MapListProcessor());
	// Map<String, String> rtMap = new HashMap<String, String>();
	// for(Map<String, String> tmpMap : list) {
	// rtMap.put(tmpMap.get("code"), tmpMap.get("value"));
	// }
	// return rtMap;
	//
	// } catch (Exception e) {
	// throw new BusinessException("ҵ�������ʼ������");
	// }
	// }

	// ���ݱ���ȡ�ͻ�VO,�ͻ��ӿڵ�ɾ��ר��
	public CustomerVO getCustomerVOByCode(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org='" + pk_org + "' ");
			return vos.toArray(new CustomerVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ�ͻ�pk
	public CustomerVO getCustomerVOByCode(String sysName, String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			// NC�ͻ�
			if (code.startsWith("|NC|")) {
				Collection<CustomerVO> vosin = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_org in ('" + pk_org + "', '" + pk_group + "') and code = '" + code.substring(4) + "'  and enablestate = 2 ");
				return vosin.toArray(new CustomerVO[vosin.size()])[0];
				// ҵ��ϵͳ�ͻ�
			} else {
				Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_org in ('" + pk_org + "', '" + pk_group + "') and def2 = '" + code + "' and def1 = '" + sysName + "'  and enablestate = 2 ");
				if (vos.size() > 0) {
					return vos.toArray(new CustomerVO[vos.size()])[0];

				// ������Ҳ����������Ƿ����˺ϲ�
				} else {
					String pk_target = this.getCustmergeTargetPk(pk_group, pk_org, this.getCustomerCode(sysName, code));
					if (StringUtils.isNotBlank(pk_target)) {
						return this.getCustomerVOByPk(pk_target);
					} else {
						throw new BusinessException();
					}
				}
			}
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ�ͻ�VO
	public CustomerVO getCustomerVOByPk(String pk_customer) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (CustomerVO) baseDAO.retrieveByPK(CustomerVO.class, pk_customer);
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + pk_customer + "]�����ڣ�");
		}
	}

	// ���ݱ����ȡ����pk
	public DeptVO getDeptVOByCode(String pk_group, String pk_org, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<DeptVO> vos = baseDAO.retrieveByClause(DeptVO.class, " dr=0 and pk_group = '" + pk_group + "' and pk_org='" + pk_org + "'  and code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new DeptVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("����[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ������������
	public IBDData getDocByCode(String classId, String pk_group, String pk_org, String code) throws BusinessException {
		IGeneralAccessor accessor = GeneralAccessorFactory.getAccessor(classId);
		IBDData ibdata = accessor.getDocByCode(pk_org, code);
		if (ibdata != null) {
			return ibdata;
		}
		ibdata = accessor.getDocByCode(pk_group, code);
		if (ibdata != null) {
			return ibdata;
		}
		ibdata = accessor.getDocByCode(IOrgConst.GLOBEORG, code);
		if (ibdata != null) {
			return ibdata;
		}
		throw new BusinessException("������������[" + code + "]�����ڣ�");
	}

	// ���ݱ����ȡĬ�ϵĲ����û�pk
	public String getInitUserPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<UserVO> vos = baseDAO.retrieveByClause(UserVO.class, " dr=0 and user_code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new UserVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("�û�[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ��֧��ĿVO
	public InoutBusiClassVO getInoutBusiClassVO(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<InoutBusiClassVO> vos = baseDAO.retrieveByClause(InoutBusiClassVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org in ('" + pk_group + "', '" + pk_org + "') ");
			return vos.toArray(new InoutBusiClassVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("��֧��Ŀ[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ����pk
	public String getMaterialPkByCode(String code, String pk_group, String pk_org) throws BusinessException {
		String pk_material = "";
		try {
			BaseDAO dao = new BaseDAO();
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT pk_material ");
			sql.append("  FROM bd_material ");
			sql.append(" WHERE (11 = 11) ");
			sql.append("   AND enablestate = 2 ");
			sql.append("   AND pk_material IN ");
			sql.append("  	       (SELECT pk_material ");
			sql.append("  	          FROM bd_marorg ");
			sql.append("  	         WHERE (pk_org IN ('" + pk_org + "')) ");
			sql.append("  	           AND (enablestate = 2) ");
			sql.append("  	        UNION ");
			sql.append("  	        SELECT pk_material ");
			sql.append("  	          FROM bd_material ");
			sql.append("  	         WHERE pk_org in ('" + pk_group + "', '" + pk_org + "') ");
			sql.append("  	           AND latest = 'Y') ");
			sql.append("  AND code = '" + code + "' ");

			pk_material = (String) dao.executeQuery(sql.toString(), new ColumnProcessor());

		} catch (Exception e) {
			throw new BusinessException("����" + code + "�������," + e.getMessage());
		}

		if (StringUtils.isBlank(pk_material))
			throw new BusinessException("����[" + code + "]�����ڣ�");
		return pk_material;
	}

	// ���ݱ����ȡ������֯
	public OrgVO getOrgByCode(String pk_group, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<OrgVO> vos = baseDAO.retrieveByClause(OrgVO.class, " dr=0 and pk_group = '" + pk_group + "' and code='" + code + "' and enablestate = 2 and isbusinessunit = 'Y' ");
			return vos.toArray(new OrgVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("������֯[" + code + "]�����ڣ�");
		}
	}

	// ���ݿͻ�pk��ȡ��Ӧ�Ĳ�����֯pk
	public String getOrgByCustomer(String pk_customer) throws BusinessException {
		ICustomerPubService service = NCLocator.getInstance().lookup(ICustomerPubService.class);
		try {
			CustomerVO[] vos = service.getCustomerVO(new String[] { pk_customer }, new String[] { "pk_financeorg" });
			return vos[0].getPk_financeorg();
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + pk_customer + "]��Ӧ���ڲ���λ�����ڣ�");
		}
	}
	
	
	// ���ݱ����ȡ������֯
	public OrgVO getOrgByPk(String pk) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (OrgVO) baseDAO.retrieveByPK(OrgVO.class, pk);
		} catch (Exception e) {
			throw new BusinessException("������֯[" + pk + "]�����ڣ�");
		}
	}

	// ���ݹ�Ӧ��pk��ȡ��Ӧ�Ĳ�����֯
	public String getOrgBySupplier(String pk_supplier) throws BusinessException {
		ISupplierPubService service = NCLocator.getInstance().lookup(ISupplierPubService.class);
		try {
			SupplierVO[] vos = service.getSupplierVO(new String[] { pk_supplier }, new String[] { "pk_financeorg" });
			return vos[0].getPk_financeorg();
		} catch (Exception e) {
			throw new BusinessException("��Ӧ��[" + pk_supplier + "]��Ӧ���ڲ���λ�����ڣ�");
		}
	}

	/**
	 * ����Դ��Ӧ��PK�������պϲ��Ĺ�Ӧ��
	 * 
	 * @param pk_source
	 * @return
	 * @throws DAOException
	 */
	public String getSupmergeBySourcePk(String pk_source) throws DAOException {
		String pk_target = "";
		BaseDAO baseDAO = new BaseDAO();
		Collection<SupmergeVO> vos = baseDAO.retrieveByClause(SupmergeVO.class, " pk_source = '" + pk_source + "' ");
		if (vos.size() > 0) {
			SupmergeVO supmergeVO = vos.toArray(new SupmergeVO[vos.size()])[0];
			pk_target = getSupmergeBySourcePk(supmergeVO.getPk_target());
		} else {
			pk_target = pk_source;
		}
		return pk_target;
	}

	/**
	 * ȡ��Ӧ�̺ϲ���ϵ
	 * 
	 * @param pk_group
	 * @param pk_org
	 * @param code
	 * @throws DAOException
	 */
	public String getSupmergeTargetPk(String pk_group, String pk_org, String code) throws DAOException {
		String pk_target = "";
		BaseDAO baseDAO = new BaseDAO();
		Collection<SupmergeVO> vos = baseDAO.retrieveByClause(SupmergeVO.class, " source_code = '" + code + "' and source_org in ('" + pk_group + "', '" + pk_org + "') ");
		if (vos.size() > 0) {
			SupmergeVO supmergeVO = vos.toArray(new SupmergeVO[vos.size()])[0];
			pk_target = getSupmergeBySourcePk(supmergeVO.getPk_target());
		}
		return pk_target;
	}

	// ����orgȡ��Ӧ��
	public String getSupplierByOrg(String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_financeorg = '" + pk_org + "' and enablestate = 2 ");
			return vos.toArray(new SupplierVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("�ͻ�[" + pk_org + "]�����ڣ�");
		}
	}

	// ���ݱ����ȡĬ�ϵĹ�Ӧ�̷���pk
	public String getSupplierclassPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierclassVO> vos = baseDAO.retrieveByClause(SupplierclassVO.class, " dr=0 and code = '" + code + "' and enablestate = 2 ");
			return vos.toArray(new SupplierclassVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("��Ӧ�̷���[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ��Ӧ��VO
	public SupplierVO getSupplierVOByCode(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org='" + pk_org + "' ");
			return vos.toArray(new SupplierVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("��Ӧ��[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ��Ӧ��pk
	public SupplierVO getSupplierVOByCode(String sysName, String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			// NC�ͻ�
			if (code.startsWith("|NC|")) {
				Collection<SupplierVO> vosin = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_org in ('" + pk_group + "', '" + pk_org + "') and code = '" + code.substring(4) + "' and enablestate = 2 ");
				return vosin.toArray(new SupplierVO[vosin.size()])[0];

				// ҵ��ϵͳ��Ӧ��
			} else {
				Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_org in ('" + pk_group + "', '" + pk_org + "') and def1 = '" + sysName + "' and def2 = '" + code + "' and enablestate = 2 ");
				if (vos.size() > 0) {
					return vos.toArray(new SupplierVO[vos.size()])[0];

					// ������Ҳ����������Ƿ����˺ϲ�
				} else {
					String pk_target = getSupmergeTargetPk(pk_group, pk_org, this.getSupplierCode(sysName, code));
					if (StringUtils.isNotBlank(pk_target)) {
						return this.getSupplierVOByPk(pk_target);
					} else {
						throw new BusinessException();
					}
				}
			}
		} catch (Exception e) {
			throw new BusinessException("��Ӧ��[" + code + "]�����ڣ�");
		}
	}

	// ���ݱ���pkȡ��Ӧ��VO
	public SupplierVO getSupplierVOByPk(String pk_supplier) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (SupplierVO) baseDAO.retrieveByPK(SupplierVO.class, pk_supplier);
		} catch (Exception e) {
			throw new BusinessException("��Ӧ��[" + pk_supplier + "]�����ڣ�");
		}
	}

	// ���ݱ���ȡ˰��pk
	public String getTaxcodePkByCode(String reptaxcountry, String materialCode) throws BusinessException {
		try {
			String taxrate = "0";
			if (StringUtils.equals(materialCode, "0101")) {
				taxrate = "0";
			} else if (StringUtils.equals(materialCode, "0102")) {
				taxrate = "13";
			} else if (StringUtils.equals(materialCode, "0103") || StringUtils.equals(materialCode, "0104")) {
				taxrate = "17";
			}
			StringBuffer sql = new StringBuffer();
			sql.append(" select bd_taxcode.pk_taxcode ");
			sql.append(" from bd_taxcode inner join bd_taxrate on bd_taxcode.pk_taxcode = bd_taxrate.pk_taxcode ");
			sql.append(" where 1=1 and enablestate = '2' ");
			sql.append(" and (bd_taxcode.pursaletype = '1' or bd_taxcode.pursaletype = '5') ");
			sql.append(" and bd_taxcode.reptaxcountry = '" + reptaxcountry + "' ");
			sql.append(" and bd_taxrate.taxrate = '" + taxrate + "' ");

			BaseDAO baseDAO = new BaseDAO();
			return (String) baseDAO.executeQuery(sql.toString(), new ColumnProcessor());

		} catch (Exception e) {
			throw new BusinessException("����[" + materialCode + "]��ȡʧ�ܣ�" + e.getMessage());
		}
	}

	// ���ݽ������ͱ����ȡpk
	public String getTradetypePkByCode(String code, String pk_group) throws BusinessException {
		try {
			// IBillTypePublic auditTask =
			// NCLocator.getInstance().lookup(IBillTypePublic.class);
			// BillTypeVO[] vos = auditTask.queryBillTypeByBillTypeCode(code,
			// pk_group);
			// return vos[0].getParentVO().getPrimaryKey();
			BaseDAO baseDAO = new BaseDAO();
			Collection<DjLXVO> vos = baseDAO.retrieveByClause(DjLXVO.class, " dr=0 and pk_group in ('~', '" + pk_group + "') and djlxbm = '" + code + "' ");
			return vos.toArray(new DjLXVO[vos.size()])[0].getPk_billtype();
		} catch (Exception e) {
			throw new BusinessException("��������[" + code + "]��ȡʧ�ܣ�" + e.getMessage());
		}
	}

	// ���ݱ���ȡƾ֤����
	// GLOBLE00000000000000��ʾȫ��
	public VoucherTypeVO getVoucherType(String pk_group, String pk_org, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<VoucherTypeVO> vos = baseDAO.retrieveByClause(VoucherTypeVO.class, "dr = 0 and enablestate = 2 and pk_org in ('" + pk_group + "', '" + pk_org + "', 'GLOBLE00000000000000') and code = '" + code + "' ");
			return vos.toArray(new VoucherTypeVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("����[" + code + "]��Ӧ��ƾ֤���Ͳ����ڣ�" + e.getMessage());
		}
	}

	public void insertImportResult(JSONArray jsonArray) {
		BaseDAO baseDAO = new BaseDAO();
		List<ArapImportResultVO> list = new ArrayList<ArapImportResultVO>();
		try {
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				ArapImportResultVO vo = new ArapImportResultVO();
				vo.setFromsys(json.getString("FROMSYS"));
				vo.setNumber(json.getString("NUMBER"));
				vo.setCompleted(json.getString("COMPLETED"));
				vo.setMessage(json.getString("MESSAGE"));
				list.add(vo);
			}
			baseDAO.insertObject((ArapImportResultVO[]) list.toArray(new ArapImportResultVO[0]), new ArapImportResultVOMeta());
		} catch (Exception e) {
			Logger.error("[PMP�󷨺�]��Ȼ����ʧ�ܣ�ԭ��" + e.getMessage() + "�����ݣ�" + jsonArray.toString());
		}
	}

	// ���ݱ���ǰ����
	public boolean isBillExit(String sql) throws BusinessException {
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer) baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (Exception e) {
			throw new BusinessException("�����Ƿ��ظ���֤ʱ����");
		}
		return count > 0;
	}

}
