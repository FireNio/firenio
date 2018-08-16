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
package com.generallycloud.baseio.collection;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.StringUtil;

public class MapParameters implements Parameters {

    private Map<String, Object> map;

    public MapParameters(Map<String, Object> object) {
        this.map = object;
    }

    public MapParameters() {
        this(new HashMap<String, Object>());
    }

    @Override
    public boolean getBooleanParameter(String key) {
        if (map == null) {
            return false;
        }
        return (boolean) map.get(key);
    }

    @Override
    public int getIntegerParameter(String key) {
        return getIntegerParameter(key, 0);
    }

    @Override
    public int getIntegerParameter(String key, int defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Integer value = (Integer) map.get(key);
        if (value == null) {
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
        if (map == null) {
            return defaultValue;
        }
        Long value = (Long) map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public Object getObjectParameter(String key) {
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public String getParameter(String key) {
        return getParameter(key, null);
    }

    @Override
    public String getParameter(String key, String defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        String value = (String) map.get(key);
        if (StringUtil.isNullOrBlank(value)) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public void put(String key, Object value) {
        map.put(key, value);
    }

    @Override
    public void putAll(Map<String, Object> params) {
        map.putAll(params);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Map<String, Object> getMap() {
        return map;
    }

}
