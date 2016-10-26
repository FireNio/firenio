package com.generallycloud.nio.common.ssl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class will not do any filtering of ciphers suites.
 */
public final class IdentityCipherSuiteFilter implements CipherSuiteFilter {
	public static final IdentityCipherSuiteFilter	INSTANCE	= new IdentityCipherSuiteFilter();

	private IdentityCipherSuiteFilter() {
	}

	@Override
	public String[] filterCipherSuites(Iterable<String> ciphers, List<String> defaultCiphers,
			Set<String> supportedCiphers) {
		if (ciphers == null) {
			return defaultCiphers.toArray(new String[defaultCiphers.size()]);
		} else {
			// List<String> newCiphers =
			// InternalThreadLocalMap.get().arrayList(supportedCiphers.size());

			List<String> newCiphers = new ArrayList<String>();
			for (String c : ciphers) {
				if (c == null) {
					break;
				}
				newCiphers.add(c);
			}
			return newCiphers.toArray(new String[newCiphers.size()]);
		}
	}

}
