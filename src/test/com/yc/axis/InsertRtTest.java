package com.yc.axis;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

//import nc.bs.dao.BaseDAO;
//import nc.bs.dao.DAOException;
//import nc.itf.arap.parm.ArapImportResultVO;
//import nc.itf.arap.parm.ArapImportResultVOMeta;

public class InsertRtTest {
	public static void main(String[] args) {
		System.out.println("==================");
//		BaseDAO baseDAO = new BaseDAO();
//		ArapImportResultVO[] vos = new ArapImportResultVO[2700];
//		for(int i = 0; i<vos.length; i++) {
//			ArapImportResultVO vo = new ArapImportResultVO();
//			vo.setFromsys("FROMSYS");
//			vo.setNumber("NUMBER");
//			vo.setCompleted("COMPLETED");
//			vo.setMessage("MESSAGE");
//		}
//		try {
//			System.out.print(baseDAO.insertObject(vos, new ArapImportResultVOMeta()));
//		} catch (DAOException e) {
//			e.printStackTrace();
//		}
		
		GsonBuilder gb = new GsonBuilder();
		gb.setPrettyPrinting();
		//添加修改名称的回调函数，功能同@SerializedName("NAME")
		gb.setFieldNamingStrategy(new FieldNamingStrategy() {
			@Override
			public String translateName(Field paramField) {
				if(StringUtils.equals(paramField.getName(), "name")) {
					return "NAME";
				}
				return paramField.getName();
			}
		});
		Gson gson = gb.create();
		System.out.println(gson.toString());
	}
}
