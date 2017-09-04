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

public class ErrorMessage extends BasicMessage implements Message {

    public static int                CODE_UNAUTH;
    public static int                CODE_CMD_NOT_FOUND    = 1;
    public static int                CODE_IOEXCEPTION      = 2;
    private int                      code;
    public static final ErrorMessage UNAUTH_MESSAGE        = new ErrorMessage(CODE_UNAUTH);
    public static final ErrorMessage CMD_NOT_FOUND_MESSAGE = new ErrorMessage(CODE_CMD_NOT_FOUND);
    public static final ErrorMessage IOEXCEPTION           = new ErrorMessage(CODE_IOEXCEPTION);

    public ErrorMessage(int code) {
        super(null, null);
        this.code = code;
    }

    @Override
    public int getMsgType() {
        return Message.TYPE_ERROR;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return new StringBuilder(24).append("{\"msgType\":0,\"code\":").append(code).append("}")
                .toString();
    }

    public static void main(String[] args) {

        ErrorMessage message = new ErrorMessage(CODE_CMD_NOT_FOUND);

        System.out.println(JSON.toJSONString(message));
        System.out.println(message.toString());
    }

}
