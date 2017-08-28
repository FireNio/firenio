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
package com.generallycloud.test.others.json;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author wangkai
 *
 */
public class JacksonTest {

    public static void main(String[] args) throws Exception {

        //JSON input
        String json = "{\"id\":1,\"name\":\"Lokesh Gupta\",\"age\":34,\"location\":\"India\"}";

        //Object mapper instance
        ObjectMapper mapper = new ObjectMapper();

        Map v = mapper.readValue(json, Map.class);

        System.out.println(v);
    }

}
