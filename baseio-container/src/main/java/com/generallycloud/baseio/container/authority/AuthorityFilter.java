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

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.RESMessage;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.NamedFuture;
import com.generallycloud.baseio.protocol.ParametersFuture;

public class AuthorityFilter extends FutureAcceptorFilter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void accept(SocketSession session, NamedFuture future) throws Exception {

        AuthorityContext authorityContext = AuthorityContext.getInstance();

        AuthoritySessionAttachment attachment = authorityContext.getSessionAttachment(session);

        AuthorityManager authorityManager = attachment.getAuthorityManager();

        if (authorityManager == null) {

            RoleManager roleManager = authorityContext.getRoleManager();

            authorityManager = roleManager.getAuthorityManager(Authority.GUEST);

            attachment.setAuthorityManager(authorityManager);
        }

        if (!authorityManager.isInvokeApproved(future.getFutureName())) {

            future.write(RESMessage.UNAUTH.toString());

            session.flush(future);

            String futureName = future.getFutureName();

            String remoteAddr = session.getRemoteAddr();

            String readText = future.getReadText();

            if (!StringUtil.isNullOrBlank(readText)) {
                logger.info("refuse connection, ip:{}, service name:{}, content: {}",
                        new String[] { remoteAddr, futureName, readText });
                return;
            }

            if (future instanceof ParametersFuture) {

                Parameters parameters = ((ParametersFuture) future).getParameters();

                if (parameters.size() > 0) {
                    logger.info("refuse connection, ip:{}, service name:{}, content: {}",
                            new String[] { remoteAddr, futureName, parameters.toString() });
                    return;
                }
            }

            logger.info("refuse connection, ip:{}, service name:{}", remoteAddr, futureName);
        }
    }

}
