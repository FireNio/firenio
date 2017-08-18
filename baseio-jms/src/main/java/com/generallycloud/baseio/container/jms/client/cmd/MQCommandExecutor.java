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

import com.generallycloud.baseio.connector.ChannelConnector;
import com.generallycloud.baseio.connector.SocketChannelConnector;
import com.generallycloud.baseio.container.jms.client.MessageBrowser;
import com.generallycloud.baseio.container.jms.cmd.CommandContext;
import com.generallycloud.baseio.container.jms.cmd.Executable;

@Deprecated
public abstract class MQCommandExecutor implements Executable {

    private String KEY_CONNECTOR = "KEY_CONNECTOR";

    private String KEY_BROWSER   = "KEY_BROWSER";

    protected SocketChannelConnector getClientConnector(CommandContext context) {
        return (SocketChannelConnector) context.getAttribute(KEY_CONNECTOR);
    }

    protected void setClientConnector(CommandContext context, ChannelConnector connector) {
        context.setAttribute(KEY_CONNECTOR, connector);
    }

    protected MessageBrowser getMessageBrowser(CommandContext context) {
        return (MessageBrowser) context.getAttribute(KEY_BROWSER);
    }

    protected void setMessageBrowser(CommandContext context, MessageBrowser connector) {
        context.setAttribute(KEY_BROWSER, connector);
    }

}
