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

import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.common.StringUtil;

public class MapMessage extends BasicMessage implements MappedMessage {

    private JSONObject map = null;

    public MapMessage(String messageId, String queueName) {
        super(messageId, queueName);
        this.map = new JSONObject();
    }

    public MapMessage(String messageId, String queueName, JSONObject map) {
        super(messageId, queueName);
        this.map = map;
    }

    public boolean getBooleanParameter(String key) {
        if (map == null) {
            return false;
        }
        return map.getBooleanValue(key);
    }

    public int getIntegerParameter(String key) {
        return getIntegerParameter(key, 0);
    }

    public int getIntegerParameter(String key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        int value = map.getIntValue(key);
        if (value == 0) {
            return defaultValue;
        }
        return value;
    }

    public String getEventName() {
        return getParameter("eventName");
    }

    public void setEventName(String eventName) {
        this.put("eventName", eventName);
    }

    public JSONArray getJSONArray(String key) {
        return map.getJSONArray(key);
    }

    public JSONObject getJSONObject(String key) {
        return map.getJSONObject(key);
    }

    public long getLongParameter(String key) {
        return getLongParameter(key, 0);
    }

    public long getLongParameter(String key, long defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        long value = map.getLongValue(key);
        if (value == 0) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public int getMsgType() {
        return Message.TYPE_MAP;
    }

    public Object getObjectParameter(String key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public String getParameter(String key) {
        return getParameter(key, null);
    }

    public String getParameter(String key, String defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        String value = map.getString(key);
        if (StringUtil.isNullOrBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    protected String getText0() {
        return map.toJSONString();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void put(Map value) {
        this.map.putAll(value);
    }

    @Override
    public void put(String key, Object value) {
        this.map.put(key, value);
    }

    @Override
    public String toString() {
        return new StringBuilder(24).append("{\"msgType\":4,\"msgId\":\"").append(getMsgId())
                .append("\",\"queueName\":\"").append(getQueueName()).append("\",\"timestamp\":")
                .append(getTimestamp()).append(",\"map\":").append(getText0()).append("}")
                .toString();
    }

    public static void main(String[] args) {

        MapMessage message = new MapMessage("mid", "qname");

        message.put("aaa", "aaa1111");

        String str = message.toString();

        System.out.println(str);

        JSON.parseObject(str);

        System.out.println();

    }
}
