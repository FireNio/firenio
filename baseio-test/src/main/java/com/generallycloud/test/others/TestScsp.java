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

import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.concurrent.ScmpLinkedQueue;

/**
 * @author wangkai
 *
 */
public class TestScsp {

    public static void main(String[] args) throws Exception {

        ScmpLinkedQueue<String> queue = new ScmpLinkedQueue<>();

        for (int i = 0; i < 4; i++) {
            ThreadUtil.exec(() -> {
                for (int j = 0; j < 32; j++) {
                    queue.offer(String.valueOf(j));
                }
            });
        }
        
//        ThreadUtil.sleep(100);
        System.out.println("size:"+queue.size());
        for(;queue.size() > 0;){
            System.out.println(queue.poll());
        }
    }

}
