package com.generallycloud.test.others.algorithm;

import java.io.IOException;
import java.io.OutputStream;

import com.generallycloud.baseio.common.MathUtil;

/**
 * @author wangkai
 *
 */
public class Lz4CompressedOutputStream extends OutputStream {

    private OutputStream     target;

    private Lz4RawCompressor compressor = new Lz4RawCompressor();

    private byte[]           outputBuffer;

    private int              outputBufferLen;

    public Lz4CompressedOutputStream(OutputStream target) {
        this(target, 1024 * 128);
    }

    public Lz4CompressedOutputStream(OutputStream target, int bufSize) {
        this.target = target;
        this.outputBuffer = new byte[bufSize];
        this.outputBufferLen = bufSize - 4;
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        Lz4RawCompressor compressor = this.compressor;
        byte[] outputBuffer = this.outputBuffer;
        int compressedDataLength = compressor.compress(b, off, len, outputBuffer, 4,
                outputBufferLen);
        MathUtil.int2Byte(outputBuffer, compressedDataLength, 0);
        target.write(outputBuffer, 0, compressedDataLength + 4);
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }

    @Override
    public void close() throws IOException {
        target.flush();
        target.close();
    }

}
