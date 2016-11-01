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
	//ɾ��ָ���ļ����������ļ�
	//param path �ļ�����������·��
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
				delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
				delFolder(path + "/" + tempList[i]);// ��ɾ�����ļ���
				flag = true;
			}
		}
		return flag;
	}
	
	//ɾ��ָ���ļ����������ļ�
	//param path �ļ�����������·��
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
	
	// ɾ���ļ���
	// param folderPath �ļ�����������·��
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // ɾ����������������
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.getAbsoluteFile().delete(); // ɾ�����ļ���
		} catch (Exception e) {
		}
	}
	
	//�����ݻ����ļ��ж�ȡ������Ϣ(JSONArray)
	public static List<JSONArray> deserializeFromFile(String baseFilePath) throws BusinessException {
		List<JSONArray> arrayList = new ArrayList<JSONArray>();
		IServerEnvironmentService service = NCLocator.getInstance().lookup(IServerEnvironmentService.class);
		UFDateTime serverTime = service.getServerTime();
		List<String> filenames = new ArrayList<String>();
		ObjectInputStream ois = null;
		try {
			//ȥ�����ǰһ�������
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
			throw new BusinessException("�����л�ʧ�ܣ�"+e.getMessage());
		} finally {
			if(ois != null)
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			//�ѱ���ȡ�ĵ�����Ҫɾ��
			FileUtils.delFiles(baseFilePath + File.separator + DateUtils.formatDateFileStr(serverTime), (String[])filenames.toArray(new String[0]));
			FileUtils.delFiles(baseFilePath + File.separator + DateUtils.getDateFileStrBefore(serverTime, -1), (String[])filenames.toArray(new String[0]));
			//ǰ������ļ���ɾ��
			FileUtils.delFolder(baseFilePath + File.separator + DateUtils.getDateFileStrBefore(serverTime, -2));
		}
		return arrayList;
	}
	
	/**
	 * ��vo�������л��������ļ���
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
			throw new BusinessException("���л�ʧ�ܣ�"+e.getMessage());
		} finally {
			if(oo != null)
				try {
					oo.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	//ȡ�����ļ��еĲ���
	public static String getProperties(String filenpath, String key) throws BusinessException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(TransUtils.class.getClassLoader().getResource(filenpath).getPath());
			Properties p = new Properties();
			p.load(in);
			return p.getProperty(key);
		} catch (Exception e) {
			throw new BusinessException("�����ļ���ȡ����");
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
