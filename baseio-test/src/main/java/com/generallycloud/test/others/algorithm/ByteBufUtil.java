package com.generallycloud.test.others.algorithm;

import java.io.IOException;
import java.io.InputStream;

import com.generallycloud.baseio.buffer.ByteBuf;

/**
 * @author wangkai
 *
 */
public class ByteBufUtil {

    public static int read(ByteBuf buf, InputStream inputStream) throws IOException {
        return read(buf, inputStream, buf.capacity());
    }

    public static int read(ByteBuf buf, InputStream inputStream, long limit) throws IOException {
        byte[] array = buf.array();
        if (!buf.hasRemaining()) {
            int read = (int) Math.min(limit, buf.capacity());
            int len = inputStream.read(array, 0, read);
            if (len > 0) {
                buf.position(0);
                buf.limit(len);
            }
            return len;
        }
        int remaining = buf.remaining();
        System.arraycopy(array, buf.position(), array, 0, remaining);
        buf.position(0);
        buf.limit(remaining);
        int read = (int) Math.min(limit, buf.capacity() - remaining);
        int len = inputStream.read(array, remaining, read);
        if (len > 0) {
            buf.limit(remaining + len);
        }
        return len;
    }

}
