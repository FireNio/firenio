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

import com.generallycloud.baseio.component.DatagramPacketAcceptor;
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.DatagramPacketGroup;

public class RTPClientDPAcceptor implements DatagramPacketAcceptor {

    private int                 markInterval;
    private long                lastMark;
    private long                currentMark;
    private DatagramPacketGroup packetGroup;
    private RTPHandle           udpReceiveHandle;
    private RTPClient           rtpClient;
    private int                 groupSize;
    private Logger              logger = LoggerFactory.getLogger(RTPClientDPAcceptor.class);

    public RTPClientDPAcceptor(int markInterval, long currentMark, int groupSize,
            RTPHandle udpReceiveHandle, RTPClient rtpClient) {
        this.markInterval = markInterval;
        this.udpReceiveHandle = udpReceiveHandle;
        this.rtpClient = rtpClient;
        this.markInterval = markInterval;
        this.currentMark = currentMark;
        this.lastMark = currentMark - markInterval;
        this.groupSize = groupSize;
        this.packetGroup = new DatagramPacketGroup(groupSize);

        logger.debug("________________lastMark______create:{}", lastMark);

    }

    @Override
    public void accept(DatagramSession session, DatagramPacket packet) throws IOException {

        long timestamp = packet.getTimestamp();

        // logger.debug("timestamp:{},lastMark:{}", timestamp, lastMark);

        if (timestamp < lastMark) {
            logger.info("______________________ignore packet:{},___lastMark:{},", packet, lastMark);
            return;
        }

        if (timestamp < currentMark) {
            packetGroup.addDatagramPacket(packet);
            return;
        }

        if (timestamp >= currentMark) {

            lastMark = currentMark;

            currentMark = lastMark + markInterval;

            final DatagramPacketGroup _packetGroup = this.packetGroup;

            // logger.debug("__________________packetGroup size :{}",_packetGroup.size());

            this.packetGroup = new DatagramPacketGroup(groupSize);

            this.packetGroup.addDatagramPacket(packet);

            //socketSession.getEventLoop()
            //			session.getEventLoop().dispatch(new Runnable() {
            //
            //				public void run() {
            //					try {
            //						udpReceiveHandle.onReceiveUDPPacket(rtpClient, _packetGroup);
            //					} catch (Throwable e) {
            //						logger.debug(e);
            //					}
            //				}
            //			});
        }
    }

}
