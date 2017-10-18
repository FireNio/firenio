/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.container.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class MapByteMessage extends MapMessage implements MappedMessage, BytedMessage {

    private byte[] array;

    public MapByteMessage(String messageId, String queueName, JSONObject map, byte[] array) {
        super(messageId, queueName, map);
        this.array = array;
    }

    public MapByteMessage(String messageId, String queueName, byte[] array) {
        super(messageId, queueName);
        this.array = array;
    }

    @Override
    public byte[] getByteArray() {
        return array;
    }

    @Override
    public String toString() {
        return new StringBuilder(24).append("{\"msgType\":5,\"msgId\":\"").append(getMsgId())
                .append("\",\"queueName\":\"").append(getQueueName()).append("\",\"timestamp\":")
                .append(getTimestamp()).append(",\"map\":").append(getText0()).append("}")
                .toString();
    }

    @Override
    public int getMsgType() {
        return Message.TYPE_MAP_BYTE;
    }

    public static void main(String[] args) {

        MapByteMessage message = new MapByteMessage("mid", "qname", null);

        message.put("aaa", "aaa1111");

        String str = message.toString();

        System.out.println(str);

        JSON.parseObject(str);

        System.out.println();
    }
}
