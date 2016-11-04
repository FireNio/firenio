package com.generallycloud.nio.codec.http2.hpack;


public interface Http2HeaderTable {
	
	void maxHeaderTableSize(long max) throws Http2Exception;

	/**
	 * Represents the value for <a
	 * href="https://tools.ietf.org/html/rfc7540#section-6.5.2"
	 * >SETTINGS_HEADER_TABLE_SIZE</a>. The initial value returned by this
	 * method must be {@link Http2CodecUtil#DEFAULT_HEADER_TABLE_SIZE}.
	 */
	long maxHeaderTableSize();

	void maxHeaderListSize(long max) throws Http2Exception;

	/**
	 * Represents the value for <a
	 * href="https://tools.ietf.org/html/rfc7540#section-6.5.2"
	 * >SETTINGS_MAX_HEADER_LIST_SIZE</a>. The initial value returned by this
	 * method must be {@link Http2CodecUtil#DEFAULT_HEADER_LIST_SIZE}.
	 */
	long maxHeaderListSize();
}
