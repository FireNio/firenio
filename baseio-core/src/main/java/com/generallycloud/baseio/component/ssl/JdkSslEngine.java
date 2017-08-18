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
package com.generallycloud.baseio.component.ssl;

import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;

class JdkSslEngine extends SSLEngine {
    private final SSLEngine     engine;
    private final JdkSslSession session;

    JdkSslEngine(SSLEngine engine) {
        this.engine = engine;
        this.session = new JdkSslSession(engine.getSession());
    }

    @Override
    public JdkSslSession getSession() {
        return session;
    }

    public SSLEngine unwrap() {
        return engine;
    }

    @Override
    public void closeInbound() throws SSLException {
        unwrap().closeInbound();
    }

    @Override
    public void closeOutbound() {
        unwrap().closeOutbound();
    }

    @Override
    public String getPeerHost() {
        return unwrap().getPeerHost();
    }

    @Override
    public int getPeerPort() {
        return unwrap().getPeerPort();
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2) throws SSLException {
        return unwrap().wrap(byteBuffer, byteBuffer2);
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] byteBuffers, ByteBuffer byteBuffer)
            throws SSLException {
        return unwrap().wrap(byteBuffers, byteBuffer);
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] byteBuffers, int i, int i2, ByteBuffer byteBuffer)
            throws SSLException {
        return unwrap().wrap(byteBuffers, i, i2, byteBuffer);
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer byteBuffer2)
            throws SSLException {
        return unwrap().unwrap(byteBuffer, byteBuffer2);
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers)
            throws SSLException {
        return unwrap().unwrap(byteBuffer, byteBuffers);
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer byteBuffer, ByteBuffer[] byteBuffers, int i, int i2)
            throws SSLException {
        return unwrap().unwrap(byteBuffer, byteBuffers, i, i2);
    }

    @Override
    public Runnable getDelegatedTask() {
        return unwrap().getDelegatedTask();
    }

    @Override
    public boolean isInboundDone() {
        return unwrap().isInboundDone();
    }

    @Override
    public boolean isOutboundDone() {
        return unwrap().isOutboundDone();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return unwrap().getSupportedCipherSuites();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return unwrap().getEnabledCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(String[] strings) {
        unwrap().setEnabledCipherSuites(strings);
    }

    @Override
    public String[] getSupportedProtocols() {
        return unwrap().getSupportedProtocols();
    }

    @Override
    public String[] getEnabledProtocols() {
        return unwrap().getEnabledProtocols();
    }

    @Override
    public void setEnabledProtocols(String[] strings) {
        unwrap().setEnabledProtocols(strings);
    }

    @Override
    public void beginHandshake() throws SSLException {
        unwrap().beginHandshake();
    }

    @Override
    public HandshakeStatus getHandshakeStatus() {
        return unwrap().getHandshakeStatus();
    }

    @Override
    public void setUseClientMode(boolean b) {
        unwrap().setUseClientMode(b);
    }

    @Override
    public boolean getUseClientMode() {
        return unwrap().getUseClientMode();
    }

    @Override
    public void setNeedClientAuth(boolean b) {
        unwrap().setNeedClientAuth(b);
    }

    @Override
    public boolean getNeedClientAuth() {
        return unwrap().getNeedClientAuth();
    }

    @Override
    public void setWantClientAuth(boolean b) {
        unwrap().setWantClientAuth(b);
    }

    @Override
    public boolean getWantClientAuth() {
        return unwrap().getWantClientAuth();
    }

    @Override
    public void setEnableSessionCreation(boolean b) {
        unwrap().setEnableSessionCreation(b);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return unwrap().getEnableSessionCreation();
    }

    @Override
    public SSLParameters getSSLParameters() {
        return unwrap().getSSLParameters();
    }

    @Override
    public void setSSLParameters(SSLParameters sslParameters) {
        unwrap().setSSLParameters(sslParameters);
    }
}
