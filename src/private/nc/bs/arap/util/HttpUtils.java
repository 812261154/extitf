package nc.bs.arap.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	public static String httpPostWithJSON(JSONArray jsonArray) throws Exception {
		JSONObject json = new JSONObject();
		json.put("MAC", "14187755FA4D");
		json.put("TIME", DateUtils.getCurrTime());
		json.put("LST_BILL_STATUS", jsonArray);
		
		String url = FileUtils.getProperties("nc/bs/arap/properties/ArapWsPrams.properties", "url");
		
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000);
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader(HTTP.CONTENT_TYPE, "application/json; charset=UTF-8");
        
        // 将JSON进行UTF-8编码,以便传输中文
        StringEntity se = new StringEntity(json.toString(), "UTF-8");
        se.setContentEncoding("UTF-8");
        se.setContentType("application/json");
        
        httpPost.setEntity(se);
        HttpResponse resp = httpClient.execute(httpPost);
        return EntityUtils.toString(resp.getEntity());
    }
}
