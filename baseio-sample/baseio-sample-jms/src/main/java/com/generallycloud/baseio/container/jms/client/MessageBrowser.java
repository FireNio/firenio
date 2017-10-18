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
package com.generallycloud.baseio.container.jms.client;

import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.Message;

public interface MessageBrowser {

    /**
     * 1.消息中有重复的id则browser到的消息为后者</BR>
     * 2.不允许browser到处于事务中的消息</BR>
     * 3.本方法用于检查服务器是否依然存在此id的消息
     * @param messageId
     * @return
     * @throws MQException
     */
    public abstract Message browser(String messageId) throws MQException;

    public abstract int size() throws MQException;

    public abstract boolean isOnline(String queueName) throws MQException;

}
