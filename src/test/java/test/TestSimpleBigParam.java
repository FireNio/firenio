package test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.common.FileUtil;

public class TestSimpleBigParam {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestSimpleServlet";
		Map param = ClientUtil.getParamMap();
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		String temp = "网易科技腾讯科技阿里巴巴";
		StringBuilder builder = new StringBuilder(temp);
		for (int i = 0; i < 600000; i++) {
			builder.append("\n");
			builder.append(temp);
		}
		param.put("param", builder.toString());
		String paramString = JSONObject.toJSONString(param);
		Response response = session.request(serviceKey, paramString);
		FileUtil.write(new File(TestSimpleBigParam.class.getName()), response.getContent());
		System.out.println("处理完成");
		
		CloseUtil.close(connector);
		
		
		
	}
}
