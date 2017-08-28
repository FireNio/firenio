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

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.DatagramPacketAcceptor;
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.connector.DatagramChannelConnector;
import com.generallycloud.baseio.protocol.DatagramPacket;

public class TestUDPClient {

    public static void main(String[] args) throws Exception {

        DatagramPacketAcceptor acceptor = new DatagramPacketAcceptor() {

            @Override
            public void accept(DatagramSession session, DatagramPacket packet) throws IOException {
                System.out.println(packet.getDataString(Encoding.UTF8));

            }
        };

        DatagramChannelContext context = new DatagramChannelContext(
                new ServerConfiguration("localhost", 18500));

        DatagramChannelConnector connector = new DatagramChannelConnector(context);

        context.setDatagramPacketAcceptor(acceptor);

        DatagramSession session = connector.connect();

        DatagramPacket packet = DatagramPacket.createSendPacket("hello world!".getBytes());

        session.sendPacket(packet);

        ThreadUtil.sleep(30);

        CloseUtil.close(connector);

    }

}
