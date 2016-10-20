package com.generallycloud.nio.extend.plugin.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.extend.plugin.jms.MapByteMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class MapByteMessageDecoder implements MessageDecoder{

	public Message decode(BaseReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		
		byte[] array = future.getBinary();
		
		return new MapByteMessage(messageID,queueName,map,array);
	}
}
