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
package com.generallycloud.baseio.container.rtp;

import java.io.IOException;

import com.generallycloud.baseio.component.DatagramPacketAcceptor;
import com.generallycloud.baseio.component.DatagramChannel;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.DatagramRequest;

public abstract class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void accept(DatagramChannel channel, DatagramPacket packet) throws IOException {

        if (packet.getType() == DatagramPacket.TYPE_ACTION) {

            execute(channel, new DatagramRequest(packet.getDataString()));
            return;
        }

        //		logger.debug("___________________server receive,packet:{}",packet);

        NioSocketChannel socketChannel = channel.getSocketChannel();

        if (socketChannel == null) {
            logger.debug("___________________null channel,packet:{}", packet);
            return;
        }

        doAccept(channel, packet, socketChannel); //FIXME UDP

    }

    protected abstract void doAccept(DatagramChannel channel, DatagramPacket packet,
            NioSocketChannel socketChannel) throws IOException;

    protected abstract void execute(DatagramChannel channel, DatagramRequest request);

}
