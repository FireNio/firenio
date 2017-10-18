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
package com.generallycloud.test.io.udp;

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.container.jms.MapMessage;
import com.generallycloud.baseio.container.rtp.RTPException;
import com.generallycloud.baseio.container.rtp.client.RTPClient;
import com.generallycloud.baseio.container.rtp.client.RTPClientDPAcceptor;
import com.generallycloud.baseio.container.rtp.client.RTPHandle;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.DatagramPacketFactory;
import com.generallycloud.baseio.protocol.DatagramPacketGroup;
import com.generallycloud.baseio.protocol.DatagramPacketGroup.DPForeach;

public class TestUDPReceiveHandle extends RTPHandle {

    private Logger logger = LoggerFactory.getLogger(TestUDPReceiveHandle.class);
    private int    sleep  = 1;

    @Override
    public void onReceiveUDPPacket(RTPClient client, DatagramPacketGroup group) {

        //		logger.debug("_______________foreach___data_size:{}", group.size());
        group.foreach(new DPForeach() {
            @Override
            public void onPacket(DatagramPacket packet) {
                String data = new String(packet.getData(), Encoding.GBK);
                logger.debug("_______________foreach___data:{},seq:{}", data,
                        packet.getSequenceNo());
            }
        });
    }

    @Override
    public void onInvite(RTPClient client, MapMessage message) {

        int markInterval = 5;

        String roomId = message.getParameter("roomId");

        String inviteUsername = message.getParameter("inviteUsername");

        DatagramPacketFactory factory = new DatagramPacketFactory(markInterval);

        long currentMark = factory.getCalculagraph().getAlphaTimestamp();

        int groupSize = 102400;

        try {
            client.joinRoom(roomId);

            client.inviteReply(inviteUsername, markInterval, currentMark, groupSize);
        } catch (RTPException e) {
            DebugUtil.debug(e);
        }

        RTPClientDPAcceptor acceptor = new RTPClientDPAcceptor(markInterval, currentMark, groupSize,
                this, client);

        client.setRTPClientDPAcceptor(acceptor);

        client.setRoomId(roomId);

        for (int i = 0; i < 10000000; i++) {

            byte[] data = (inviteUsername + i).getBytes();

            DatagramPacket packet = factory.createDatagramPacket(data);

            try {
                client.sendDatagramPacket(packet);
            } catch (RTPException e) {
                DebugUtil.debug(e);
            }

            //			logger.debug("________________________send_packet:{}",packet);

            ThreadUtil.sleep(sleep);
        }
    }

    @Override
    public void onInviteReplyed(RTPClient client, MapMessage message) {

        int markInterval = message.getIntegerParameter(RTPClient.MARK_INTERVAL);

        long currentMark = message.getLongParameter(RTPClient.CURRENT_MARK);

        int groupSize = message.getIntegerParameter(RTPClient.GROUP_SIZE);

        logger.debug("___________onInviteReplyed:{},{},{}",
                new Object[] { markInterval, currentMark, groupSize });

        DatagramPacketFactory factory = new DatagramPacketFactory(markInterval, currentMark);

        RTPClientDPAcceptor acceptor = new RTPClientDPAcceptor(markInterval, currentMark, groupSize,
                this, client);

        client.setRTPClientDPAcceptor(acceptor);

        for (int i = 0; i < 10000000; i++) {

            byte[] data = (client.getInviteUsername() + i).getBytes();

            DatagramPacket packet = factory.createDatagramPacket(data);

            try {
                client.sendDatagramPacket(packet);
            } catch (RTPException e) {
                DebugUtil.debug(e);
            }

            //			logger.debug("________________________send_packet:{}",packet);

            ThreadUtil.sleep(sleep);
        }
    }

    @Override
    public void onBreak(RTPClient client, MapMessage message) {

        logger.debug("_________________________leave,{}", message.toString());
    }

}
