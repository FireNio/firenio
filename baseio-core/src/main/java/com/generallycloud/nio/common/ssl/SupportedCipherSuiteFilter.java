package com.generallycloud.nio.common.ssl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLEngine;

/**
 * This class will filter all requested ciphers out that are not supported by the current {@link SSLEngine}.
 */
public final class SupportedCipherSuiteFilter implements CipherSuiteFilter {
    public static final SupportedCipherSuiteFilter INSTANCE = new SupportedCipherSuiteFilter();

    private SupportedCipherSuiteFilter() { }

    @Override
    public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers,
            Set<String> supportedCiphers) {
        if (defaultCiphers == null) {
            throw new NullPointerException("defaultCiphers");
        }
        if (supportedCiphers == null) {
            throw new NullPointerException("supportedCiphers");
        }

        final List<String> newCiphers = new ArrayList<String>();
//        if (ciphers == null) {
//            newCiphers = InternalThreadLocalMap.get().arrayList(defaultCiphers.size());
//            ciphers = defaultCiphers;
//        } else {
//            newCiphers = InternalThreadLocalMap.get().arrayList(supportedCiphers.size());
//        }
        for (String c : ciphers) {
            if (c == null) {
                break;
            }
            if (supportedCiphers.contains(c)) {
                newCiphers.add(c);
            }
        }
        return newCiphers.toArray(new String[newCiphers.size()]);
    }

}
