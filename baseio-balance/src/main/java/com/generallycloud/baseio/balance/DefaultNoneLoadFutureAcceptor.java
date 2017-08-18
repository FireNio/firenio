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
package com.generallycloud.baseio.balance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generallycloud.baseio.balance.facade.BalanceFacadeSocketSession;
import com.generallycloud.baseio.balance.reverse.BalanceReverseLogger;

/**
 * @author wangkai
 *
 */
public class DefaultNoneLoadFutureAcceptor implements NoneLoadFutureAcceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void accept(BalanceFacadeSocketSession session, BalanceFuture future,
            BalanceReverseLogger reverseLogger) {
        logger.info("none load node found: [ {} ], msg: {}", session.getRemoteSocketAddress(),
                future);
    }

}
