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
package com.generallycloud.baseio.container.rtp.client;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.connector.DatagramChannelConnector;
import com.generallycloud.baseio.container.FixedSession;
import com.generallycloud.baseio.container.OnFuture;
import com.generallycloud.baseio.container.authority.Authority;
import com.generallycloud.baseio.container.jms.JmsUtil;
import com.generallycloud.baseio.container.jms.MQException;
import com.generallycloud.baseio.container.jms.MapMessage;
import com.generallycloud.baseio.container.jms.client.MessageProducer;
import com.generallycloud.baseio.container.jms.client.impl.DefaultMessageProducer;
import com.generallycloud.baseio.container.jms.client.impl.FixedMessageConsumer;
import com.generallycloud.baseio.container.jms.client.impl.OnMappedMessage;
import com.generallycloud.baseio.container.rtp.RTPException;
import com.generallycloud.baseio.container.rtp.RTPServerDPAcceptor;
import com.generallycloud.baseio.container.rtp.server.RTPCreateRoomServlet;
import com.generallycloud.baseio.container.rtp.server.RTPJoinRoomServlet;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.Future;

public class RTPClient {

    public static final String       CURRENT_MARK  = "CURRENT_MARK";
    public static final String       GROUP_SIZE    = "GROUP_SIZE";
    public static final String       MARK_INTERVAL = "MARK_INTERVAL";

    private DatagramChannelConnector connector;
    private FixedMessageConsumer     consumer;
    private DatagramChannelContext   context;
    private String                   inviteUsername;
    private MessageProducer          producer;
    private String                   roomId;
    private FixedSession             session;
    private RTPHandle                handle;

    public RTPClient(FixedSession session, DatagramChannelConnector connector) {
        this(session, connector, new FixedMessageConsumer(session),
                new DefaultMessageProducer(session));
    }

    // FIXME listen onf break
    public RTPClient(FixedSession session, DatagramChannelConnector connector,
            FixedMessageConsumer consumer, MessageProducer producer) {
        this.connector = connector;
        this.session = session;
        this.producer = producer;
        this.consumer = consumer;
        this.context = connector.getContext();
    }

    public void setRTPHandle(final RTPHandle handle) throws RTPException {

        if (this.handle != null) {
            return;
        }

        this.consumer.listen("invite", new OnMappedMessage() {

            @Override
            public void onReceive(MapMessage message) {
                handle.onInvite(RTPClient.this, message);
            }
        });

        this.consumer.listen("invite-reply", new OnMappedMessage() {

            @Override
            public void onReceive(MapMessage message) {
                handle.onInviteReplyed(RTPClient.this, message);
            }
        });

        this.consumer.listen("break", new OnMappedMessage() {

            @Override
            public void onReceive(MapMessage message) {
                handle.onBreak(RTPClient.this, message);
            }
        });

        this.handle = handle;

        try {

            this.consumer.receive(null);
        } catch (MQException e) {
            throw new RTPException(e);
        }
    }

    public RTPHandle getRTPHandle() {
        return handle;
    }

    public boolean createRoom(String inviteUsername) throws RTPException {

        ProtobaseFuture future;

        try {
            future = session.request(RTPCreateRoomServlet.SERVICE_NAME, null);
        } catch (IOException e) {
            throw new RTPException(e.getMessage(), e);
        }

        String roomId = future.getReadText();

        if ("-1".equals(roomId)) {
            throw new RTPException("create room failed");
        }

        this.roomId = roomId;

        this.inviteCustomer(inviteUsername);

        return true;
    }

    public DatagramChannelContext getContext() {
        return context;
    }

    public String getInviteUsername() {
        return inviteUsername;
    }

    public void inviteCustomer(String inviteUsername) throws RTPException {

        if (roomId == null) {
            throw new RTPException("none roomId,create room first");
        }

        Authority authority = session.getAuthority();

        if (authority == null) {
            throw new RTPException("not login");
        }

        MapMessage message = new MapMessage("msgId", inviteUsername);

        message.put("eventName", "invite");
        message.put("roomId", roomId);
        message.put("inviteUsername", authority.getUsername());

        try {
            producer.offer(message);

        } catch (MQException e) {
            throw new RTPException(e);
        }

        this.inviteUsername = inviteUsername;
    }

    public void inviteReply(String inviteUsername, int markinterval, long currentMark,
            int groupSize) throws RTPException {

        MapMessage message = new MapMessage("msgId", inviteUsername);

        message.put("eventName", "invite-reply");
        message.put(MARK_INTERVAL, markinterval);
        message.put(CURRENT_MARK, currentMark);
        message.put(GROUP_SIZE, groupSize);

        try {
            producer.offer(message);
        } catch (MQException e) {
            throw new RTPException(e);
        }

        this.inviteUsername = inviteUsername;
    }

    public boolean joinRoom(String roomId) throws RTPException {
        try {
            ProtobaseFuture future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomId);
            return JmsUtil.isTrue(future);
        } catch (IOException e) {
            throw new RTPException(e.getMessage(), e);
        }
    }

    public boolean leaveRoom() throws RTPException {
        try {

            Authority authority = session.getAuthority();

            if (authority == null) {
                throw new RTPException("not login");
            }

            ProtobaseFuture future = session.request(RTPJoinRoomServlet.SERVICE_NAME, roomId);

            this.handle.onBreak(this, new MapMessage("", authority.getUuid()));

            return JmsUtil.isTrue(future);
        } catch (IOException e) {
            throw new RTPException(e.getMessage(), e);
        }
    }

    public void sendDatagramPacket(DatagramPacket packet) throws RTPException {

        if (roomId == null) {
            throw new RTPException("none roomId,create room first");
        }

        try {
            connector.sendDatagramPacket(packet);
        } catch (IOException e) {
            throw new RTPException(e);
        }
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRTPClientDPAcceptor(RTPClientDPAcceptor acceptor) {
        context.setDatagramPacketAcceptor(acceptor);
    }

    public void bindTCPSession() throws IOException {

        if (connector == null) {
            throw new IllegalArgumentException("null udp connector");
        }

        Authority authority = session.getAuthority();

        if (authority == null) {
            throw new IllegalArgumentException("not login");
        }

        JSONObject json = new JSONObject();

        json.put("serviceName", RTPServerDPAcceptor.BIND_SESSION);

        json.put("username", authority.getUsername());
        json.put("password", authority.getPassword());

        final DatagramPacket packet = DatagramPacket
                .createSendPacket(json.toJSONString().getBytes(context.getEncoding()));

        final String BIND_SESSION_CALLBACK = RTPServerDPAcceptor.BIND_SESSION_CALLBACK;

        final Waiter<Integer> waiter = new Waiter<>();

        session.listen(BIND_SESSION_CALLBACK, new OnFuture() {

            @Override
            public void onResponse(SocketSession session, Future future) {

                waiter.setPayload(0);
            }
        });

        final byte[] shortWaiter = new byte[] {};

        ThreadUtil.execute(new Runnable() {

            @Override
            public void run() {

                for (int i = 0; i < 10; i++) {

                    try {
                        connector.sendDatagramPacket(packet);
                    } catch (IOException e) {
                        DebugUtil.debug(e);
                    }

                    if (waiter.isDnoe()) {
                        break;
                    }

                    synchronized (shortWaiter) {
                        try {
                            shortWaiter.wait(300);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });

        if (waiter.await(3000)) {

            CloseUtil.close(connector);

            throw new ClosedChannelException("disconnected");
        }
    }

}
