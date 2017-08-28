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
package com.generallycloud.test.io.fixedlength;

import java.net.StandardSocketOptions;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListener;

/**
 * @author wangkai
 *
 */
public class SetOptionListener implements SocketSessionEventListener {

    /*
     * (non-Javadoc)
     * 
     * @see com.generallycloud.baseio.component.SocketSessionEventListener#
     * sessionOpened(com.generallycloud.baseio.component.SocketSession)
     */
    @Override
    public void sessionOpened(SocketSession session) throws Exception {
        session.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        session.setOption(StandardSocketOptions.TCP_NODELAY, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.generallycloud.baseio.component.SocketSessionEventListener#
     * sessionClosed(com.generallycloud.baseio.component.SocketSession)
     */
    @Override
    public void sessionClosed(SocketSession session) {
        // TODO Auto-generated method stub

    }

}
