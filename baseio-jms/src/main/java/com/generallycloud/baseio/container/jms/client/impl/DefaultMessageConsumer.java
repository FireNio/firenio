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

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.RESMessage;
import com.generallycloud.baseio.container.RESMessageDecoder;
import com.generallycloud.baseio.container.WaiterOnFuture;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.client.MessageConsumer;
import com.generallycloud.baseio.container.jms.client.OnMessage;
import com.generallycloud.baseio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.baseio.container.jms.decode.MessageDecoder;
import com.generallycloud.baseio.container.jms.server.MQTransactionServlet;

public class DefaultMessageConsumer implements MessageConsumer {

    private MessageDecoder messageDecoder           = new DefaultMessageDecoder();
    private boolean        needSendReceiveCommand   = true;
    private boolean        needSendSubscribeCommand = true;
    private FixedSession   session;

    public DefaultMessageConsumer(FixedSession session) {
        this.session = session;
    }

    @Override
    public boolean beginTransaction() throws MQException {
        return transactionVal("begin");
    }

    private boolean transactionVal(String action) throws MQException {
        try {

            WaiterOnFuture onReadFuture = new WaiterOnFuture();

            session.listen(MQTransactionServlet.SERVICE_NAME, onReadFuture);

            session.write(MQTransactionServlet.SERVICE_NAME, action);

            if (onReadFuture.await(3000)) {
                throw MQException.TIME_OUT;
            }

            ProtobaseFuture future = (ProtobaseFuture) onReadFuture.getReadFuture();

            RESMessage message = RESMessageDecoder.decode(future.getReadText());

            if (message.getCode() == 0) {
                return true;
            } else {
                throw new MQException(message.getDescription());
            }

        } catch (IOException e) {
            throw new MQException(e.getMessage(), e);
        }
    }

    @Override
    public boolean commit() throws MQException {
        return transactionVal("commit");
    }

    @Override
    public boolean rollback() throws MQException {
        return transactionVal("rollback");
    }

    @Override
    public void receive(OnMessage onMessage) throws MQException {

        sendReceiveCommandCallback(onMessage);
    }

    @Override
    public void subscribe(OnMessage onMessage) throws MQException {

        sendSubscribeCommandCallback(onMessage);
    }

    private void sendReceiveCommandCallback(OnMessage onMessage) throws MQException {

        if (!needSendReceiveCommand) {
            return;
        }

        checkLoginState();

        try {

            session.listen("MQConsumerServlet", new ConsumerOnFuture(onMessage, messageDecoder));

            session.write("MQConsumerServlet", null);

            needSendReceiveCommand = false;
        } catch (IOException e) {
            throw new MQException(e);
        }
    }

    private void checkLoginState() throws MQException {
        if (session.getAuthority() == null) {
            throw new MQException("not login");
        }
    }

    private void sendSubscribeCommandCallback(OnMessage onMessage) throws MQException {

        if (!needSendSubscribeCommand) {
            return;
        }

        checkLoginState();

        try {

            session.listen("MQSubscribeServlet", new ConsumerOnFuture(onMessage, messageDecoder));

            session.write("MQSubscribeServlet", null);

            needSendSubscribeCommand = false;
        } catch (IOException e) {
            throw new MQException(e);
        }
    }
}
