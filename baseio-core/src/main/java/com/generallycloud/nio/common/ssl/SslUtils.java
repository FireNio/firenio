package com.generallycloud.nio.common.ssl;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.IOSession;

/**
 * Constants for SSL packets.
 */
public final class SslUtils {

	private static Logger logger = LoggerFactory.getLogger(SslUtils.class);
	
    /**
     * change cipher spec
     */
    public static final int SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC = 20;

    /**
     * alert
     */
    public static final int SSL_CONTENT_TYPE_ALERT = 21;

    /**
     * handshake
     */
    public static final int SSL_CONTENT_TYPE_HANDSHAKE = 22;

    /**
     * application data
     */
    public static final int SSL_CONTENT_TYPE_APPLICATION_DATA = 23;

    /**
     * the length of the ssl record header (in bytes)
     */
    public static final int SSL_RECORD_HEADER_LENGTH = 5;

    /**
     * Return how much bytes can be read out of the encrypted data. Be aware that this method will not increase
     * the readerIndex of the given {@link ByteBuf}.
     *
     * @param   buffer
     *                  The {@link ByteBuf} to read from. Be aware that it must have at least
     *                  {@link #SSL_RECORD_HEADER_LENGTH} bytes to read,
     *                  otherwise it will throw an {@link IllegalArgumentException}.
     * @return length
     *                  The length of the encrypted packet that is included in the buffer. This will
     *                  return {@code -1} if the given {@link ByteBuf} is not encrypted at all.
     * @throws IllegalArgumentException
     *                  Is thrown if the given {@link ByteBuf} has not at least {@link #SSL_RECORD_HEADER_LENGTH}
     *                  bytes to read.
     */
    public static  int getEncryptedPacketLength(byte [] data, int offset) {
        int packetLength = 0;

        // SSLv3 or TLS - Check ContentType
        
        int h1 = data[offset] & 0xff;
     		   
        boolean tls;
        switch (h1) {
            case SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
            case SSL_CONTENT_TYPE_ALERT:
            case SSL_CONTENT_TYPE_HANDSHAKE:
            case SSL_CONTENT_TYPE_APPLICATION_DATA:
                tls = true;
                break;
            default:
                // SSLv2 or bad data
                tls = false;
        }

        if (tls) {
            // SSLv3 or TLS - Check ProtocolVersion
            int majorVersion = data[offset + 1] & 0xff;
            if (majorVersion == 3) {
                // SSLv3 or TLS
                packetLength = MathUtil.byte2IntFrom2Byte(data, offset + 3) + SSL_RECORD_HEADER_LENGTH;
                if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
                    // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                    tls = false;
                }
            } else {
                // Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
                tls = false;
            }
        }

        if (!tls) {
            // SSLv2 or bad data - Check the version
            int headerLength = (data[offset + 4] & 0x80) != 0 ? 2 : 3;
            int majorVersion = data[offset + headerLength + 5 + 1];
            if (majorVersion == 2 || majorVersion == 3) {
                // SSLv2
                if (headerLength == 2) {
                    packetLength = (MathUtil.byte2IntFrom2Byte(data, offset + 6) & 0x7FFF) + 2;
                } else {
                    packetLength = (MathUtil.byte2IntFrom2Byte(data, offset + 6) & 0x3FFF) + 3;
                }
                if (packetLength <= headerLength) {
                    return -1;
                }
            } else {
                return -1;
            }
        }
        return packetLength;
    }

    public static  void notifyHandshakeFailure(IOSession session, Throwable cause) {
       logger.error(cause.getMessage(),cause);
    }

    /**
     * Fills the {@link ByteBuf} with zero bytes.
     */
    public static  void zeroout(ByteBuf  buf) {
        
    }

    /**
     * Fills the {@link ByteBuf} with zero bytes and releases it.
     */
    public static  void zerooutAndRelease(ByteBuf buffer) {
        zeroout(buffer);
        buffer.release();
    }

    private SslUtils() {
    }
}
