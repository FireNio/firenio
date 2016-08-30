package com.generallycloud.nio.extend.plugin.jms.decode;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.MapByteMessage;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class MapByteMessageDecoder implements MessageDecoder{

	public Message decode(NIOReadFuture future) {
		Parameters param = future.getParameters();
		String messageID = param.getParameter("msgID");
		String queueName = param.getParameter("queueName");
		JSONObject map = param.getJSONObject("map");
		
		BufferedOutputStream outputStream = (BufferedOutputStream) future.getOutputStream();
		
		byte[] array = outputStream.toByteArray();
		
		return new MapByteMessage(messageID,queueName,map,array);
	}
}
