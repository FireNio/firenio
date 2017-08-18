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

import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.X509Certificate;

final class JdkSslSession implements SSLSession {

    private int        packetBufferSize;
    private String     applicationProtocol;
    private SSLSession sslSession;

    JdkSslSession(SSLSession sslSession) {
        this.sslSession = sslSession;
        this.packetBufferSize = sslSession.getPacketBufferSize();
    }

    private SSLSession unwrap() {
        return sslSession;
    }

    @Override
    public String getProtocol() {
        return unwrap().getProtocol();
    }

    public String getApplicationProtocol() {
        return applicationProtocol;
    }

    protected void setApplicationProtocol(String applicationProtocol) {
        this.applicationProtocol = applicationProtocol;
    }

    @Override
    public byte[] getId() {
        return unwrap().getId();
    }

    @Override
    public SSLSessionContext getSessionContext() {
        return unwrap().getSessionContext();
    }

    @Override
    public long getCreationTime() {
        return unwrap().getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return unwrap().getLastAccessedTime();
    }

    @Override
    public void invalidate() {
        unwrap().invalidate();
    }

    @Override
    public boolean isValid() {
        return unwrap().isValid();
    }

    @Override
    public void putValue(String s, Object o) {
        unwrap().putValue(s, o);
    }

    @Override
    public Object getValue(String s) {
        return unwrap().getValue(s);
    }

    @Override
    public void removeValue(String s) {
        unwrap().removeValue(s);
    }

    @Override
    public String[] getValueNames() {
        return unwrap().getValueNames();
    }

    @Override
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return unwrap().getPeerCertificates();
    }

    @Override
    public Certificate[] getLocalCertificates() {
        return unwrap().getLocalCertificates();
    }

    @Override
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return unwrap().getPeerCertificateChain();
    }

    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return unwrap().getPeerPrincipal();
    }

    @Override
    public Principal getLocalPrincipal() {
        return unwrap().getLocalPrincipal();
    }

    @Override
    public String getCipherSuite() {
        return unwrap().getCipherSuite();
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
    public int getPacketBufferSize() {
        return packetBufferSize;
    }

    @Override
    public int getApplicationBufferSize() {
        return getPacketBufferSize();
    }
}
