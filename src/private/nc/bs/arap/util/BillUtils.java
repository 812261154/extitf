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

	// 取应收应付单里的外系统单据号
	public static String getArapBillSourceNo(JSONObject bill) {
		try {
			return bill.getJSONObject("parent").getString("def1");
		} catch (Exception e) {
			return "[未知单据号]";
		}
	}

	// 取应收应付单里的来源系统
	public static String getArapBillSourceSys(JSONObject bill) {
		try {
			return bill.getJSONObject("parent").getString("def4");
		} catch (Exception e) {
			return "[未知来源系统]";
		}
	}

	// 根据来源系统给客户编码加前缀
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
	 * 按照"XXXXXXXXXXXX"格式，获取本机MAC地址
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
						// 与11110000作按位与运算以便读取当前字节高4位
						sb.append(Integer.toHexString((b & 240) >> 4));
						// 与00001111作按位与运算以便读取当前字节低4位
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
			throw new BusinessException("凭证号获取失败；");
		}
	}

	// 从单据json字符串里取来源业务系统
	public static String getSourceSysFromJson(JSONObject bill) {
		return bill.getJSONObject("parent").getString("def4");
	}

	// 根据来源系统给供应商编码加前缀
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

	// 获取唯一ID
	public static String getUUID() {
		String s = UUID.randomUUID().toString();
		// 去掉"-"符号
		return s.replace("-", "");
	}

	// 取凭证里的单据号
	public static String getVoucherSourceNo(JSONObject voucher) {
		try {
			return voucher.getString("free4");
		} catch (Exception e) {
			return "[未知来源单据号]";
		}
	}

	// 取凭证里的来源系统
	public static String getVoucherSourceSys(JSONObject voucher) {
		try {
			return voucher.getString("free3");
		} catch (Exception e) {
			return "[未知来源系统]";
		}
	}

	// 输入项是否合法
	public static boolean isFieldLegal(ImportFeildVO field, String value) {
		if (!field.isNullable() && StringUtils.isBlank(value)) {
			return false;
		}
		// 单据日期必须是日期型字符串
		if (field.getDataType() == ImportFeildVO.DATETIME && !DateUtils.isFormatDateTimeString(value)) {
			return false;
		}
		if (field.getDataType() == ImportFeildVO.DATE && !DateUtils.isFormatDateString(value)) {
			return false;
		}
		// 金额必须是数字型字符串
		if (field.getDataType() == ImportFeildVO.NUMBER && !BillUtils.isNumberic(value)) {
			return false;
		}
		return true;
	}

	// 判断字符串是否是数字
	public static boolean isNumberic(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// 来源业务系统是否合法
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

	// 根据pk取辅助核算项
	public AccAssItemVO getAccassItemVO(String pk_accassitem) throws BusinessException {
		BaseDAO baseDAO = new BaseDAO();
		try {
			AccAssItemVO vo = (AccAssItemVO) baseDAO.retrieveByPK(AccAssItemVO.class, pk_accassitem);
			return vo;
		} catch (Exception e) {
			throw new BusinessException("辅助核算项[" + pk_accassitem + "]不存在，" + e.getMessage());
		}
	}

	// 根据财务组织编码取默认的核算账簿
	public AccountingBookVO getAccountingBook(String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<AccountingBookVO> vos = baseDAO.retrieveByClause(AccountingBookVO.class, "dr = 0 and accounttype = 1 and pk_group = '" + pk_group + "' and pk_relorg = '" + pk_org + "' ");
			return vos.toArray(new AccountingBookVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("财务组织[" + pk_org + "]对应的主核算账簿不存在，" + e.getMessage());
		}
	}

	// 根据编码查会计科目
	public AccountVO getAccountVOByCode(String pk_accountingbook, String code, String busiDate) throws BusinessException {
		try {
			IAccountPubService accountservice = (IAccountPubService) NCLocator.getInstance().lookup(IAccountPubService.class.getName());
			AccountVO[] accountVOs = accountservice.queryAccountVOsByCodes(pk_accountingbook, new String[] { code }, busiDate);
			return accountVOs[0];
		} catch (Exception e) {
			throw new BusinessException("会计科目[" + code + "]不存在，" + e.getMessage());
		}
	}

	// 根据客户ID查询国家
	public String getCountryByCustomerID(String pk_customer) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_customer='" + pk_customer + "' ");
			return vos.toArray(new CustomerVO[vos.size()])[0].getPk_country();
		} catch (Exception e) {
			throw new BusinessException("客户[" + pk_customer + "]对应的国家不存在，" + e.getMessage());
		}
	}

	// 根据供应商ID查询国家
	public String getCountryBySupplierID(String pk_supplier) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_supplier='" + pk_supplier + "' ");
			return vos.toArray(new SupplierVO[vos.size()])[0].getPk_country();
		} catch (Exception e) {
			throw new BusinessException("供应商[" + pk_supplier + "]对应的国家查找失败，" + e.getMessage());
		}
	}

	// 根据pk_org取国家
	public String getCountryId(String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<OrgVO> vos = baseDAO.retrieveByClause(OrgVO.class, " dr=0 and pk_org='" + pk_org + "' ");
			return vos.toArray(new OrgVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("财务组织[" + pk_org + "]对应的国家不存在，" + e.getMessage());
		}
	}

	// 根据编码获取默认的国家pk以及其对应的时区pk
	public CountryZoneVO getCountryZoneFormatPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CountryZoneVO> vos = baseDAO.retrieveByClause(CountryZoneVO.class, " dr=0 and code='" + code + "' ");
			return vos.toArray(new CountryZoneVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("国家[" + code + "]不存在；");
		}
	}

	// 获取当前集团的pk
	public String getCurrGroupPk(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<GroupVO> vos = baseDAO.retrieveByClause(GroupVO.class, " dr=0 and code='" + code + "' and enablestate=2 ");
			return vos.toArray(new GroupVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("集团[" + code + "]获取失败；");
		}
	}

	// 根据币种编码获取pk
	public CurrtypeVO getCurrtypeByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CurrtypeVO> vos = baseDAO.retrieveByClause(CurrtypeVO.class, " dr=0 and code='" + code + "' ");
			return vos.toArray(new CurrtypeVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("币种[" + code + "]获取失败；");
		}
	}

	// 根据编码获取默认的客户分类pk
	public String getCustclassPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustClassVO> vos = baseDAO.retrieveByClause(CustClassVO.class, " dr=0 and code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new CustClassVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("客户分类[" + code + "]不存在；");
		}
	}

	/**
	 * 根据源供应商PK查找最终合并的供应商
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
	 * 取客户合并关系
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
	
	// 根据org取客户
		public String getCustomerByOrg(String pk_org) throws BusinessException {
			try {
				BaseDAO baseDAO = new BaseDAO();
				Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_financeorg='" + pk_org + "' and enablestate = 2 ");
				return vos.toArray(new CustomerVO[vos.size()])[0].getPrimaryKey();
			} catch (Exception e) {
				throw new BusinessException("客户[" + pk_org + "]不存在；");
			}
		}

	// 取业务参数
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
	// throw new BusinessException("业务参数初始化错误；");
	// }
	// }

	// 根据编码取客户VO,客户接口的删改专用
	public CustomerVO getCustomerVOByCode(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org='" + pk_org + "' ");
			return vos.toArray(new CustomerVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("客户[" + code + "]不存在；");
		}
	}

	// 根据编码取客户pk
	public CustomerVO getCustomerVOByCode(String sysName, String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			// NC客户
			if (code.startsWith("|NC|")) {
				Collection<CustomerVO> vosin = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_org in ('" + pk_org + "', '" + pk_group + "') and code = '" + code.substring(4) + "'  and enablestate = 2 ");
				return vosin.toArray(new CustomerVO[vosin.size()])[0];
				// 业务系统客户
			} else {
				Collection<CustomerVO> vos = baseDAO.retrieveByClause(CustomerVO.class, " dr=0 and pk_org in ('" + pk_org + "', '" + pk_group + "') and def2 = '" + code + "' and def1 = '" + sysName + "'  and enablestate = 2 ");
				if (vos.size() > 0) {
					return vos.toArray(new CustomerVO[vos.size()])[0];

				// 如果查找不到，考虑是否做了合并
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
			throw new BusinessException("客户[" + code + "]不存在；");
		}
	}

	// 根据编码取客户VO
	public CustomerVO getCustomerVOByPk(String pk_customer) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (CustomerVO) baseDAO.retrieveByPK(CustomerVO.class, pk_customer);
		} catch (Exception e) {
			throw new BusinessException("客户[" + pk_customer + "]不存在；");
		}
	}

	// 根据编码获取部门pk
	public DeptVO getDeptVOByCode(String pk_group, String pk_org, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<DeptVO> vos = baseDAO.retrieveByClause(DeptVO.class, " dr=0 and pk_group = '" + pk_group + "' and pk_org='" + pk_org + "'  and code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new DeptVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("部门[" + code + "]不存在；");
		}
	}

	// 根据编码取辅助核算内容
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
		throw new BusinessException("辅助核算内容[" + code + "]不存在；");
	}

	// 根据编码获取默认的操作用户pk
	public String getInitUserPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<UserVO> vos = baseDAO.retrieveByClause(UserVO.class, " dr=0 and user_code='" + code + "' and enablestate = 2 ");
			return vos.toArray(new UserVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("用户[" + code + "]不存在；");
		}
	}

	// 根据编码取收支项目VO
	public InoutBusiClassVO getInoutBusiClassVO(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<InoutBusiClassVO> vos = baseDAO.retrieveByClause(InoutBusiClassVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org in ('" + pk_group + "', '" + pk_org + "') ");
			return vos.toArray(new InoutBusiClassVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("收支项目[" + code + "]不存在；");
		}
	}

	// 根据编码取物料pk
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
			throw new BusinessException("物料" + code + "翻译出错," + e.getMessage());
		}

		if (StringUtils.isBlank(pk_material))
			throw new BusinessException("物料[" + code + "]不存在；");
		return pk_material;
	}

	// 根据编码获取财务组织
	public OrgVO getOrgByCode(String pk_group, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<OrgVO> vos = baseDAO.retrieveByClause(OrgVO.class, " dr=0 and pk_group = '" + pk_group + "' and code='" + code + "' and enablestate = 2 and isbusinessunit = 'Y' ");
			return vos.toArray(new OrgVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("财务组织[" + code + "]不存在；");
		}
	}

	// 根据客户pk获取对应的财务组织pk
	public String getOrgByCustomer(String pk_customer) throws BusinessException {
		ICustomerPubService service = NCLocator.getInstance().lookup(ICustomerPubService.class);
		try {
			CustomerVO[] vos = service.getCustomerVO(new String[] { pk_customer }, new String[] { "pk_financeorg" });
			return vos[0].getPk_financeorg();
		} catch (Exception e) {
			throw new BusinessException("客户[" + pk_customer + "]对应的内部单位不存在；");
		}
	}
	
	
	// 根据编码获取财务组织
	public OrgVO getOrgByPk(String pk) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (OrgVO) baseDAO.retrieveByPK(OrgVO.class, pk);
		} catch (Exception e) {
			throw new BusinessException("财务组织[" + pk + "]不存在；");
		}
	}

	// 根据供应商pk获取对应的财务组织
	public String getOrgBySupplier(String pk_supplier) throws BusinessException {
		ISupplierPubService service = NCLocator.getInstance().lookup(ISupplierPubService.class);
		try {
			SupplierVO[] vos = service.getSupplierVO(new String[] { pk_supplier }, new String[] { "pk_financeorg" });
			return vos[0].getPk_financeorg();
		} catch (Exception e) {
			throw new BusinessException("供应商[" + pk_supplier + "]对应的内部单位不存在；");
		}
	}

	/**
	 * 根据源供应商PK查找最终合并的供应商
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
	 * 取供应商合并关系
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

	// 根据org取供应商
	public String getSupplierByOrg(String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_financeorg = '" + pk_org + "' and enablestate = 2 ");
			return vos.toArray(new SupplierVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("客户[" + pk_org + "]不存在；");
		}
	}

	// 根据编码获取默认的供应商分类pk
	public String getSupplierclassPkByCode(String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<SupplierclassVO> vos = baseDAO.retrieveByClause(SupplierclassVO.class, " dr=0 and code = '" + code + "' and enablestate = 2 ");
			return vos.toArray(new SupplierclassVO[vos.size()])[0].getPrimaryKey();
		} catch (Exception e) {
			throw new BusinessException("供应商分类[" + code + "]不存在；");
		}
	}

	// 根据编码取供应商VO
	public SupplierVO getSupplierVOByCode(String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<CustomerVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and code='" + code + "' and pk_group='" + pk_group + "' and pk_org='" + pk_org + "' ");
			return vos.toArray(new SupplierVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("供应商[" + code + "]不存在；");
		}
	}

	// 根据编码取供应商pk
	public SupplierVO getSupplierVOByCode(String sysName, String code, String pk_group, String pk_org) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			// NC客户
			if (code.startsWith("|NC|")) {
				Collection<SupplierVO> vosin = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_org in ('" + pk_group + "', '" + pk_org + "') and code = '" + code.substring(4) + "' and enablestate = 2 ");
				return vosin.toArray(new SupplierVO[vosin.size()])[0];

				// 业务系统供应商
			} else {
				Collection<SupplierVO> vos = baseDAO.retrieveByClause(SupplierVO.class, " dr=0 and pk_org in ('" + pk_group + "', '" + pk_org + "') and def1 = '" + sysName + "' and def2 = '" + code + "' and enablestate = 2 ");
				if (vos.size() > 0) {
					return vos.toArray(new SupplierVO[vos.size()])[0];

					// 如果查找不到，考虑是否做了合并
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
			throw new BusinessException("供应商[" + code + "]不存在；");
		}
	}

	// 根据编码pk取供应商VO
	public SupplierVO getSupplierVOByPk(String pk_supplier) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			return (SupplierVO) baseDAO.retrieveByPK(SupplierVO.class, pk_supplier);
		} catch (Exception e) {
			throw new BusinessException("供应商[" + pk_supplier + "]不存在；");
		}
	}

	// 根据编码取税码pk
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
			throw new BusinessException("稅码[" + materialCode + "]获取失败，" + e.getMessage());
		}
	}

	// 根据交易类型编码获取pk
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
			throw new BusinessException("交易类型[" + code + "]获取失败，" + e.getMessage());
		}
	}

	// 根据编码取凭证类型
	// GLOBLE00000000000000表示全局
	public VoucherTypeVO getVoucherType(String pk_group, String pk_org, String code) throws BusinessException {
		try {
			BaseDAO baseDAO = new BaseDAO();
			Collection<VoucherTypeVO> vos = baseDAO.retrieveByClause(VoucherTypeVO.class, "dr = 0 and enablestate = 2 and pk_org in ('" + pk_group + "', '" + pk_org + "', 'GLOBLE00000000000000') and code = '" + code + "' ");
			return vos.toArray(new VoucherTypeVO[vos.size()])[0];
		} catch (Exception e) {
			throw new BusinessException("编码[" + code + "]对应的凭证类型不存在，" + e.getMessage());
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
			Logger.error("[PMP大法好]竟然插入失败，原因：" + e.getMessage() + "；数据：" + jsonArray.toString());
		}
	}

	// 单据保存前查重
	public boolean isBillExit(String sql) throws BusinessException {
		BaseDAO baseDAO = new BaseDAO();
		int count = 0;
		try {
			count = (Integer) baseDAO.executeQuery(sql, new ColumnProcessor());
		} catch (Exception e) {
			throw new BusinessException("单据是否重复验证时出错！");
		}
		return count > 0;
	}

}
