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
package com.generallycloud.baseio.container.rtp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.component.DatagramChannel;
import com.generallycloud.baseio.component.Channel;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.concurrent.ReentrantList;
import com.generallycloud.baseio.container.rtp.RTPContext;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;

//FIXME 是不是要限制最多room数
public class RTPRoom {

    private static final FixedAtomicInteger ROOM_Id             = new FixedAtomicInteger();
    private static final Logger             logger              = LoggerFactory
            .getLogger(RTPRoom.class);

    private RTPContext                      context;
    private ReentrantList<DatagramChannel>  datagramChannelList = new ReentrantList<>(
            new ArrayList<DatagramChannel>());
    private RTPRoomFactory                  roomFactory;
    private int                             roomId;
    private boolean                         closed              = false;

    public RTPRoom(RTPContext context, Channel channel) {
        this.roomId = genRoomId();
        this.roomFactory = context.getRTPRoomFactory();
        this.context = context;
        //		this.join(channel.getDatagramChannel()); //FIXME udp 
    }

    public void broadcast(DatagramChannel channel, DatagramPacket packet) {

        List<DatagramChannel> datagramChannels = datagramChannelList.takeSnapshot();

        for (DatagramChannel ch : datagramChannels) {

            if (channel == ch) {
                continue;
            }

            try {
                ch.sendPacket(packet);
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }

    private int genRoomId() {
        return ROOM_Id.getAndIncrement();
    }

    public int getRoomId() {
        return roomId;
    }

    public boolean join(DatagramChannel channel) {

        if (channel == null) {
            return false;
        }

        ReentrantLock lock = datagramChannelList.getReentrantLock();

        lock.lock();

        if (closed) {

            lock.unlock();

            return false;
        }

        if (!datagramChannelList.add(channel)) {

            lock.unlock();

            return false;
        }

        lock.unlock();

        //		Channel channel = (Channel) channel.getChannel();

        //FIXME RTP
        //		RTPChannelAttachment attachment = (RTPChannelAttachment) channel.getAttachment(context.getPluginIndex());

        //		attachment.setRTPRoom(this);

        return true;
    }

    public void leave(DatagramChannel channel) {

        ReentrantLock lock = datagramChannelList.getReentrantLock();

        lock.lock();

        datagramChannelList.remove(channel);

        List<DatagramChannel> chs = datagramChannelList.takeSnapshot();

        for (DatagramChannel ch : chs) {

            if (ch == channel) {
                continue;
            }

            //FIXME RTP
            //			NioSocketChannel channel = (NioSocketChannel) ch.getChannel();

            //			Authority authority = ApplicationContextUtil.getAuthority(channel);
            //
            //			MapMessage message = new MapMessage("mmm", authority.getUuid());
            //
            //			message.setEventName("break");
            //
            //			message.put("userId", authority.getUserId());
            //
            //			MQContext mqContext = MQContext.getInstance();
            //
            //			mqContext.offerMessage(message);
        }

        if (datagramChannelList.size() == 0) {

            this.closed = true;

            roomFactory.removeRTPRoom(roomId);
        }

        lock.unlock();
    }

}
