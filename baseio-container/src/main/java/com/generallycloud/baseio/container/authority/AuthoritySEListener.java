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
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.baseio.container.ApplicationContextUtil;

public class AuthoritySEListener extends SocketSessionEventListenerAdapter {

    @Override
    public void sessionOpened(SocketSession session) {

        AuthorityContext context = AuthorityContext.getInstance();

        AuthoritySessionAttachment attachment = context.getSessionAttachment(session);

        if (attachment == null) {

            attachment = new AuthoritySessionAttachment();

            session.setAttribute(context.getPluginKey(), attachment);
        }

    }

    @Override
    public void sessionClosed(SocketSession session) {

        Authority authority = ApplicationContextUtil.getAuthority(session);

        if (authority == null) {
            return;
        }

        //		AuthorityContext context = AuthorityContext.getInstance();
        //		
        //		session.setAttachment(context.getPluginIndex(), null);
    }

}
