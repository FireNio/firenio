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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface Parameters {

    public abstract boolean getBooleanParameter(String key);

    public abstract int getIntegerParameter(String key);

    public abstract int getIntegerParameter(String key, int defaultValue);

    public abstract JSONArray getJSONArray(String key);

    public abstract JSONObject getJSONObject(String key);

    public abstract long getLongParameter(String key);

    public abstract long getLongParameter(String key, long defaultValue);

    public abstract Object getObjectParameter(String key);

    public abstract String getParameter(String key);

    public abstract String getParameter(String key, String defaultValue);

    public abstract int size();

    public abstract JSONObject getJsonObject();
}
