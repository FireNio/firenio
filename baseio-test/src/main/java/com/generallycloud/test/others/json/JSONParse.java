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

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.test.test.ITest;
import com.generallycloud.test.test.ITestHandle;

public class JSONParse {

    private static final Logger logger = LoggerFactory.getLogger(JSONParse.class);

    public static void main(String[] args) throws JSONSyntaxException {

        String text = "{\"a\":\"sss\",\"b\":true,\"c\": false ,\"1     d\":-3.6  ,\"e\":{\"a\":[true,false,1.]},\"f\":{\"a\":\"xx\"}}";
        System.out.println(JSON.stringToMap(text));
        System.out.println(com.alibaba.fastjson.JSON.parseObject(text));
        // System.out.println(text);
        // System.out.println(JSON.stringToMap(text));
        text = "[null,\"a\",-1.6,true,false,[1,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":-3.6,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[1,true,{\"a\":\"aa\"}]}}]]]";
        text = "[   null    ,   {  a   :   \"a\"   ,   b   :   1.   }   ,   1   ]";
        System.out.println(JSON.stringToArray(text));
        //		testFastJson();
        //		testJackson();
        testMyJson();
        System.out.println("1\f11");

    }

    static void testFastJson() {
        ITestHandle.doTest(new ITest() {

            @Override
            public void test(int i) {
                com.alibaba.fastjson.JSON.parseArray(
                        "[\"a\",true,true,false,[true,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":true,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[true,true,{\"a\":\"aa\"}]}}]]");
            }

        }, 1500000, "Fast Json");
    }

    static void testMyJson() {
        ITestHandle.doTest(new ITest() {
            @Override
            public void test(int i) {
                try {
                    JSON.stringToArray(
                            "[\"a\",true,true,false,[true,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":true,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[true,true,{\"a\":\"aa\"}]}}]]");
                } catch (JSONSyntaxException e) {
                    logger.debug(e);
                }
            }
        }, 1500000, "My Json");
    }

    static void testJackson() {

        //Object mapper instance
        ObjectMapper mapper = new ObjectMapper();

        ITestHandle.doTest(new ITest() {
            @Override
            public void test(int i) {
                try {
                    mapper.readValue(
                            "[\"a\",true,true,false,[true,true,{\"a\":\"sss\",\"b\":true,\"c\":false,\"d\":true,\"e\":{\"a\":true},\"f\":{\"a\":\"xx\",\"b\":[true,true,{\"a\":\"aa\"}]}}]]",
                            List.class);
                } catch (Exception e) {
                    logger.debug(e);
                }
            }
        }, 1500000, "Jackson");
    }

}
