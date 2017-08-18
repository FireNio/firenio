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

import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class MessageWriterJob implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MessageWriterJob.class);
    private Consumer      consumer;
    private Message       message;
    private MQContext     context;

    public MessageWriterJob(MQContext context, Consumer consumer, Message message) {
        this.context = context;
        this.consumer = consumer;
        this.message = message;
    }

    @Override
    public void run() {
        try {

            consumer.push(message);

            context.consumerMessage(message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 回炉
            context.offerMessage(message);

        }

    }

}
