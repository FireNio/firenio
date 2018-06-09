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
package com.generallycloud.test.io.balance;

import java.io.IOException;

import com.generallycloud.baseio.balance.BalanceServerBootStrap;
import com.generallycloud.baseio.balance.FacadeAcceptor;
import com.generallycloud.baseio.balance.FacadeInterceptorImpl;
import com.generallycloud.baseio.balance.router.HashedBalanceRouter;
import com.generallycloud.baseio.codec.protobase.ProtobaseCodec;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerExceptionCaughtHandle;
import com.generallycloud.baseio.configuration.Configuration;

public class TestBalanceMain {

    public static void main(String[] args) throws IOException {

        BalanceServerBootStrap f = new BalanceServerBootStrap();

        Configuration protoCfg = new Configuration();
        Configuration fixedCfg = new Configuration();
        Configuration rc = new Configuration();
        protoCfg.setPort(8600);
        fixedCfg.setPort(8700);
        rc.setPort(8800);

        ChannelContext protoCtx = new ChannelContext(protoCfg);
        ChannelContext fixedCtx = new ChannelContext(fixedCfg);
        ChannelContext rcCtx = new ChannelContext(rc);
        ChannelAcceptor protoCa = new ChannelAcceptor(protoCtx);
        ChannelAcceptor fixedCa = new ChannelAcceptor(fixedCtx);

        protoCtx.setProtocolCodec(new ProtobaseCodec());
        rcCtx.setProtocolCodec(new ProtobaseCodec());

        f.addFacadeAcceptor(new FacadeAcceptor(protoCa));
        f.addFacadeAcceptor(new FacadeAcceptor(fixedCa));
        f.setReverseChannelContext(rcCtx);
        f.setFacadeExceptionCaughtHandle(new LoggerExceptionCaughtHandle());
        f.setReverseExceptionCaughtHandle(new LoggerExceptionCaughtHandle());
        f.setFacadeInterceptor(new FacadeInterceptorImpl(500, 50000));
        f.setBalanceRouter(new HashedBalanceRouter(10240));
        //		f.setBalanceRouter(new SimpleNextRouter());

        f.startup();
    }
}
