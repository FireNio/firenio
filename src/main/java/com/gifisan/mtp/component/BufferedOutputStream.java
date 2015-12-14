package com.gifisan.mtp.component;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class BufferedOutputStream extends OutputStream{
	
    protected byte buf[];

    protected int count;

    public BufferedOutputStream() {
    	this(32);
    }

    public BufferedOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        buf = new byte[size];
    }

    public void close() throws IOException {
    }
    
    public void reset() {
    	count = 0;
    }
    
	public int size() {
    	return count;
    }

	public byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    public String toString() {
    	return new String(buf, 0, count);
    }

    public String toString(String charsetName) throws UnsupportedEncodingException {
    	return new String(buf, 0, count, charsetName);
    }

    public void write(byte bytes[], int off, int len) {
		if (	(off < 0) 
				|| (off > bytes.length) 
				|| (len < 0) 
				|| ((off + len) > bytes.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return;
		}
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(bytes, off, buf, count, len);
        count = newcount;
    }

    public void write(byte[] bytes) {
		this.write(bytes, 0, bytes.length);
	}

    /**
     * writeByte instead
     */
    @Deprecated
    public void write(int b) {
		this.writeByte((byte)b);
    }
    
    public void writeByte(byte b){
    	
    	int newcount = count + 1;
		if (newcount > buf.length) {
	            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
		}
		buf[count] = b;
		count = newcount;
    }

    public void writeTo(OutputStream out) throws IOException {
    	out.write(buf, 0, count);
    }

}
