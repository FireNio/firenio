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

import java.io.IOException;

import com.generallycloud.baseio.acceptor.DatagramChannelAcceptor;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.DatagramPacketAcceptor;
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.DebugUtil;
import com.generallycloud.baseio.protocol.DatagramPacket;

public class TestUDPServer {

    public static void main(String[] args) throws IOException {

        DatagramPacketAcceptor datagramPacketAcceptor = new DatagramPacketAcceptor() {

            @Override
            public void accept(DatagramSession session, DatagramPacket packet) throws IOException {
                String req = packet.getDataString(Encoding.UTF8);

                DebugUtil.debug(req);

                byte[] resMsg = ("yes ," + req).getBytes(Encoding.UTF8);

                DatagramPacket res = DatagramPacket.createSendPacket(resMsg);

                session.sendPacket(res);
            }
        };

        DatagramChannelContext context = new DatagramChannelContext(new ServerConfiguration(18500));

        context.setDatagramPacketAcceptor(datagramPacketAcceptor);

        DatagramChannelAcceptor acceptor = new DatagramChannelAcceptor(context);

        acceptor.bind();
    }

}
