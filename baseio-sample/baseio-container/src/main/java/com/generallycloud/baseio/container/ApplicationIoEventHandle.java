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

import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.LifeCycleListener;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ChannelEventListener;
import com.generallycloud.baseio.component.ConfigurationParser;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.FrameAcceptor;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerExceptionCaughtHandle;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.configuration.ApplicationConfiguration;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Frame;

public class ApplicationIoEventHandle extends IoEventHandle implements LifeCycleListener {

    private ApplicationExtLoader           applicationExtLoader;
    private String                         appLocalAddres;
    private FrameAcceptor                  appOnRedeployService;
    private ChannelContext                 context;
    private URLDynamicClassLoader          classLoader;
    private ApplicationConfiguration       configuration;
    private ApplicationConfigurationLoader configurationLoader;
    private volatile boolean               deploying    = true;
    private String                         runtimeMode;
    private Charset                        encoding;
    private ContainerIoEventHandle         frameAcceptor;
    private ExceptionCaughtHandle          ioExceptionCaughtHandle;
    private Logger                         logger       = LoggerFactory.getLogger(getClass());
    private AtomicInteger                  redeployTime = new AtomicInteger();
    private String                         rootLocalAddress;

    // exceptionCaughtHandle ; ioExceptionCaughtHandle
    // 区分 socket || application exception handle
    public ApplicationIoEventHandle(String rootPath, String runtimeMode) {
        this.rootLocalAddress = rootPath;
        this.runtimeMode = runtimeMode;
    }

    @Override
    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        if (deploying) {
            appOnRedeployService.accept(ch, frame);
            return;
        }
        try {
            frameAcceptor.accept(ch, frame);
        } catch (Exception e) {
            frameAcceptor.exceptionCaught(ch, frame, e);
        }
    }

    public void addChannelEventListener(ChannelEventListener listener) {
        context.addChannelEventListener(listener);
    }

    private void destroy(ChannelContext context) throws Exception {
        this.deploying = true;
        this.destroyHandle(context, false);
    }

    private void destroyHandle(ChannelContext context, boolean redeploy) {
        frameAcceptor.destroy(context, redeploy);
        classLoader.unloadClassLoader();
    }

    @Override
    public void exceptionCaught(NioSocketChannel ch, Frame frame, Exception ex) {
        ioExceptionCaughtHandle.exceptionCaught(ch, frame, ex);
    }

    public ApplicationExtLoader getApplicationExtLoader() {
        return applicationExtLoader;
    }

    public String getAppLocalAddress() {
        return appLocalAddres;
    }

    public ChannelContext getChannelContext() {
        return context;
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

    public ContainerIoEventHandle getFrameAcceptor() {
        return frameAcceptor;
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

    private void initialize(ChannelContext context) throws Exception {
        this.context = context;
        if (StringUtil.isNullOrBlank(rootLocalAddress)) {
            throw new IllegalArgumentException("rootLocalAddress");
        }
        if (applicationExtLoader == null) {
            applicationExtLoader = new DefaultExtLoader();
        }
        if (configurationLoader == null) {
            configurationLoader = new FileSystemACLoader();
        }
        if (configuration == null) {
            configuration = new ApplicationConfiguration();
            ConfigurationParser.parseConfiguration("app.", configuration, context.getProperties());
        }
        this.rootLocalAddress = FileUtil.getPrettyPath(rootLocalAddress);
        this.encoding = context.getCharset();
        this.appLocalAddres = FileUtil.getPrettyPath(getRootLocalAddress() + "app");
        LoggerUtil.prettyLog(logger, "application path      :[ {} ]", appLocalAddres);
        this.initializeHandle(context, false);
        this.deploying = false;
    }

    private void initializeHandle(ChannelContext context, boolean redeploy) throws Exception {
        ClassLoader parent = getClass().getClassLoader();
        this.classLoader = ApplicationBootstrap.newClassLoader(parent, runtimeMode, true,
                rootLocalAddress, ApplicationBootstrap.withDefault());
        this.applicationExtLoader.loadExts(this, classLoader);
        this.appOnRedeployService = (FrameAcceptor) newInstanceFromClass(
                configuration.getOnRedeployFrameAcceptor(), appOnRedeployService);
        if (appOnRedeployService == null) {
            appOnRedeployService = new DefaultOnRedeployAcceptor();
        }
        this.ioExceptionCaughtHandle = (ExceptionCaughtHandle) newInstanceFromClass(
                configuration.getIoExceptionCaughtHandle(), ioExceptionCaughtHandle);
        if (ioExceptionCaughtHandle == null) {
            ioExceptionCaughtHandle = new LoggerExceptionCaughtHandle();
        }
        if (StringUtil.isNullOrBlank(configuration.getFrameAcceptor())) {
            throw new IllegalArgumentException("APP_FRAME_ACCEPTOR");
        }
        Class<?> clazz = classLoader.loadClass(configuration.getFrameAcceptor());
        frameAcceptor = (ContainerIoEventHandle) clazz.newInstance();
        frameAcceptor.initialize(context, redeploy);
    }

    private Object newInstanceFromClass(String className, Object defaultObj) throws Exception {
        if (StringUtil.isNullOrBlank(className)) {
            return defaultObj;
        }
        Class<?> clazz = classLoader.loadClass(className);
        return clazz.newInstance();
    }

    public String getRuntimeMode() {
        return runtimeMode;
    }

    public boolean isDeploying() {
        return deploying;
    }

    // FIXME 考虑部署失败后如何再次部署
    // FIXME keep http ch
    public synchronized boolean redeploy() {
        this.deploying = true;
        try {
            LoggerUtil.prettyLog(logger,
                    "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  开始卸载服务  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            destroyHandle(context, true);

            LoggerUtil.prettyLog(logger,
                    "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  开始加载服务  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            initializeHandle(context, true);
            deploying = false;
            LoggerUtil.prettyLog(logger,
                    "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  加载服务完成  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            System.gc();
            return true;
        } catch (Exception e) {
            classLoader.unloadClassLoader();
            deploying = false;
            LoggerUtil.prettyLog(logger,
                    "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^  加载服务失败  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
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

    public void setChannelContext(ChannelContext context) {
        this.context = context;
    }

    public void setAppOnRedeployService(FrameAcceptor appOnRedeployService) {
        this.appOnRedeployService = appOnRedeployService;
    }

    public void setIoExceptionCaughtHandle(ExceptionCaughtHandle ioExceptionCaughtHandle) {
        this.ioExceptionCaughtHandle = ioExceptionCaughtHandle;
    }

    @Override
    public int lifeCycleListenerSortIndex() {
        return 0;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifeCycle) {

    }

    @Override
    public void lifeCycleStarted(LifeCycle lifeCycle) {
        try {
            initialize((ChannelContext) lifeCycle);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifeCycle, Exception exception) {

    }

    @Override
    public void lifeCycleStopping(LifeCycle lifeCycle) {
        try {
            destroy((ChannelContext) lifeCycle);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifeCycle) {

    }

}
