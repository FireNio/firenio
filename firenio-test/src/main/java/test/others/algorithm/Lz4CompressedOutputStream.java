package test.others.algorithm;

import java.io.IOException;
import java.io.OutputStream;

import com.firenio.common.ByteUtil;

/**
 * @author wangkai
 */
public class Lz4CompressedOutputStream extends OutputStream {

    private Lz4RawCompressor compressor = new Lz4RawCompressor();

    private byte[] outputBuffer;

    private int outputBufferLen;

    private OutputStream target;

    public Lz4CompressedOutputStream(OutputStream target) {
        this(target, 1024 * 128);
    }

    public Lz4CompressedOutputStream(OutputStream target, int bufSize) {
        this.target = target;
        this.outputBuffer = new byte[bufSize];
        this.outputBufferLen = bufSize - 4;
    }

    @Override
    public void close() throws IOException {
        target.flush();
        target.close();
    }

    @Override
    public void flush() throws IOException {
        target.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        Lz4RawCompressor compressor   = this.compressor;
        byte[]           outputBuffer = this.outputBuffer;
        int compressedDataLength = compressor.compress(b, off, len, outputBuffer, 4, outputBufferLen);
        ByteUtil.putInt(outputBuffer, compressedDataLength, 0);
        target.write(outputBuffer, 0, compressedDataLength + 4);
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

}
