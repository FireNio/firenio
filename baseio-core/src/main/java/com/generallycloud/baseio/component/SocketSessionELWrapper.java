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

import com.generallycloud.baseio.concurrent.Linkable;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class SocketSessionELWrapper implements SocketSessionEventListener {

    private SocketSessionELWrapper next;

    private Logger                            logger;

    private SocketSessionEventListener        value;

    public SocketSessionELWrapper(SocketSessionEventListener value) {
        this.value = value;
        this.logger = LoggerFactory.getLogger(value.getClass());
    }

    @Override
    public void sessionOpened(SocketSession session) {
        try {
            value.sessionOpened(session);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        SocketSessionELWrapper listener = getNext();
        if (listener == null) {
            return;
        }
        listener.sessionOpened(session);
    }

    @Override
    public void sessionClosed(SocketSession session) {
        try {
            value.sessionClosed(session);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        SocketSessionELWrapper listener = getNext();
        if (listener == null) {
            return;
        }
        listener.sessionClosed(session);
    }

    public SocketSessionELWrapper getNext() {
        return next;
    }

    public void setNext(Linkable next) {
        this.next = (SocketSessionELWrapper) next;
    }

}
