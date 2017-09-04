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
package com.generallycloud.baseio.container.jms.client.cmd;

import java.util.HashMap;

import com.generallycloud.baseio.container.jms.client.MessageBrowser;
import com.generallycloud.baseio.container.jms.cmd.CmdResponse;
import com.generallycloud.baseio.container.jms.cmd.CommandContext;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

@Deprecated
public class MessageSizeExecutable extends MQCommandExecutor {

    private Logger logger = LoggerFactory.getLogger(MessageSizeExecutable.class);

    @Override
    public CmdResponse exec(CommandContext context, HashMap<String, String> params) {

        CmdResponse response = new CmdResponse();

        MessageBrowser browser = getMessageBrowser(context);

        if (browser == null) {
            response.setResponse("请先登录！");
            return response;
        }

        try {
            int size = browser.size();
            response.setResponse(String.valueOf(size));
        } catch (Exception e) {
            response.setResponse(e.getMessage());
            logger.debug(e);
        }

        return response;
    }
}
