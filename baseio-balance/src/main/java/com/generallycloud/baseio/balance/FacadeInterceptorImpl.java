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

import com.generallycloud.baseio.balance.facade.BalanceFacadeSocketSession;

public class FacadeInterceptorImpl implements FacadeInterceptor {

    private int interceptorLimit;

    // 1CPU limit
    private int  globalLimit;

    private int  check = 0;

    private long next_check_time;

    public FacadeInterceptorImpl(int interceptorLimit, int globalLimit) {
        this.globalLimit = globalLimit;
        this.interceptorLimit = interceptorLimit;
    }

    @Override
    public boolean intercept(BalanceFacadeSocketSession session, BalanceFuture future)
            throws Exception {

        long now = System.currentTimeMillis();

        if (now > next_check_time) {
            next_check_time = now + 1000;
            check = 0;
            return session.overfulfil(interceptorLimit);
        }

        return ++check > globalLimit || session.overfulfil(interceptorLimit);
    }

}
