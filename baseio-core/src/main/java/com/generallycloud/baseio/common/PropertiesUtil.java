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
package com.generallycloud.baseio.common;

/**
 * @author wangkai
 *
 */
public class PropertiesUtil {

    public static void setSystemPropertiesIfNull(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    public static boolean isSystemTrue(String key) {
        String v = System.getProperty(key);
        if (v == null) {
            return false;
        }
        v = v.toLowerCase();
        return "true".equals(v) || "1".equals(v);
    }

    public static int getProperty(String key) {
        return getProperty(key, 0);
    }

    public static int getProperty(String key, int defaultValue) {
        String v = System.getProperty(key);
        if (!StringUtil.isNullOrBlank(v)) {
            try {
                return Integer.parseInt(v);
            } catch (Exception e) {}
        }
        return defaultValue;
    }

}
