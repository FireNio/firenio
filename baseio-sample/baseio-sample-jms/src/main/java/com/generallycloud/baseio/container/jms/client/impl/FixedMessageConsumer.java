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

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.jms.ErrorMessage;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.MapByteMessage;
import com.generallycloud.baseio.container.jms.MapMessage;
import com.generallycloud.baseio.container.jms.Message;
import com.generallycloud.baseio.container.jms.NullMessage;
import com.generallycloud.baseio.container.jms.TextByteMessage;
import com.generallycloud.baseio.container.jms.TextMessage;
import com.generallycloud.baseio.container.jms.client.MessageConsumer;
import com.generallycloud.baseio.container.jms.client.OnMessage;

public class FixedMessageConsumer implements OnMessage, MessageConsumer {

    private Map<String, OnMappedMessage> onMappedMessages = new HashMap<>();

    private OnNullMessage                onNullMessage;

    private OnErrorMessage               onErrorMessage;

    private OnTextByteMessage            onTextByteMessage;

    private OnTextMessage                onTextMessage;

    private MessageConsumer              messageConsumer;

    public FixedMessageConsumer(FixedSession session) {
        this.messageConsumer = new DefaultMessageConsumer(session);
    }

    @Override
    public void onReceive(Message message) {

        int msgType = message.getMsgType();

        if (Message.TYPE_MAP == msgType) {

            MapMessage m = (MapMessage) message;

            String eventName = m.getParameter("eventName");

            OnMappedMessage onMessage = onMappedMessages.get(eventName);

            if (onMessage == null) {
                return;
            }

            onMessage.onReceive(m);

        } else if (Message.TYPE_MAP_BYTE == msgType) {

            MapByteMessage m = (MapByteMessage) message;

            String eventName = m.getParameter("eventName");

            OnMappedMessage onMessage = onMappedMessages.get(eventName);

            if (onMessage == null) {
                return;
            }

            onMessage.onReceive(m);

        } else if (Message.TYPE_TEXT == msgType) {

            if (onTextMessage != null) {
                onTextMessage.onReceive((TextMessage) message);
            }

        } else if (Message.TYPE_TEXT_BYTE == msgType) {

            if (onTextByteMessage != null) {
                onTextByteMessage.onReceive((TextByteMessage) message);
            }

        } else if (Message.TYPE_ERROR == msgType) {

            if (onErrorMessage != null) {
                onErrorMessage.onReceive((ErrorMessage) message);
            }

        } else if (Message.TYPE_NULL == msgType) {

            if (onNullMessage != null) {
                onNullMessage.onReceive((NullMessage) message);
            }

        }
    }

    public void listenTextMessage(OnTextMessage onTextMessage) {
        this.onTextMessage = onTextMessage;
    }

    public void listenTextByteMessage(OnTextByteMessage onTextByteMessage) {
        this.onTextByteMessage = onTextByteMessage;
    }

    public void listenErrorMessage(OnErrorMessage onErrorMessage) {
        this.onErrorMessage = onErrorMessage;
    }

    public void listenNullMessage(OnNullMessage onNullMessage) {
        this.onNullMessage = onNullMessage;
    }

    public void listen(String eventName, OnMappedMessage onMapByteMessage) {
        this.onMappedMessages.put(eventName, onMapByteMessage);
    }

    @Override
    public boolean beginTransaction() throws MQException {
        return messageConsumer.beginTransaction();
    }

    @Override
    public boolean commit() throws MQException {
        return messageConsumer.commit();
    }

    @Override
    public boolean rollback() throws MQException {
        return messageConsumer.rollback();
    }

    @Override
    public void receive(OnMessage onMessage) throws MQException {

        if (onMessage != null) {
            throw new MQException("");
        }

        messageConsumer.receive(this);
    }

    @Override
    public void subscribe(OnMessage onMessage) throws MQException {

        if (onMessage != null) {
            throw new MQException("");
        }

        messageConsumer.subscribe(this);
    }

    public interface OnTextMessage {
        public abstract void onReceive(TextMessage message);
    }

    public interface OnTextByteMessage {
        public abstract void onReceive(TextByteMessage message);
    }

    public interface OnErrorMessage {
        public abstract void onReceive(ErrorMessage message);
    }

    public interface OnNullMessage {
        public abstract void onReceive(NullMessage message);
    }
}
