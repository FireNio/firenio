package com.gifisan.nio.extend.plugin.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.plugin.jms.MapMessage;
import com.gifisan.nio.extend.plugin.jms.Message;

public class MapMessageDecoder implements MessageDecoder {

	public Message decode(NIOReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		return new MapMessage(messageID, queueName, map);
	}
}
