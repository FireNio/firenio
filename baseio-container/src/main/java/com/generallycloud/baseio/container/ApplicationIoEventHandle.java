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

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.Bootstrap;
import com.generallycloud.baseio.component.DynamicClassLoader;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.FutureAcceptor;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerExceptionCaughtHandle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.component.URLDynamicClassLoader;
import com.generallycloud.baseio.container.configuration.ApplicationConfiguration;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.container.implementation.SystemRedeployServlet;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class ApplicationIoEventHandle extends IoEventHandleAdaptor {

    private ApplicationExtLoader           applicationExtLoader;
    private String                         appLocalAddres;
    private FutureAcceptor                 appRedeployService;
    private SocketChannelContext           channelContext;
    private URLDynamicClassLoader          classLoader;
    private ApplicationConfiguration       configuration;
    private ApplicationConfigurationLoader configurationLoader;
    private volatile boolean               deploying    = true;
    private boolean                        deployModel;
    private Charset                        encoding;
    private ExceptionCaughtHandle          exceptionCaughtHandle;
    private ContainerIoEventHandle         futureAcceptor;
    private ExceptionCaughtHandle          ioExceptionCaughtHandle;
    private Logger                         logger       = LoggerFactory.getLogger(getClass());
    private boolean                        redeploying;
    private AtomicInteger                  redeployTime = new AtomicInteger();
    private String                         rootLocalAddress;

    // exceptionCaughtHandle ; ioExceptionCaughtHandle
    // 区分 socket || application exception handle
    public ApplicationIoEventHandle(String rootPath, boolean deployModel) {
        this.rootLocalAddress = rootPath;
        this.deployModel = deployModel;
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {
        if (deploying) {
            appRedeployService.accept(session, future);
            return;
        }
        try {
            futureAcceptor.accept(session, future);
        } catch (Exception e) {
            exceptionCaughtHandle.exceptionCaught(session, future, e);
        }
    }

    public void addSessionEventListener(SocketSessionEventListener listener) {
        channelContext.addSessionEventListener(listener);
    }

    @Override
    protected void destroy(SocketChannelContext context) throws Exception {
        this.deploying = true;
        this.destroyHandle(context);
        super.destroy(context);
    }

    private void destroyHandle(SocketChannelContext context) throws Exception {
        classLoader.unloadClassLoader();
        getFutureAcceptor().destroy(context);
    }

    @Override
    public void exceptionCaught(SocketSession session, Future future, Exception ex) {
        ioExceptionCaughtHandle.exceptionCaught(session, future, ex);
    }

    public ApplicationExtLoader getApplicationExtLoader() {
        return applicationExtLoader;
    }

    public String getAppLocalAddress() {
        return appLocalAddres;
    }

    public FutureAcceptor getAppRedeployService() {
        return appRedeployService;
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

    public ApplicationConfigurationLoader getConfigurationLoader() {
        return configurationLoader;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public ExceptionCaughtHandle getExceptionCaughtHandle() {
        return exceptionCaughtHandle;
    }

    public ContainerIoEventHandle getFutureAcceptor() {
        return futureAcceptor;
    }

    public ExceptionCaughtHandle getIoExceptionCaughtHandle() {
        return ioExceptionCaughtHandle;
    }

    public AtomicInteger getRedeployTime() {
        return redeployTime;
    }

    public String getRootLocalAddress() {
        return rootLocalAddress;
    }

    @Override
    protected void initialize(SocketChannelContext context) throws Exception {
        this.channelContext = context;
        if (StringUtil.isNullOrBlank(rootLocalAddress)) {
            throw new IllegalArgumentException("rootLocalAddress");
        }
        if (applicationExtLoader == null) {
            applicationExtLoader = new DefaultExtLoader();
        }
        if (configurationLoader == null) {
            configurationLoader = new FileSystemACLoader();
        }
        this.rootLocalAddress = FileUtil.getPrettyPath(rootLocalAddress);
        this.encoding = channelContext.getEncoding();
        this.appLocalAddres = FileUtil.getPrettyPath(getRootLocalAddress() + "app");
        LoggerUtil.prettyLog(logger, "application path      :{ {} }", appLocalAddres);
        this.initializeHandle(context);
        this.deploying = false;
        super.initialize(context);
    }

    private void initializeHandle(SocketChannelContext context) throws Exception {
        this.classLoader = Bootstrap.newClassLoader(getClass().getClassLoader(), deployModel,
                true, rootLocalAddress, Bootstrap.withDefault());
        this.applicationExtLoader.loadExts(this, classLoader);
        this.configuration = configurationLoader.loadConfiguration(classLoader);
        //FIXME ....get from configuration 
        if (appRedeployService == null) {
            appRedeployService = new SystemRedeployServlet();
        }
        if (exceptionCaughtHandle == null) {
            exceptionCaughtHandle = new LoggerExceptionCaughtHandle();
        }
        if (ioExceptionCaughtHandle == null) {
            ioExceptionCaughtHandle = new LoggerExceptionCaughtHandle();
        }
        if (StringUtil.isNullOrBlank(configuration.getAPP_FUTURE_ACCEPTOR())) {
            throw new IllegalArgumentException("APP_FUTURE_ACCEPTOR");
        }
        Class<?> clazz = classLoader.loadClass(configuration.getAPP_FUTURE_ACCEPTOR());
        setFutureAcceptor((ContainerIoEventHandle) clazz.newInstance());
        getFutureAcceptor().initialize(channelContext);
    }

    public boolean isDeployModel() {
        return deployModel;
    }

    public boolean isRedeploying() {
        return redeploying;
    }

    // FIXME 考虑部署失败后如何再次部署
    // FIXME keep http session
    public synchronized boolean redeploy() {
        LoggerUtil.prettyLog(logger, "//**********************  开始卸载服务  **********************//");
        this.redeploying = true;
        try {
            destroyHandle(channelContext);
            LoggerUtil.prettyLog(logger,
                    "//**********************  卸载服务完成  **********************//\n");
            LoggerUtil.prettyLog(logger,
                    "//**********************  开始加载服务  **********************//");
            initializeHandle(channelContext);
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

    public void setApplicationConfigurationLoader(ApplicationConfigurationLoader loader) {
        this.configurationLoader = loader;
    }

    public void setApplicationExtLoader(ApplicationExtLoader applicationExtLoader) {
        this.applicationExtLoader = applicationExtLoader;
    }

    public void setAppRedeployService(FutureAcceptor appRedeployService) {
        this.appRedeployService = appRedeployService;
    }

    public void setChannelContext(SocketChannelContext context) {
        this.channelContext = context;
    }

    /**
     * accept抛出异常时则使用该
     * @param exceptionCaughtHandle
     */
    public void setExceptionCaughtHandle(ExceptionCaughtHandle exceptionCaughtHandle) {
        this.exceptionCaughtHandle = exceptionCaughtHandle;
    }

    protected void setFutureAcceptor(ContainerIoEventHandle futureAcceptor) {
        this.futureAcceptor = futureAcceptor;
    }

    public void setIoExceptionCaughtHandle(ExceptionCaughtHandle ioExceptionCaughtHandle) {
        this.ioExceptionCaughtHandle = ioExceptionCaughtHandle;
    }

}
