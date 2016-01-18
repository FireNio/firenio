package test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.FileUtil;

public class TestSimpleBigParam {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestSimpleServlet";
		Map param = ClientUtil.getParamMap();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		String temp = "网易科技腾讯科技阿里巴巴";
		StringBuilder builder = new StringBuilder(temp);
		for (int i = 0; i < 600000; i++) {
			builder.append("\n");
			builder.append(temp);
		}
		param.put("param", builder.toString());
		String paramString = JSONObject.toJSONString(param);
		client.request(serviceKey, paramString);
		Response response = client.request(serviceKey, paramString);
		client.close();
		FileUtil.write(new File(TestSimpleBigParam.class.getName()), response.getContent());
		System.out.println("处理完成");
	}
}
