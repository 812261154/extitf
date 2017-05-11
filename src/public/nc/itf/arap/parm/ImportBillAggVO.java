package nc.itf.arap.parm;

import java.util.ArrayList;
import java.util.List;

public class ImportBillAggVO {
	
	private List<ImportFeildVO> headFields = new ArrayList<ImportFeildVO>();
	private List<ImportFeildVO> bodyFields = new ArrayList<ImportFeildVO>();
	
	
	public List<ImportFeildVO> getHeadFields() {
		return headFields;
	}
	public void setHeadFields(List<ImportFeildVO> headFields) {
		this.headFields = headFields;
	}
	public List<ImportFeildVO> getBodyFields() {
		return bodyFields;
	}
	public void setBodyFields(List<ImportFeildVO> bodyFields) {
		this.bodyFields = bodyFields;
	}
	
	
	public void addHeadField(ImportFeildVO importFeildVO) {
		headFields.add(importFeildVO);
	}
	public void addBodyFields(ImportFeildVO importFeildVO) {
		bodyFields.add(importFeildVO);
	}
	
}
