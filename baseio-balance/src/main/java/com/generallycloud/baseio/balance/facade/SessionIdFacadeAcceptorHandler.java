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
package com.generallycloud.baseio.balance.facade;

import com.generallycloud.baseio.balance.BalanceFuture;
import com.generallycloud.baseio.balance.SessionIdBalanceFuture;
import com.generallycloud.baseio.balance.reverse.ReverseSocketSession;

public class SessionIdFacadeAcceptorHandler extends FacadeAcceptorHandler {

    @Override
    protected void doAccept(FacadeSocketSession fs, ReverseSocketSession rs,
            BalanceFuture future) {
        SessionIdBalanceFuture f = (SessionIdBalanceFuture) future;
        f.setSessionId(fs.getSessionId());
        rs.flush(f.translate(rs));
        logDispatchMsg(fs, rs, f);
    }

}
