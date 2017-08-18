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
package com.generallycloud.baseio.container.jms;

import com.alibaba.fastjson.JSON;

public class NullMessage extends BasicMessage implements Message {

    public static final NullMessage NULL_MESSAGE = new NullMessage();

    private NullMessage() {
        super(null, null);
    }

    @Override
    public int getMsgType() {
        return Message.TYPE_NULL;
    }

    @Override
    public String toString() {
        return "{\"msgType\":1}";
    }

    public static void main(String[] args) {

        System.out.println(JSON.toJSONString(NULL_MESSAGE));
        System.out.println(NULL_MESSAGE.toString());
    }

}
