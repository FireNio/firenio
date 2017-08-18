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

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.AbstractEventLoop;
import com.generallycloud.baseio.container.ApplicationContextUtil;
import com.generallycloud.baseio.container.authority.Authority;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class AbstractProductLine extends AbstractEventLoop implements MessageQueue {

    protected MQContext                  context;
    protected MessageStorage             storage;
    protected long                       dueTime;
    protected Map<String, ConsumerQueue> consumerMap;
    private Logger                       logger = LoggerFactory.getLogger(getClass());

    public AbstractProductLine(MQContext context) {

        this.context = context;

        this.storage = new MessageStorage();

        this.consumerMap = new HashMap<>();

        this.dueTime = context.getMessageDueTime();
    }

    // TODO 处理剩下的message 和 receiver
    @Override
    protected void doStop() {}

    public MQContext getContext() {
        return context;
    }

    @Override
    public void pollMessage(SocketSession session, ProtobaseFuture future,
            MQSessionAttachment attachment) {

        if (attachment.getConsumer() != null) {
            return;
        }

        Authority authority = ApplicationContextUtil.getAuthority(session);

        String queueName = authority.getUuid();

        // 来自终端类型
        context.addReceiver(queueName);

        ConsumerQueue consumerQueue = getConsumerQueue(queueName);

        Consumer consumer = new Consumer(consumerQueue, attachment, session, future, queueName);

        attachment.setConsumer(consumer);

        consumerQueue.offer(consumer);
    }

    protected ConsumerQueue getConsumerQueue(String queueName) {

        ConsumerQueue consumerQueue = consumerMap.get(queueName);

        if (consumerQueue == null) {

            synchronized (consumerMap) {

                consumerQueue = consumerMap.get(queueName);

                if (consumerQueue == null) {
                    consumerQueue = createConsumerQueue();
                    consumerMap.put(queueName, consumerQueue);
                }
            }
        }
        return consumerQueue;
    }

    protected abstract ConsumerQueue createConsumerQueue();

    @Override
    public void offerMessage(Message message) {

        storage.offer(message);
    }

    protected void filterUseless(Message message) {
        long now = System.currentTimeMillis();
        long dueTime = this.dueTime;

        if (now - message.getTimestamp() > dueTime) {
            // 消息过期了
            logger.debug(">>>> message invalidate : {}", message);
            return;
        }
        this.offerMessage(message);
    }

    public void setDueTime(long dueTime) {
        this.dueTime = dueTime;
    }
}
