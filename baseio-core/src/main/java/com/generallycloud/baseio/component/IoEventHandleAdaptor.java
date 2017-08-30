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

import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public abstract class IoEventHandleAdaptor implements IoEventHandle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void exceptionCaught(SocketSession session, Future future, Exception ex) {
        logger.error(ex.getMessage(),ex);
    }

    @Override
    public void futureSent(SocketSession session, Future future) {

    }

    protected void initialize(SocketChannelContext context) throws Exception {

    }

    protected void destroy(SocketChannelContext context) throws Exception {

    }

}
