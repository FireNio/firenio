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

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.SocketChannelEventListenerAdapter;
import com.generallycloud.baseio.container.rtp.RTPContext;

public class RTPChannelEventListener extends SocketChannelEventListenerAdapter {

    @Override
    public void sessionOpened(NioSocketChannel channel) {

        RTPContext context = RTPContext.getInstance();

        RTPChannelAttachment attachment = context.getChannelAttachment(channel);

        if (attachment == null) {

            attachment = new RTPChannelAttachment(context);

            channel.setAttribute(context.getPluginKey(), attachment);
        }
    }

    @Override
    public void sessionClosed(NioSocketChannel channel) {

        RTPContext context = RTPContext.getInstance();

        RTPChannelAttachment attachment = context.getChannelAttachment(channel);

        if (attachment == null) {
            return;
        }

        RTPRoom room = attachment.getRtpRoom();

        if (room == null) {
            return;
        }

        //		room.leave(channel.getDatagramChannel()); //FIXME udp 
    }

}
