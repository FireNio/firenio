package com.generallycloud.nio.codec.http2.hpack;

import com.generallycloud.nio.buffer.ByteBuf;

/**
 * Encodes {@link Http2Headers} into HPACK-encoded headers blocks.
 */
public interface Http2HeadersEncoder {
    /**
     * Configuration related elements for the {@link Http2HeadersEncoder} interface
     */
    interface Configuration {
        /**
         * Access the Http2HeaderTable for this {@link Http2HeadersEncoder}
         */
        Http2HeaderTable headerTable();
    }

    /**
     * Determine if a header name/value pair is treated as
     * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
     * If the object can be dynamically modified and shared across multiple connections it may need to be thread safe.
     */
    interface SensitivityDetector {
        /**
         * Determine if a header {@code name}/{@code value} pair should be treated as
         * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
         * @param name The name for the header.
         * @param value The value of the header.
         * @return {@code true} if a header {@code name}/{@code value} pair should be treated as
         * <a href="http://tools.ietf.org/html/draft-ietf-httpbis-header-compression-12#section-7.1.3">sensitive</a>.
         * {@code false} otherwise.
         */
        boolean isSensitive(CharSequence name, CharSequence value);
    }

    /**
     * Encodes the given headers and writes the output headers block to the given output buffer.
     *
     * @param headers the headers to be encoded.
     * @param buffer the buffer to receive the encoded headers.
     */
    void encodeHeaders(Http2Headers headers, ByteBuf buffer) throws Http2Exception;

    /**
     * Get the {@link Configuration} for this {@link Http2HeadersEncoder}
     */
    Configuration configuration();

    /**
     * Always return {@code false} for {@link SensitivityDetector#isSensitive(CharSequence, CharSequence)}.
     */
    SensitivityDetector NEVER_SENSITIVE = new SensitivityDetector() {
        @Override
        public boolean isSensitive(CharSequence name, CharSequence value) {
            return false;
        }
    };

    /**
     * Always return {@code true} for {@link SensitivityDetector#isSensitive(CharSequence, CharSequence)}.
     */
    SensitivityDetector ALWAYS_SENSITIVE = new SensitivityDetector() {
        @Override
        public boolean isSensitive(CharSequence name, CharSequence value) {
            return true;
        }
    };
}

