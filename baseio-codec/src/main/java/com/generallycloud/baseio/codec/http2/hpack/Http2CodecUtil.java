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

import static com.generallycloud.baseio.codec.http2.hpack.Http2Error.PROTOCOL_ERROR;
import static com.generallycloud.baseio.codec.http2.hpack.Http2Exception.streamError;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.component.ssl.ApplicationProtocolNames;

public final class Http2CodecUtil {
    public static final int          CONNECTION_STREAM_ID             = 0;
    public static final int          HTTP_UPGRADE_STREAM_ID           = 1;
    public static final CharSequence HTTP_UPGRADE_SETTINGS_HEADER     = new String(
            "HTTP2-Settings");
    public static final CharSequence HTTP_UPGRADE_PROTOCOL_NAME       = "h2c";
    public static final CharSequence TLS_UPGRADE_PROTOCOL_NAME        = ApplicationProtocolNames.HTTP_2;

    public static final int          PING_FRAME_PAYLOAD_LENGTH        = 8;
    public static final short        MAX_UNSIGNED_BYTE                = 0xFF;
    /**
     * The maximum number of padding bytes. That is the 255 padding bytes appended to the end of a frame and the 1 byte
     * pad length field.
     */
    public static final int          MAX_PADDING                      = 256;
    public static final int          MAX_UNSIGNED_SHORT               = 0xFFFF;
    public static final long         MAX_UNSIGNED_INT                 = 0xFFFFFFFFL;
    public static final int          FRAME_HEADER_LENGTH              = 9;
    public static final int          SETTING_ENTRY_LENGTH             = 6;
    public static final int          PRIORITY_ENTRY_LENGTH            = 5;
    public static final int          INT_FIELD_LENGTH                 = 4;
    public static final short        MAX_WEIGHT                       = 256;
    public static final short        MIN_WEIGHT                       = 1;

    private static final int         MAX_PADDING_LENGTH_LENGTH        = 1;
    public static final int          DATA_FRAME_HEADER_LENGTH         = FRAME_HEADER_LENGTH
            + MAX_PADDING_LENGTH_LENGTH;
    public static final int          HEADERS_FRAME_HEADER_LENGTH      = FRAME_HEADER_LENGTH
            + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH + 1;
    public static final int          PRIORITY_FRAME_LENGTH            = FRAME_HEADER_LENGTH
            + PRIORITY_ENTRY_LENGTH;
    public static final int          RST_STREAM_FRAME_LENGTH          = FRAME_HEADER_LENGTH
            + INT_FIELD_LENGTH;
    public static final int          PUSH_PROMISE_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH
            + MAX_PADDING_LENGTH_LENGTH + INT_FIELD_LENGTH;
    public static final int          GO_AWAY_FRAME_HEADER_LENGTH      = FRAME_HEADER_LENGTH
            + 2 * INT_FIELD_LENGTH;
    public static final int          WINDOW_UPDATE_FRAME_LENGTH       = FRAME_HEADER_LENGTH
            + INT_FIELD_LENGTH;
    public static final int          CONTINUATION_FRAME_HEADER_LENGTH = FRAME_HEADER_LENGTH
            + MAX_PADDING_LENGTH_LENGTH;

    public static final char         SETTINGS_HEADER_TABLE_SIZE       = 1;
    public static final char         SETTINGS_ENABLE_PUSH             = 2;
    public static final char         SETTINGS_MAX_CONCURRENT_STREAMS  = 3;
    public static final char         SETTINGS_INITIAL_WINDOW_SIZE     = 4;
    public static final char         SETTINGS_MAX_FRAME_SIZE          = 5;
    public static final char         SETTINGS_MAX_HEADER_LIST_SIZE    = 6;
    public static final int          NUM_STANDARD_SETTINGS            = 6;

    public static final long         MAX_HEADER_TABLE_SIZE            = MAX_UNSIGNED_INT;
    public static final long         MAX_CONCURRENT_STREAMS           = MAX_UNSIGNED_INT;
    public static final int          MAX_INITIAL_WINDOW_SIZE          = Integer.MAX_VALUE;
    public static final int          MAX_FRAME_SIZE_LOWER_BOUND       = 0x4000;
    public static final int          MAX_FRAME_SIZE_UPPER_BOUND       = 0xFFFFFF;
    public static final long         MAX_HEADER_LIST_SIZE             = MAX_UNSIGNED_INT;

    public static final long         MIN_HEADER_TABLE_SIZE            = 0;
    public static final long         MIN_CONCURRENT_STREAMS           = 0;
    public static final int          MIN_INITIAL_WINDOW_SIZE          = 0;
    public static final long         MIN_HEADER_LIST_SIZE             = 0;

    public static final int          DEFAULT_WINDOW_SIZE              = 65535;
    public static final boolean      DEFAULT_ENABLE_PUSH              = true;
    public static final short        DEFAULT_PRIORITY_WEIGHT          = 16;
    public static final int          DEFAULT_HEADER_TABLE_SIZE        = 4096;
    public static final int          DEFAULT_HEADER_LIST_SIZE         = 8192;
    public static final int          DEFAULT_MAX_FRAME_SIZE           = MAX_FRAME_SIZE_LOWER_BOUND;

    /**
     * Returns {@code true} if the stream is an outbound stream.
     *
     * @param server    {@code true} if the endpoint is a server, {@code false} otherwise.
     * @param streamId  the stream identifier
     */
    public static boolean isOutboundStream(boolean server, int streamId) {
        boolean even = (streamId & 1) == 0;
        return streamId > 0 && server == even;
    }

    /**
     * Returns true if the {@code streamId} is a valid HTTP/2 stream identifier.
     */
    public static boolean isStreamIdValid(int streamId) {
        return streamId >= 0;
    }

    /**
     * The assumed minimum value for {@code SETTINGS_MAX_CONCURRENT_STREAMS} as
     * recommended by the HTTP/2 spec.
     */
    public static final int SMALLEST_MAX_CONCURRENT_STREAMS = 100;

    /**
     * Indicates whether or not the given value for max frame size falls within the valid range.
     */
    public static boolean isMaxFrameSizeValid(int maxFrameSize) {
        return maxFrameSize >= MAX_FRAME_SIZE_LOWER_BOUND
                && maxFrameSize <= MAX_FRAME_SIZE_UPPER_BOUND;
    }

    /**
     * Returns a buffer filled with all zeros that is the appropriate length for a PING frame.
     */
    public static ByteBuf emptyPingBuf() {
        // Return a duplicate so that modifications to the reader index will not affect the original buffer.
        return EmptyByteBuf.getInstance().duplicate();
    }

    /**
     * Iteratively looks through the causaility chain for the given exception and returns the first
     * {@link Http2Exception} or {@code null} if none.
     */
    public static Http2Exception getEmbeddedHttp2Exception(Throwable cause) {
        while (cause != null) {
            if (cause instanceof Http2Exception) {
                return (Http2Exception) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    /**
     * Reads a big-endian (31-bit) integer from the buffer.
     */
    public static int readUnsignedInt(ByteBuf buf) {
        return buf.getInt() & 0x7fffffff;
    }

    /**
     * Writes a big-endian (32-bit) unsigned integer to the buffer.
     */
    public static void writeUnsignedInt(long value, ByteBuf out) {
        out.putByte((byte) (value >> 24 & 0xFF));
        out.putByte((byte) (value >> 16 & 0xFF));
        out.putByte((byte) (value >> 8 & 0xFF));
        out.putByte((byte) (value & 0xFF));
    }

    /**
     * Writes a big-endian (16-bit) unsigned integer to the buffer.
     */
    public static void writeUnsignedShort(int value, ByteBuf out) {
        out.putByte((byte) (value >> 8 & 0xFF));
        out.putByte((byte) (value & 0xFF));
    }

    public static void headerListSizeExceeded(int streamId, long maxHeaderListSize)
            throws Http2Exception {
        throw streamError(streamId, PROTOCOL_ERROR, "Header size exceeded max allowed size (%d)",
                maxHeaderListSize);
    }

    //	    static void writeFrameHeaderInternal(ByteBuf out, int payloadLength, byte type,
    //	            byte flags, int streamId) {
    //	        out.writeMedium(payloadLength);
    //	        out.put(type);
    //	        out.put(flags);
    //	        out.writeInt(streamId);
    //	    }

    public static void verifyPadding(int padding) {
        if (padding < 0 || padding > MAX_PADDING) {
            throw new IllegalArgumentException(String.format(
                    "Invalid padding '%d'. Padding must be between 0 and " + "%d (inclusive).",
                    padding, MAX_PADDING));
        }
    }

    private Http2CodecUtil() {}
}
