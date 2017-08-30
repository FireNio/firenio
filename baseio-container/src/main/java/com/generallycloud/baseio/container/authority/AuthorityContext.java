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

import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.AbstractPluginContext;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.ContainerConsotant;
import com.generallycloud.baseio.container.InitializeUtil;
import com.generallycloud.baseio.container.LoginCenter;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorService;

public class AuthorityContext extends AbstractPluginContext {

    private static AuthorityContext instance = null;

    private LoginCenter             loginCenter;

    private RoleManager             roleManager;

    public static AuthorityContext getInstance() {
        return instance;
    }

    @Override
    public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {

        String loginAction = ContainerConsotant.ACTION_LOGIN;

        loginAction = getConfig().getParameter("login-action", loginAction);

        ContainerConsotant.ACTION_LOGIN = loginAction;

        putServlet(acceptors, new SystemAuthorityServlet(loginAction));
    }

    @Override
    public void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters) {

        AuthorityFilter authorityFilter = new AuthorityFilter();

        authorityFilter.setSortIndex(0);

        filters.add(authorityFilter);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        super.initialize(context, config);

        context.addSessionEventListener(new AuthoritySEListener());

        //TODO read config
        loginCenter = new AuthorityLoginCenter();
        roleManager = new RoleManager();

        loginCenter.initialize(context, config);
        roleManager.initialize(context, config);

        instance = this;
    }

    public AuthoritySessionAttachment getSessionAttachment(SocketSession session) {
        return (AuthoritySessionAttachment) session.getAttribute(getPluginKey());
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {
        super.destroy(context, config);
        InitializeUtil.destroy(loginCenter, context);
        InitializeUtil.destroy(roleManager, context);
    }

    /**
     * @return the loginCenter
     */
    public LoginCenter getLoginCenter() {
        return loginCenter;
    }

    /**
     * @return the roleManager
     */
    public RoleManager getRoleManager() {
        return roleManager;
    }

}
