package nc.itf.arap.parm;

import nc.jdbc.framework.mapping.IMappingMeta;

public class ArapImportResultVOMeta implements IMappingMeta {
	
	private String[] attributes = new String[] { "pk_importresult", "fromsys", "number", "completed", "message", "ts" };
	private String[] columns = new String[] { "PK_IMPORTRESULT", "FROMSYS", "NUMBER", "COMPLETED", "MESSAGE", "TS" };

	@Override
	public String getPrimaryKey() {
		// TODO Auto-generated method stub
		return "pk_importresult";
	}

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return "arap_importresult";
	}

	@Override
	public String[] getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}

	@Override
	public String[] getColumns() {
		// TODO Auto-generated method stub
		return columns;
	}

}
