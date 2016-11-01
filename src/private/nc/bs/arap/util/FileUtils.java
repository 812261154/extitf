package nc.bs.arap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import nc.bs.framework.common.NCLocator;
import nc.login.bs.IServerEnvironmentService;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDateTime;
import net.sf.json.JSONArray;

public class FileUtils {
	//删除指定文件夹下所有文件
	//param path 文件夹完整绝对路径
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.getAbsoluteFile().delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}
	
	//删除指定文件夹下所有文件
	//param path 文件夹完整绝对路径
	public static void delFiles(String path, String[] filenames) {
		File file = new File(path);
		if (!file.exists() || !file.isDirectory()) {
			return;
		}
		File temp = null;
		for (int i = 0; i < filenames.length; i++) {
			temp = new File(path + File.separator + filenames[i]);
			if (temp.exists() && temp.isFile()) {
				temp.getAbsoluteFile().delete();
			}
		}
	}
	
	// 删除文件夹
	// param folderPath 文件夹完整绝对路径
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.getAbsoluteFile().delete(); // 删除空文件夹
		} catch (Exception e) {
		}
	}
	
	//从数据缓存文件中读取单据信息(JSONArray)
	public static List<JSONArray> deserializeFromFile(String baseFilePath) throws BusinessException {
		List<JSONArray> arrayList = new ArrayList<JSONArray>();
		IServerEnvironmentService service = NCLocator.getInstance().lookup(IServerEnvironmentService.class);
		UFDateTime serverTime = service.getServerTime();
		List<String> filenames = new ArrayList<String>();
		ObjectInputStream ois = null;
		try {
			//去当天和前一天的数据
			String[] filePathStrs = {
					baseFilePath + File.separator + DateUtils.formatDateFileStr(serverTime),
					baseFilePath + File.separator + DateUtils.getDateFileStrBefore(serverTime, -1)
			};
			for(String filePathStr : filePathStrs) {
				File filePath = new File(filePathStr);
				if(filePath.exists() && filePath.isDirectory()) {
					File[] fileList = filePath.listFiles();
					for(File file : fileList) {
						filenames.add(file.getName());
						if(ois != null) {
							ois.close();
						}
						ois = new ObjectInputStream(new FileInputStream(file));
						JSONArray array = (JSONArray)ois.readObject();
						arrayList.add(array);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException("反序列化失败，"+e.getMessage());
		} finally {
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			//已被提取的单据需要删除
			FileUtils.delFiles(baseFilePath + File.separator + DateUtils.formatDateFileStr(serverTime), (String[])filenames.toArray(new String[0]));
			FileUtils.delFiles(baseFilePath + File.separator + DateUtils.getDateFileStrBefore(serverTime, -1), (String[])filenames.toArray(new String[0]));
			//前两天的文件夹删除
			FileUtils.delFolder(baseFilePath + File.separator + DateUtils.getDateFileStrBefore(serverTime, -2));
		}
		return arrayList;
	}
	
	/**
	 * 把vo对象序列化到本地文件中
	 * 
	 * @param vos
	 * @throws BusinessException 
	 */
	public static void serializeJSONArrayToFile(JSONArray array, String baseFilePath) throws BusinessException {
		if(array == null || array.size() == 0) {
			return;
		}
		ObjectOutputStream oo = null;
		try {
			IServerEnvironmentService service = NCLocator.getInstance().lookup(IServerEnvironmentService.class);
			UFDateTime serverTime = service.getServerTime();
			String filePathStr = baseFilePath + DateUtils.formatDateFileStr(serverTime);
			File filePath = new File(filePathStr);
			if(!filePath.exists() && !filePath.isDirectory()) {
				filePath.mkdir();
			}
			String filename = filePathStr + "/temp_" + BillUtils.getUUID() + ".txt";
			oo = new ObjectOutputStream(new FileOutputStream(new File(filename)));
			oo.writeObject(array);
		} catch (Exception e) {
			throw new BusinessException("序列化失败，"+e.getMessage());
		} finally {
			if(oo != null)
				try {
					oo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	//取配置文件中的参数
	public static String getProperties(String filenpath, String key) throws BusinessException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(TransUtils.class.getClassLoader().getResource(filenpath).getPath());
			Properties p = new Properties();
			p.load(in);
			return p.getProperty(key);
		} catch (Exception e) {
			throw new BusinessException("配置文件读取出错！");
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
