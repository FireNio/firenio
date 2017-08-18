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

import com.generallycloud.baseio.component.Session;
import com.generallycloud.baseio.container.rtp.RTPContext;

public class RTPSessionAttachment {

    private RTPContext context;

    private RTPRoom    rtpRoom;

    public RTPRoom getRtpRoom() {
        return rtpRoom;
    }

    public RTPRoom createRTPRoom(Session session) {
        if (rtpRoom == null) {

            rtpRoom = new RTPRoom(context, session);

            RTPRoomFactory factory = context.getRTPRoomFactory();

            factory.putRTPRoom(rtpRoom);
        }
        return rtpRoom;
    }

    protected RTPSessionAttachment(RTPContext context) {
        this.context = context;
    }

    protected void setRTPRoom(RTPRoom room) {
        this.rtpRoom = room;
    }

}
