package com.generallycloud.nio.codec.http2.hpack;

import static com.generallycloud.nio.codec.http2.hpack.Http2Error.COMPRESSION_ERROR;
import static com.generallycloud.nio.codec.http2.hpack.Http2Exception.connectionError;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;

public class DefaultHttp2HeadersEncoder implements Http2HeadersEncoder, Http2HeadersEncoder.Configuration {
	    private final Encoder encoder;
	    private final SensitivityDetector sensitivityDetector;
	    private final Http2HeaderTable headerTable;
	    private final ByteBuf tableSizeChangeOutput = UnpooledByteBufAllocator.getInstance().allocate(1024 * 8);

	    public DefaultHttp2HeadersEncoder() {
	        this(NEVER_SENSITIVE);
	    }

	    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector) {
	        this(sensitivityDetector, new Encoder());
	    }

	    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize) {
	        this(sensitivityDetector, new Encoder(ignoreMaxHeaderListSize));
	    }

	    public DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize,
	                                      int dynamicTableArraySizeHint) {
	        this(sensitivityDetector, new Encoder(ignoreMaxHeaderListSize, dynamicTableArraySizeHint));
	    }

	    /**
	     * Exposed Used for testing only! Default values used in the initial settings frame are overriden intentionally
	     * for testing but violate the RFC if used outside the scope of testing.
	     */
	    DefaultHttp2HeadersEncoder(SensitivityDetector sensitivityDetector, Encoder encoder) {
	        this.sensitivityDetector = sensitivityDetector;
	        this.encoder = encoder;
	        headerTable = new Http2HeaderTableEncoder();
	    }

	    @Override
	    public void encodeHeaders(Http2Headers headers, ByteBuf buffer) throws Http2Exception {
	        try {
	            // If there was a change in the table size, serialize the output from the encoder
	            // resulting from that change.
	            if (tableSizeChangeOutput.hasRemaining()) {
	                buffer.read(tableSizeChangeOutput);
	                tableSizeChangeOutput.clear();
	            }

	            encoder.encodeHeaders(buffer, headers, sensitivityDetector);
	        } catch (Http2Exception e) {
	            throw e;
	        } catch (Throwable t) {
	            throw connectionError(COMPRESSION_ERROR, t, "Failed encoding headers block: %s", t.getMessage());
	        }
	    }

	    @Override
	    public Http2HeaderTable headerTable() {
	        return headerTable;
	    }

	    @Override
	    public Configuration configuration() {
	        return this;
	    }

	    /**
	     * {@link Http2HeaderTable} implementation to support {@link Http2HeadersEncoder}
	     */
	    private final class Http2HeaderTableEncoder implements Http2HeaderTable {
	        @Override
	        public void maxHeaderTableSize(long max) throws Http2Exception {
	            encoder.setMaxHeaderTableSize(tableSizeChangeOutput, max);
	        }

	        @Override
	        public long maxHeaderTableSize() {
	            return encoder.getMaxHeaderTableSize();
	        }

	        @Override
	        public void maxHeaderListSize(long max) throws Http2Exception {
	            encoder.setMaxHeaderListSize(max);
	        }

	        @Override
	        public long maxHeaderListSize() {
	            return encoder.getMaxHeaderListSize();
	        }
	    }
}
