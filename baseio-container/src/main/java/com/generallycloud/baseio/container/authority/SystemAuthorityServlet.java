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
package com.generallycloud.baseio.container.authority;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContextUtil;
import com.generallycloud.baseio.container.LoginCenter;
import com.generallycloud.baseio.container.RESMessage;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ParametersFuture;

public class SystemAuthorityServlet extends FutureAcceptorService {

    public SystemAuthorityServlet(String serviceName) {
        super(serviceName);
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        LoginCenter loginCenter = AuthorityContext.getInstance().getLoginCenter();

        ParametersFuture f = (ParametersFuture) future;

        boolean login = loginCenter.login(session, f.getParameters());

        RESMessage message = RESMessage.UNAUTH;

        if (login) {

            Authority authority = ApplicationContextUtil.getAuthority(session);

            message = new RESMessage(0, authority, null);
        }

        future.write(message.toString());

        session.flush(future);
    }

}
