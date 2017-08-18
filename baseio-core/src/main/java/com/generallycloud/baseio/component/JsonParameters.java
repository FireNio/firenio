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
package com.generallycloud.baseio.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.common.StringUtil;

public class JsonParameters implements Parameters {

    private JSONObject jsonObject;

    private String     json;

    public JsonParameters(String json) {
        if (!StringUtil.isNullOrBlank(json)) {
            try {
                jsonObject = JSON.parseObject(json);
            } catch (Exception e) {
                throw new IllegalArgumentException(json, e);
            }
            this.json = json;
        } else {
            this.jsonObject = new JSONObject();
        }
    }

    public JsonParameters(JSONObject object) {
        this.jsonObject = object;
    }

    public JsonParameters() {
        this(new JSONObject());
    }

    @Override
    public boolean getBooleanParameter(String key) {
        if (jsonObject == null) {
            return false;
        }
        return jsonObject.getBooleanValue(key);
    }

    @Override
    public int getIntegerParameter(String key) {
        return getIntegerParameter(key, 0);
    }

    @Override
    public int getIntegerParameter(String key, int defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }
        int value = jsonObject.getIntValue(key);
        if (value == 0) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public long getLongParameter(String key) {
        return getLongParameter(key, 0);
    }

    @Override
    public long getLongParameter(String key, long defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }
        long value = jsonObject.getLongValue(key);
        if (value == 0) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public Object getObjectParameter(String key) {
        if (jsonObject == null) {
            return null;
        }
        return jsonObject.get(key);
    }

    @Override
    public String getParameter(String key) {
        return getParameter(key, null);
    }

    @Override
    public String getParameter(String key, String defaultValue) {
        if (jsonObject == null) {
            return defaultValue;
        }
        String value = jsonObject.getString(key);
        if (StringUtil.isNullOrBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String toString() {
        if (json == null) {
            json = jsonObject.toJSONString();
        }
        return json;
    }

    @Override
    public JSONObject getJSONObject(String key) {
        return jsonObject.getJSONObject(key);
    }

    @Override
    public JSONArray getJSONArray(String key) {
        return jsonObject.getJSONArray(key);
    }

    @Override
    public int size() {
        return jsonObject.size();
    }

    @Override
    public JSONObject getJsonObject() {
        return jsonObject;
    }

}
