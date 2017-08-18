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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.net.SocketAddress;

import com.generallycloud.baseio.protocol.DatagramPacket;

public interface DatagramChannel extends Channel {

    public abstract void sendPacket(DatagramPacket packet, SocketAddress socketAddress)
            throws IOException;

    public abstract void sendPacket(DatagramPacket packet) throws IOException;

    @Override
    public abstract DatagramSession getSession();

    @Override
    public abstract DatagramChannelContext getContext();

}
