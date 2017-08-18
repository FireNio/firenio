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

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class MQSessionEventListener extends SocketSessionEventListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MQSessionEventListener.class);

    @Override
    public void sessionOpened(SocketSession session) {

        MQContext context = MQContext.getInstance();

        MQSessionAttachment attachment = context.getSessionAttachment(session);

        if (attachment == null) {

            attachment = new MQSessionAttachment(context);

            session.setAttribute(context.getPluginKey(), attachment);
        }
    }

    // FIXME 移除该session上的consumer
    @Override
    public void sessionClosed(SocketSession session) {

        MQContext context = MQContext.getInstance();

        MQSessionAttachment attachment = context.getSessionAttachment(session);

        if (attachment == null) {
            return;
        }

        TransactionSection section = attachment.getTransactionSection();

        if (section != null) {

            section.rollback();
        }

        Consumer consumer = attachment.getConsumer();

        if (consumer != null) {

            consumer.getConsumerQueue().remove(consumer);

            consumer.getConsumerQueue().getSnapshot();

            context.removeReceiver(consumer.getQueueName());
        }

        LOGGER.debug(">>>> TransactionProtectListener execute");

    }

}
