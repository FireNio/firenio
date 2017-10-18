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

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.jms.ErrorMessage;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.NullMessage;
import com.generallycloud.baseio.container.jms.TextByteMessage;

public class MQBrowserServlet extends MQServlet {

    public static final String SIZE         = "0";

    public static final String BROWSER      = "1";

    public static final String ONLINE       = "2";

    public static final String SERVICE_NAME = MQBrowserServlet.class.getSimpleName();

    @Override
    public void doAccept(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) throws Exception {

        Parameters param = future.getParameters();

        String messageId = param.getParameter("messageId");

        Message message = NullMessage.NULL_MESSAGE;

        MQContext context = getMQContext();

        String cmd = param.getParameter("cmd");
        if (StringUtil.isNullOrBlank(cmd)) {
            message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
        } else {

            if (SIZE.equals(cmd)) {

                future.write(String.valueOf(context.messageSize()));

            } else if (BROWSER.equals(cmd)) {

                if (!StringUtil.isNullOrBlank(messageId)) {

                    message = context.browser(messageId);

                    if (message == null) {

                        message = NullMessage.NULL_MESSAGE;

                        future.write(message.toString());
                    } else {

                        int msgType = message.getMsgType();

                        String content = message.toString();

                        future.write(content);

                        if (msgType == 3) {

                            TextByteMessage byteMessage = (TextByteMessage) message;

                            byte[] bytes = byteMessage.getByteArray();

                            future.writeBinary(bytes);
                        }
                    }
                }
            } else if (ONLINE.equals(cmd)) {

                boolean bool = context.isOnLine(param.getParameter("queueName"));

                future.write(String.valueOf(bool));
            }
        }

        session.flush(future);
    }

}
