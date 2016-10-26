package com.generallycloud.nio.common.ssl;

import java.util.ArrayList;
import java.util.List;

final class ApplicationProtocolUtil {
    private static final int DEFAULT_LIST_SIZE = 2;

    private ApplicationProtocolUtil() {
    }

    static List<String> toList(Iterable<String> protocols) {
        return toList(DEFAULT_LIST_SIZE, protocols);
    }

    static List<String> toList(int initialListSize, Iterable<String> protocols) {
        if (protocols == null) {
            return null;
        }

        List<String> result = new ArrayList<String>(initialListSize);
        for (String p : protocols) {
            if (p == null || p.isEmpty()) {
                throw new IllegalArgumentException("protocol cannot be null or empty");
            }
            result.add(p);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("protocols cannot empty");
        }

        return result;
    }

    static List<String> toList(String... protocols) {
        return toList(DEFAULT_LIST_SIZE, protocols);
    }

    static List<String> toList(int initialListSize, String... protocols) {
        if (protocols == null) {
            return null;
        }

        List<String> result = new ArrayList<String>(initialListSize);
        for (String p : protocols) {
            if (p == null || p.isEmpty()) {
                throw new IllegalArgumentException("protocol cannot be null or empty");
            }
            result.add(p);
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("protocols cannot empty");
        }

        return result;
    }
}
