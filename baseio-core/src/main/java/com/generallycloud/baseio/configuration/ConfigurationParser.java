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
package com.generallycloud.baseio.configuration;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.generallycloud.baseio.common.Properties;

/**
 * @author wangkai
 *
 */
public class ConfigurationParser {

    public static void parseConfiguration(String prefix, Object cfg, Properties properties)
            throws Exception {
        String setPrefix = "set" + prefix + "_";
        String prefixDot = prefix + ".";
        Method[] methods = cfg.getClass().getDeclaredMethods();
        for (Method method : methods) {
            String name = method.getName();
            if (!name.startsWith("set")) {
                continue;
            }
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            Class<?> type = method.getParameterTypes()[0];
            String temp = name.replace(setPrefix, prefixDot);
            if (type == String.class) {
                method.invoke(cfg, properties.getProperty(temp));
            } else if (type == int.class) {
                method.invoke(cfg, properties.getIntegerProperty(temp));
            } else if (type == double.class) {
                method.invoke(cfg, properties.getDoubleProperty(temp));
            } else if (type == boolean.class) {
                method.invoke(cfg, properties.getBooleanProperty(temp));
            } else if (type == long.class) {
                method.invoke(cfg, properties.getLongProperty(temp));
            } else if (type == Charset.class) {
                method.invoke(cfg, Charset.forName(properties.getProperty(temp, "GBK")));
            } else {
                throw new Exception("unknow type " + type);
            }
        }

    }

}
