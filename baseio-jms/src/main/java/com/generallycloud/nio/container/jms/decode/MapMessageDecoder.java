package com.generallycloud.nio.container.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.container.jms.MapMessage;
import com.generallycloud.nio.container.jms.Message;

public class MapMessageDecoder implements MessageDecoder {

	public Message decode(ProtobaseReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		return new MapMessage(messageID, queueName, map);
	}
}
