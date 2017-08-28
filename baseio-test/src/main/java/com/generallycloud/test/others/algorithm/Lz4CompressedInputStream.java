package com.generallycloud.test.others.algorithm;

import java.io.IOException;
import java.io.InputStream;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.MathUtil;

/**
 * @author wangkai
 *
 */
public class Lz4CompressedInputStream extends InputStream {

    private ByteBuf     buf;

    private InputStream inputStream;

    private boolean     hasRemaining = true;

    public Lz4CompressedInputStream(InputStream inputStream, int bufSize) {
        this.inputStream = inputStream;
        this.buf = UnpooledByteBufAllocator.getHeapInstance().allocate(bufSize);
        this.buf.limit(0);
    }

    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ByteBuf buf = this.buf;
        byte[] readBuffer = buf.array();
        int limit = buf.limit();
        int offset = buf.position();
        if (buf.remaining() <= 4) {
            if (!hasRemaining) {
                return -1;
            }
            read(buf);
            return read(b, off, len);
        }
        int _len = MathUtil.byte2Int(readBuffer, offset);
        offset += 4;
        if (limit - offset < _len) {
            if (!hasRemaining) {
                return -1;
            }
            read(buf);
            return read(b, off, len);
        }
        buf.position(offset + _len);
        return Lz4RawDecompressor.decompress(readBuffer, offset, _len, b, off, len);
    }

    private int read(ByteBuf buf) throws IOException {
        int len = ByteBufUtil.read(buf, inputStream);
        if (len == -1) {
            hasRemaining = false;
        }
        return len;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

}
