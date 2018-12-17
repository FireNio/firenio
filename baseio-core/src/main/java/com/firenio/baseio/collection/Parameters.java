/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.collection;

import java.util.Map;

public interface Parameters {

    boolean getBooleanParameter(String key);

    int getIntegerParameter(String key);

    int getIntegerParameter(String key, int defaultValue);

    long getLongParameter(String key);
    
    long getLongParameter(String key, long defaultValue);
    
    Map<String,Object> getMap();

    Object getObjectParameter(String key);

    String getParameter(String key);

    String getParameter(String key, String defaultValue);

    void put(String key,Object value);

    void putAll(Map<String,Object> params);

    int size();

}
