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
package com.generallycloud.test.others;

import com.generallycloud.baseio.log.DebugUtil;

/**
 * @author wangkai
 *
 */
public class TestFor {

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        int time = 2000000000;

        //		testWhile1(time);
        //		testWhile2(time);
        testWhile3(time);

        DebugUtil.info("Time:{}", System.currentTimeMillis() - startTime);

    }

    static void testWhile1(long time) {
        int i = 0;
        while (i++ < time) {}
    }

    static void testWhile2(long time) {
        int i = 0;
        while (i < time) {
            i++;
        }
    }

    static void testWhile3(long time) {
        int i = 0;
        while (++i < time) {}
    }

}
