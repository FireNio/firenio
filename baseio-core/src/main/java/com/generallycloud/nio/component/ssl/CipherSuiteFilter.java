package com.generallycloud.nio.component.ssl;

import java.util.List;
import java.util.Set;

/**
 * Provides a means to filter the supplied cipher suite based upon the supported and default cipher suites.
 */
public interface CipherSuiteFilter {

	String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers, Set<String> supportedCiphers);

}

