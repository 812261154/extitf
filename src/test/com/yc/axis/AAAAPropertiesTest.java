package com.yc.axis;

import nc.bs.arap.util.FileUtils;
import nc.vo.pub.BusinessException;

/**
 * @author Administrator
 *
 */
public class AAAAPropertiesTest {
	public static void main(String[] args) throws BusinessException {
		System.out.println(FileUtils.getProperties("nc/bs/arap/properties/PMPTGPram.properties", "BookstoreOrg"));
	}
}
