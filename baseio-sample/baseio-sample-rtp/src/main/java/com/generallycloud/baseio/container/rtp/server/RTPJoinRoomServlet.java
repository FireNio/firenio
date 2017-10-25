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

import com.generallycloud.baseio.codec.protobase.future.ProtobaseFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.rtp.RTPContext;

public class RTPJoinRoomServlet extends RTPServlet {

    public static final String SERVICE_NAME = RTPJoinRoomServlet.class.getSimpleName();

    @Override
    public void doAccept(SocketSession session, ProtobaseFuture future,
            RTPSessionAttachment attachment) throws Exception {

        RTPContext context = getRTPContext();

        int roomId = Integer.parseInt(future.getReadText());

        RTPRoomFactory roomFactory = context.getRTPRoomFactory();

        RTPRoom room = roomFactory.getRTPRoom(roomId);

        if (room == null) {

            future.write("0");

            session.flush(future);

            return;
        }

        if (room.join(null)) {

            future.write("1");

        } else {

            future.write("0");
        }

        session.flush(future);
    }

}
