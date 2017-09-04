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
package com.generallycloud.baseio.container.jms.server;

import com.generallycloud.baseio.container.RESMessage;

public class MQRESMessage {

    public static int        CODE_TRANSACTION_BEGINED   = 901;
    public static int        CODE_TRANSACTION_NOT_BEGIN = 902;
    public static int        CODE_TRANSACTION_UNAUTH    = 903;
    public static int        CODE_CMD_NOT_FOUND         = 904;

    public static RESMessage R_TRANSACTION_BEGINED      = new RESMessage(CODE_TRANSACTION_BEGINED,
            "transaction begined");
    public static RESMessage R_TRANSACTION_NOT_BEGIN    = new RESMessage(CODE_TRANSACTION_NOT_BEGIN,
            "transaction not begin");
    public static RESMessage R_UNAUTH                   = new RESMessage(CODE_TRANSACTION_UNAUTH,
            "unauthorized");
    public static RESMessage R_CMD_NOT_FOUND            = new RESMessage(CODE_CMD_NOT_FOUND,
            "cmd not found");

}
