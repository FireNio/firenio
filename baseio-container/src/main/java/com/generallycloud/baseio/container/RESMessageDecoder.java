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
package com.generallycloud.baseio.container;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.common.StringUtil;

public class RESMessageDecoder {

    public static RESMessage decode(String content) {
        if (StringUtil.isNullOrBlank(content)) {
            return null;
        }
        JSONObject object = JSON.parseObject(content);
        int code = object.getIntValue("code");
        Object data = object.get("data");
        String description = object.getString("description");
        return new RESMessage(code, data, description);
    }
}
