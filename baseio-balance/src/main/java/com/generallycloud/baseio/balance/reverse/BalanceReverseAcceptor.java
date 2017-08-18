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
package com.generallycloud.baseio.balance.reverse;

import java.io.IOException;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class BalanceReverseAcceptor {

    private Logger                logger   = LoggerFactory.getLogger(getClass());

    private SocketChannelAcceptor acceptor = null;

    public void start(SocketChannelContext context) throws IOException {

        this.acceptor = new SocketChannelAcceptor(context);

        this.acceptor.bind();

        LoggerUtil.prettyLog(logger, "Balance Reverse Acceptor startup completed ...");
    }

    public SocketChannelAcceptor getAcceptor() {
        return acceptor;
    }

    public void stop() {
        CloseUtil.unbind(acceptor);
    }
}
