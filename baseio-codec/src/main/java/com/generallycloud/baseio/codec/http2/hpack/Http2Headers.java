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
package com.generallycloud.baseio.codec.http2.hpack;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A collection of headers sent or received via HTTP/2.
 */
public interface Http2Headers extends Iterable<Entry<String, String>> {

    /**
     * HTTP/2 pseudo-headers names.
     */
    enum PseudoHeaderName {
        /**
         * {@code :method}.
         */
        METHOD(":method"),

        /**
         * {@code :scheme}.
         */
        SCHEME(":scheme"),

        /**
         * {@code :authority}.
         */
        AUTHORITY(":authority"),

        /**
         * {@code :path}.
         */
        PATH(":path"),

        /**
         * {@code :status}.
         */
        STATUS(":status");

        private final String             value;
        private static final Set<String> PSEUDO_HEADERS = new HashSet<>();
        static {
            for (PseudoHeaderName pseudoHeader : PseudoHeaderName.values()) {
                PSEUDO_HEADERS.add(pseudoHeader.value());
            }
        }

        PseudoHeaderName(String value) {
            this.value = value;
        }

        public String value() {
            // Return a slice so that the buffer gets its own reader index.
            return value;
        }

        /**
         * Indicates whether the given header name is a valid HTTP/2 pseudo
         * header.
         */
        public static boolean isPseudoHeader(String header) {
            return PSEUDO_HEADERS.contains(header);
        }
    }

    /**
     * Returns an iterator over all HTTP/2 headers. The iteration order is as
     * follows: 1. All pseudo headers (order not specified). 2. All non-pseudo
     * headers (in insertion order).
     */
    @Override
    Iterator<Entry<String, String>> iterator();

    /**
     * Sets the {@link PseudoHeaderName#METHOD} header or {@code null} if there
     * is no such header
     */
    Http2Headers method(String value);

    /**
     * Sets the {@link PseudoHeaderName#SCHEME} header if there is no such
     * header
     */
    Http2Headers scheme(String value);

    /**
     * Sets the {@link PseudoHeaderName#AUTHORITY} header or {@code null} if
     * there is no such header
     */
    Http2Headers authority(String value);

    /**
     * Sets the {@link PseudoHeaderName#PATH} header or {@code null} if there
     * is no such header
     */
    Http2Headers path(String value);

    /**
     * Sets the {@link PseudoHeaderName#STATUS} header or {@code null} if there
     * is no such header
     */
    Http2Headers status(String value);

    /**
     * Gets the {@link PseudoHeaderName#METHOD} header or {@code null} if there
     * is no such header
     */
    String method();

    /**
     * Gets the {@link PseudoHeaderName#SCHEME} header or {@code null} if there
     * is no such header
     */
    String scheme();

    /**
     * Gets the {@link PseudoHeaderName#AUTHORITY} header or {@code null} if
     * there is no such header
     */
    String authority();

    /**
     * Gets the {@link PseudoHeaderName#PATH} header or {@code null} if there
     * is no such header
     */
    String path();

    /**
     * Gets the {@link PseudoHeaderName#STATUS} header or {@code null} if there
     * is no such header
     */
    String status();

    void add(String name, String value);
}
