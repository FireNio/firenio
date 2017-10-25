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
package com.generallycloud.baseio.container.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.jms.JmsUtil;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.client.MessageBrowser;
import com.generallycloud.baseio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.baseio.container.jms.decode.MessageDecoder;
import com.generallycloud.baseio.container.jms.server.MQBrowserServlet;

public class DefaultMessageBrowser implements MessageBrowser {

    private final String   SERVICE_NAME   = "MQBrowserServlet";

    private MessageDecoder messageDecoder = new DefaultMessageDecoder();

    private FixedSession   session        = null;

    public DefaultMessageBrowser(FixedSession session) {
        this.session = session;
    }

    @Override
    public Message browser(String messageId) throws MQException {
        JSONObject param = new JSONObject();
        param.put("messageId", messageId);
        param.put("cmd", MQBrowserServlet.BROWSER);

        ProtobaseFuture future;
        try {
            future = session.request(SERVICE_NAME, param.toJSONString());
        } catch (IOException e) {
            throw new MQException(e.getMessage(), e);
        }

        return messageDecoder.decode(future);
    }

    @Override
    public int size() throws MQException {
        String param = "{cmd:\"0\"}";

        ProtobaseFuture future;
        try {
            future = session.request(SERVICE_NAME, param);
        } catch (IOException e) {
            throw new MQException(e.getMessage(), e);
        }
        return Integer.parseInt(future.getReadText());
    }

    @Override
    public boolean isOnline(String queueName) throws MQException {

        JSONObject param = new JSONObject();
        param.put("queueName", queueName);
        param.put("cmd", MQBrowserServlet.ONLINE);

        ProtobaseFuture future;
        try {
            future = session.request(SERVICE_NAME, param.toJSONString());
        } catch (IOException e) {
            throw new MQException(e.getMessage(), e);
        }

        return JmsUtil.isTrue(future);
    }
}
