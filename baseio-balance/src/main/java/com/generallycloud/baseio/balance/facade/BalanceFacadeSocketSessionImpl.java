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

import java.util.Random;

import com.generallycloud.baseio.balance.reverse.BalanceReverseSocketSession;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.UnsafeSocketSessionImpl;

public class BalanceFacadeSocketSessionImpl extends UnsafeSocketSessionImpl
        implements BalanceFacadeSocketSession {

    private int                         msg_size;

    private long                        next_check_time;

    private BalanceReverseSocketSession reverseSocketSession;

    public BalanceFacadeSocketSessionImpl(SocketChannel channel) {
        super(channel);
    }

    @Override
    public BalanceReverseSocketSession getReverseSocketSession() {
        return reverseSocketSession;
    }

    @Override
    public boolean overfulfil(int size) {

        long now = System.currentTimeMillis();

        if (now > next_check_time) {
            next_check_time = now + 1000;
            msg_size = 0;
        }

        return ++msg_size > size;
    }

    @Override
    public void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession) {
        this.reverseSocketSession = reverseSocketSession;
    }

    private static Long generateToken1() {
        long r = new Random().nextInt();
        if (r < 0) {
            r *= -1;
        }
        int s = new Random().nextInt(Integer.MAX_VALUE);
        return s | (r << 32);
    }

    @Override
    public Object getSessionKey() {
        return getSessionId();
    }

    public static void main(String[] args) {

        for (int i = 0; i < 20; i++) {
            System.out.println(MathUtil.long2HexString(generateToken1()));
        }

    }

}
