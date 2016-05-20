package com.gifisan.nio.plugin.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.MapMessage;
import com.gifisan.nio.plugin.jms.Message;

public class MapMessageDecoder implements MessageDecoder {

	public Message decode(ReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		return new MapMessage(messageID, queueName, map);
	}
}
