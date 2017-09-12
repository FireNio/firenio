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
package com.generallycloud.baseio.container;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.DynamicClassLoader;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.LoggerExceptionCaughtHandle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.component.URLDynamicClassLoader;
import com.generallycloud.baseio.container.configuration.ApplicationConfiguration;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.container.implementation.SystemRedeployServlet;
import com.generallycloud.baseio.container.implementation.SystemStopServerServlet;
import com.generallycloud.baseio.container.service.FutureAcceptorFilterLoader;
import com.generallycloud.baseio.container.service.FutureAcceptorFilterWrapper;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceLoader;
import com.generallycloud.baseio.container.service.PluginLoader;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ApplicationContext extends AbstractLifeCycle {

    private static ApplicationContext instance;

    public static ApplicationContext getInstance() {
        return instance;
    }

    private ApplicationConfigurationLoader acLoader;
    private ApplicationExtLoader           applicationExtLoader;
    private String                         appLocalAddres;
    private FutureAcceptorService          appRedeployService;
    private Set<String>                    blackIPs;
    private SocketChannelContext           channelContext;
    private URLDynamicClassLoader          classLoader;
    private ApplicationConfiguration       configuration;
    private boolean                        deployModel;
    private Charset                        encoding;
    private ExceptionCaughtHandle          exceptionCaughtHandle;
    private FutureAcceptorFilterLoader     filterLoader;
    private FutureAcceptorServiceFilter    futureAcceptorServiceFilter;
    private ExceptionCaughtHandle          ioExceptionCaughtHandle;
    private Logger                         logger       = LoggerFactory.getLogger(getClass());
    private PluginLoader                   pluginLoader;
    private boolean                        redeploying;
    private String                         rootLocalAddress;
    private AtomicInteger                  redeployTime = new AtomicInteger();

    public ApplicationContext(ApplicationConfiguration configuration) {
        this(FileUtil.getCurrentPath(), configuration);
    }

    public ApplicationContext(String rootLocalAddress) {
        this(rootLocalAddress, null);
    }

    public ApplicationContext(String rootLocalAddress, ApplicationConfiguration configuration) {
        this.rootLocalAddress = rootLocalAddress;
        this.configuration = configuration;
    }

    public void addSessionEventListener(SocketSessionEventListener listener) {
        channelContext.addSessionEventListener(listener);
    }

    private void destroyApplicationContext() {
        classLoader.unloadClassLoader();
        InitializeUtil.destroy(filterLoader, this);
        InitializeUtil.destroy(pluginLoader, this);
    }

    @Override
    protected void doStart() throws Exception {

        if (channelContext == null) {
            throw new IllegalArgumentException("null nio context");
        }

        if (StringUtil.isNullOrBlank(rootLocalAddress)) {
            throw new IllegalArgumentException("rootLocalAddress");
        }

        if (futureAcceptorServiceFilter == null) {
            this.futureAcceptorServiceFilter = new FutureAcceptorServiceFilter();
        }

        if (appRedeployService == null) {
            appRedeployService = new SystemRedeployServlet();
        }

        if (acLoader == null) {
            acLoader = new FileSystemACLoader();
        }

        if (exceptionCaughtHandle == null) {
            exceptionCaughtHandle = new LoggerExceptionCaughtHandle();
        }

        if (ioExceptionCaughtHandle == null) {
            ioExceptionCaughtHandle = new LoggerExceptionCaughtHandle();
        }

        instance = this;

        this.rootLocalAddress = FileUtil.getPrettyPath(rootLocalAddress);

        this.encoding = channelContext.getEncoding();

        this.appLocalAddres = FileUtil.getPrettyPath(getRootLocalAddress() + "app");

        LoggerUtil.prettyLog(logger, "application path      :{ {} }", appLocalAddres);

        this.initializeApplicationContext();
    }

    @Override
    protected void doStop() throws Exception {
        destroyApplicationContext();
        instance = null;
    }

    /**
     * @return the acLoader
     */
    public ApplicationConfigurationLoader getAcLoader() {
        return acLoader;
    }

    public ApplicationExtLoader getApplicationExtLoader() {
        return applicationExtLoader;
    }

    public String getAppLocalAddress() {
        return appLocalAddres;
    }

    public FutureAcceptorService getAppRedeployService() {
        return appRedeployService;
    }

    public Set<String> getBlackIPs() {
        return blackIPs;
    }

    public SocketChannelContext getChannelContext() {
        return channelContext;
    }

    public DynamicClassLoader getClassLoader() {
        return classLoader;
    }

    public ApplicationConfiguration getConfiguration() {
        return configuration;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public ExceptionCaughtHandle getExceptionCaughtHandle() {
        return exceptionCaughtHandle;
    }

    public FutureAcceptorFilterLoader getFilterLoader() {
        return filterLoader;
    }

    public FutureAcceptorServiceFilter getFutureAcceptorServiceFilter() {
        return futureAcceptorServiceFilter;
    }

    public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
        return filterLoader.getFutureAcceptorServiceLoader();
    }

    public PluginContext[] getPluginContexts() {
        return pluginLoader.getPluginContexts();
    }

    public FutureAcceptorFilterWrapper getRootFutureAcceptorFilter() {
        return filterLoader.getRootFilter();
    }

    public String getRootLocalAddress() {
        return rootLocalAddress;
    }

    private void initializeApplicationContext() throws Exception {

        this.classLoader = newClassLoader();

        this.configuration = acLoader.loadConfiguration(classLoader);

        this.applicationExtLoader.loadExts(this, classLoader);

        if (pluginLoader == null) {
            this.pluginLoader = new PluginLoader();
        }

        if (filterLoader == null) {
            this.filterLoader = new FutureAcceptorFilterLoader(getFutureAcceptorServiceFilter());
        }

        if (configuration.isAPP_ENABLE_REDEPLOY()) {
            SystemRedeployServlet redeployServlet = new SystemRedeployServlet();
            filterLoader.getFutureAcceptorServiceLoader().getServices()
                    .put(redeployServlet.getServiceName(), redeployServlet);
        }

        if (configuration.isAPP_ENABLE_STOPSERVER()) {
            SystemStopServerServlet stopServerServlet = new SystemStopServerServlet();
            filterLoader.getFutureAcceptorServiceLoader().getServices()
                    .put(stopServerServlet.getServiceName(), stopServerServlet);
        }

        pluginLoader.initialize(this, null);

        filterLoader.initialize(this, null);
    }

    public boolean isDeployModel() {
        return deployModel;
    }

    /**
     * @return the redeploying
     */
    public boolean isRedeploying() {
        return redeploying;
    }

    private URLDynamicClassLoader newClassLoader() throws IOException {

        ClassLoader parent = getClass().getClassLoader();

        URLDynamicClassLoader classLoader = new URLDynamicClassLoader(parent);

        if (deployModel) {

            classLoader.scan(new File(getRootLocalAddress() + "/lib"));

            classLoader.scan(new File(getRootLocalAddress() + "/conf"));
        } else {

            classLoader.addExcludePath("/app");

            classLoader.scan(new File(getRootLocalAddress()));
        }

        return classLoader;
    }

    // FIXME 考虑部署失败后如何再次部署
    // FIXME redeploy roleManager
    // FIXME redeploy loginCenter
    // FIXME keep http session
    public synchronized boolean redeploy() {

        LoggerUtil.prettyLog(logger, "//**********************  开始卸载服务  **********************//");

        redeploying = true;

        destroyApplicationContext();

        LoggerUtil.prettyLog(logger,
                "//**********************  卸载服务完成  **********************//\n");

        try {

            // FIXME 重新加载configuration
            LoggerUtil.prettyLog(logger,
                    "//**********************  开始加载服务  **********************//");

            initializeApplicationContext();

            redeploying = false;

            LoggerUtil.prettyLog(logger,
                    "//**********************  加载服务完成  **********************//\n");

            return true;

        } catch (Exception e) {

            classLoader.unloadClassLoader();

            redeploying = false;

            LoggerUtil.prettyLog(logger,
                    "//**********************  加载服务失败  **********************//\n");

            logger.info(e.getMessage(), e);

            return false;
        }

    }

    public void setApplicationConfigurationLoader(ApplicationConfigurationLoader acLoader) {
        this.acLoader = acLoader;
    }

    public void setApplicationExtLoader(ApplicationExtLoader applicationExtLoader) {
        this.applicationExtLoader = applicationExtLoader;
    }

    public void setAppRedeployService(FutureAcceptorService appRedeployService) {
        this.appRedeployService = appRedeployService;
    }

    public void setBlackIPs(Set<String> blackIPs) {
        this.blackIPs = blackIPs;
    }

    public void setChannelContext(SocketChannelContext context) {
        this.channelContext = context;
    }

    public void setDeployModel(boolean deployModel) {
        this.deployModel = deployModel;
    }

    /**
     * accept抛出异常时则使用该
     * @param exceptionCaughtHandle
     */
    public void setExceptionCaughtHandle(ExceptionCaughtHandle exceptionCaughtHandle) {
        this.exceptionCaughtHandle = exceptionCaughtHandle;
    }

    /**
     * @param ioExceptionCaughtHandle the ioExceptionCaughtHandle to set
     */
    public void setIoExceptionCaughtHandle(ExceptionCaughtHandle ioExceptionCaughtHandle) {
        this.ioExceptionCaughtHandle = ioExceptionCaughtHandle;
    }

    public void setServiceFilter(FutureAcceptorServiceFilter serviceFilter) {
        this.futureAcceptorServiceFilter = serviceFilter;
    }

    /**
     * @return the ioExceptionCaughtHandle
     */
    public ExceptionCaughtHandle getIoExceptionCaughtHandle() {
        return ioExceptionCaughtHandle;
    }

    /**
     * @return the redeployTime
     */
    public AtomicInteger getRedeployTime() {
        return redeployTime;
    }

}
