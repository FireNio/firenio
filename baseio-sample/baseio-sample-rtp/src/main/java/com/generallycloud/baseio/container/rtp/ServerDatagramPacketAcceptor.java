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
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;
import com.generallycloud.baseio.protocol.DatagramRequest;

public abstract class ServerDatagramPacketAcceptor implements DatagramPacketAcceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void accept(DatagramSession session, DatagramPacket packet) throws IOException {

        if (packet.getType() == DatagramPacket.TYPE_ACTION) {

            execute(session, new DatagramRequest(packet.getDataString()));
            return;
        }

        //		logger.debug("___________________server receive,packet:{}",packet);

        SocketSession socketSession = session.getSocketSession();

        if (socketSession == null) {
            logger.debug("___________________null session,packet:{}", packet);
            return;
        }

        doAccept(session, packet, socketSession); //FIXME UDP

    }

    protected abstract void doAccept(DatagramSession session, DatagramPacket packet,
            SocketSession socketSession) throws IOException;

    protected abstract void execute(DatagramSession session, DatagramRequest request);

}
