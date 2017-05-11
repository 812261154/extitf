package nc.itf.arap.parm;

public class ImportFeildVO {
	
	public static final int VARCHAR = 1;
	public static final int NUMBER = 2;
	public static final int CHAR = 3;
	public static final int DATE = 4;
	public static final int DATETIME = 4;
	
	private String code;
	private String name;
	private boolean nullable = true;
	private int dataType = 1;
	
	
	public ImportFeildVO(String code, String name) {
		super();
		this.code = code;
		this.name = name;
	}
	
	public ImportFeildVO(String code, String name, boolean nullable, int dataType) {
		super();
		this.code = code;
		this.name = name;
		this.nullable = nullable;
		this.dataType = dataType;
	}

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
}
