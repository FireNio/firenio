/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.codec.http11;

import static com.firenio.baseio.codec.http11.HttpHeader.Content_Length;
import static com.firenio.baseio.codec.http11.HttpHeader.Content_Type;
import static com.firenio.baseio.common.ByteUtil.b;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.firenio.baseio.Develop;
import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.collection.IntMap;
import com.firenio.baseio.common.ByteUtil;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.component.FastThreadLocal;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.NioEventLoop;
import com.firenio.baseio.component.ProtocolCodec;

/**
 * @author wangkai
 *
 */
public class HttpCodec extends ProtocolCodec {

    static final byte[]      CONTENT_LENGTH_MATCH      = b("Content-Length:");
    static final int         decode_state_body         = 2;
    static final int         decode_state_complate     = 3;
    static final int         decode_state_header       = 1;
    static final int         decode_state_line_one     = 0;
    static final int         encode_bytes_arrays_index = nextIndexedVariablesIndex();
    static final int         content_len_index         = nextIndexedVariablesIndex();
    static final String      FRAME_CACHE_KEY           = "_HTTP_FRAME_CACHE_KEY";
    static final KMPUtil     KMP_BOUNDARY              = new KMPUtil("boundary=");
    static final byte        N                         = '\n';
    static final IOException OVER_LIMIT                = EXCEPTION("over limit");
    static final byte        R                         = '\r';
    static final byte        SPACE                     = ' ';

    private final int        blimit;
    private final byte[][]   CONTENT_LENGTHS           = new byte[1024][];
    private final int        hlimit;
    private final int        fcache;
    private final boolean    lite;
    private final boolean    inline;
    private final ByteBuffer contentLenBuf;

    public HttpCodec() {
        this(0);
    }

    public HttpCodec(int frameCache) {
        this(null, frameCache);
    }

    public HttpCodec(String server) {
        this(server, 0);
    }

    public HttpCodec(String server, int frameCache) {
        this(server, frameCache, false, false);
    }

    public HttpCodec(String server, int frameCache, boolean lite, boolean inline) {
        this(server, frameCache, 1024 * 8, 1024 * 256, lite, inline);
    }

    public HttpCodec(String server, int fcache, int hlimit, int blimit, boolean lite,
            boolean inline) {
        this.lite = lite;
        this.inline = inline;
        this.hlimit = hlimit;
        this.blimit = blimit;
        this.fcache = fcache;
        ByteBuffer temp = ByteBuffer.allocate(128);
        if (server == null) {
            temp.put(b("\r\nContent-Length: "));
        } else {
            temp.put(b("\r\nServer: " + server + "\r\nContent-Length: "));
        }
        contentLenBuf = temp.duplicate();
        contentLenBuf.flip();
        int p = temp.position();
        for (int i = 0; i < CONTENT_LENGTHS.length; i++) {
            temp.clear().position(p);
            temp.put(String.valueOf(i).getBytes());
            temp.flip();
            CONTENT_LENGTHS[i] = new byte[temp.limit()];
            temp.get(CONTENT_LENGTHS[i]);
        }
    }

    HttpFrame newFrame() {
        return new HttpFrame();
    }

    private HttpFrame allocFrame(NioEventLoop el) {
        if (fcache > 0) {
            Frame res = (Frame) el.getCache(FRAME_CACHE_KEY, fcache);
            if (res == null) {
                return newFrame();
            } else {
                return (HttpFrame) res.reset();
            }
        }
        return newFrame();
    }

    private int decode_lite(ByteBuf src, HttpFrame f) throws IOException {
        int decode_state = f.getDecodeState();
        int abs_pos = src.absPos();
        int h_len = f.getHeaderLength();
        if (decode_state == decode_state_line_one) {
            int ln = src.indexOf(N);
            if (ln == -1) {
                return decode_state_line_one;
            } else {
                int p = abs_pos;
                h_len += (ln - p);
                decode_state = decode_state_header;
                int skip;
                if (src.absByte(p) == 'G') {
                    f.setMethod(HttpMethod.GET);
                    skip = 4;
                } else {
                    f.setMethod(HttpMethod.POST);
                    skip = 5;
                }
                StringBuilder line = FastThreadLocal.get().getStringBuilder();
                int count = ln - 10;
                for (int i = p + skip; i < count; i++) {
                    line.append((char) (src.absByte(i) & 0xff));
                }
                int qmark = Util.indexOf(line, '?');
                if (qmark > -1) {
                    parse_kv(f.getRequestParams(), line, qmark + 1, line.length(), '=', '&');
                    f.setRequestURL((String) line.subSequence(0, qmark));
                } else {
                    f.setRequestURL(line.toString());
                }
                abs_pos = ln + 1;
            }
        }
        if (decode_state == decode_state_header) {
            for (;;) {
                int ps = abs_pos;
                int pe = read_line_range(src, ps, h_len, hlimit);
                if (pe == -1) {
                    f.setHeaderLength(h_len);
                    src.absPos(abs_pos);
                    break;
                }
                abs_pos = pe-- + 1;
                int size = pe - ps;
                h_len += size;
                if (size == 0) {
                    if (f.getContentLength() < 1) {
                        decode_state = decode_state_complate;
                    } else {
                        if (f.getContentLength() > blimit) {
                            throw OVER_LIMIT;
                        }
                        decode_state = decode_state_body;
                    }
                    src.absPos(abs_pos);
                    break;
                } else {
                    if (!f.isGet()) {
                        if (start_with(src, ps, pe, CONTENT_LENGTH_MATCH)) {
                            int cp = ps + CONTENT_LENGTH_MATCH.length;
                            int cps = ByteUtil.skip(src, cp, pe, SPACE);
                            if (cps == -1) {
                                throw OVER_LIMIT;
                            }
                            int ctLen = 0;
                            for (int i = cps; i < pe; i++) {
                                ctLen = (src.absByte(i) - '0') + ctLen * 10;
                            }
                            f.setContentLength(ctLen);
                        }
                    }
                }
            }
        }
        return decode_state;
    }

    private int decode_full(ByteBuf src, HttpFrame f) throws IOException {
        int decode_state = f.getDecodeState();
        StringBuilder line = FastThreadLocal.get().getStringBuilder();
        int h_len = f.getHeaderLength();
        int abs_pos = src.absPos();
        if (decode_state == decode_state_line_one) {
            int pn = read_line(line, src, abs_pos, 0, hlimit);
            if (pn == -1) {
                return decode_state_line_one;
            } else {
                abs_pos = pn;
                h_len += line.length();
                decode_state = decode_state_header;
                parse_line_one(f, line);
            }
        }
        if (decode_state == decode_state_header) {
            for (;;) {
                line.setLength(0);
                int pn = read_line(line, src, abs_pos, h_len, hlimit);
                if (pn == -1) {
                    src.absPos(abs_pos);
                    f.setHeaderLength(h_len);
                    break;
                }
                abs_pos = pn;
                h_len += line.length();
                if (line.length() == 0) {
                    src.absPos(abs_pos);
                    decode_state = onHeaderReadComplete(f);
                    break;
                } else {
                    int p = Util.indexOf(line, ':');
                    if (p == -1) {
                        continue;
                    }
                    int rp = Util.skip(line, ' ', p + 1);
                    String name = line.substring(0, p);
                    String value = line.substring(rp);
                    f.setReadHeader(name, value);
                }
            }
        }
        return decode_state;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws Exception {
        boolean remove = false;
        HttpAttachment att = (HttpAttachment) ch.getAttachment();
        HttpFrame f = att.getUncompleteFrame();
        if (f == null) {
            f = allocFrame(ch.getEventLoop());
        } else {
            remove = true;
        }
        int decode_state;
        if (lite) {
            decode_state = decode_lite(src, f);
        } else {
            decode_state = decode_full(src, f);
        }
        if (decode_state == decode_state_body) {
            decode_state = decodeRemainBody(ch, src, f);
        }
        if (decode_state == decode_state_complate) {
            if (remove) {
                att.setUncompleteFrame(null);
            }
            return f;
        } else {
            f.setDecodeState(decode_state);
            att.setUncompleteFrame(f);
            return null;
        }
    }

    int decodeRemainBody(Channel ch, ByteBuf src, HttpFrame f) {
        int contentLength = f.getContentLength();
        int remain = src.remaining();
        if (remain < contentLength) {
            return decode_state_body;
        } else {
            byte[] content = new byte[contentLength];
            src.getBytes(content);
            if (f.isForm()) {
                String param = new String(content, ch.getCharset());
                parse_kv(f.getRequestParams(), param, 0, param.length(), '=', '&');
            } else {
                f.setContent(content);
            }
            return decode_state_complate;
        }
    }

    private byte[] getContentLenBuf(FastThreadLocal l) {
        byte[] bb = (byte[]) l.getIndexedVariable(content_len_index);
        if (bb == null) {
            int limit = contentLenBuf.limit();
            bb = new byte[contentLenBuf.limit() + 16];
            contentLenBuf.get(bb, 0, limit);
            contentLenBuf.clear().limit(limit);
            l.setIndexedVariable(content_len_index, bb);
        }
        return bb;
    }

    @Override
    public ByteBuf encode(final Channel ch, Frame frame) throws IOException {
        boolean inline = this.inline;
        HttpFrame f = (HttpFrame) frame;
        FastThreadLocal l = FastThreadLocal.get();
        HttpAttachment att = (HttpAttachment) ch.getAttachment();
        List<byte[]> encode_bytes_array = getEncodeBytesArray(l);
        Object content = f.getContent();
        ByteBuf contentBuf = null;
        byte[] contentArray = null;
        byte[] head_bytes = f.getStatus().getLine();
        byte[] conn_bytes = f.getConnection().getLine();
        byte[] type_bytes = f.getContentType().getLine();
        byte[] date_bytes = f.getDate();
        boolean isArray = false;
        int write_size = 0;
        if (content instanceof ByteBuf) {
            contentBuf = ((ByteBuf) content).flip();
            write_size = contentBuf.limit();
        } else if (content instanceof byte[]) {
            isArray = true;
            contentArray = (byte[]) content;
            write_size = contentArray.length;
        }
        byte[] cl_len_bytes;
        int cl_len;
        if (write_size < 1024) {
            cl_len_bytes = CONTENT_LENGTHS[write_size];
            cl_len = cl_len_bytes.length;
        } else {
            cl_len_bytes = getContentLenBuf(l);
            int tmp_len = contentLenBuf.limit();
            int len_idx = Util.valueOf(write_size, cl_len_bytes);
            int num_len = cl_len_bytes.length - len_idx;
            System.arraycopy(cl_len_bytes, len_idx, cl_len_bytes, tmp_len, num_len);
            cl_len = tmp_len + num_len;
        }
        int hlen = head_bytes.length;
        int tlen = type_bytes == null ? 0 : type_bytes.length;
        int clen = conn_bytes == null ? 0 : conn_bytes.length;
        int dlen = date_bytes == null ? 0 : date_bytes.length;
        int len = hlen + cl_len + dlen + 2 + clen + tlen;
        int header_size = 0;
        IntMap<byte[]> headers = f.getResponseHeaders();
        if (headers != null) {
            for (headers.scan(); headers.hasNext();) {
                byte[] k = HttpHeader.get(headers.nextKey()).getBytes();
                byte[] v = headers.value();
                header_size++;
                encode_bytes_array.add(k);
                encode_bytes_array.add(v);
                len += 4;
                len += k.length;
                len += v.length;
            }
        }
        len += 2;
        if (isArray) {
            len += write_size;
        }
        ByteBuf buf;
        boolean offer = true;
        if (inline) {
            buf = att.getLastWriteBuf();
            if (buf.isReleased() || buf.capacity() - buf.limit() < len) {
                buf = ch.alloc().allocate(len);
                att.setLastWriteBuf(buf);
            } else {
                offer = false;
                buf.absPos(buf.absLimit());
                buf.limit(buf.capacity());
            }
        } else {
            if (Develop.BUF_DEBUG) {
                buf = ch.allocate();
            } else {
                buf = ch.alloc().allocate(len);
            }
        }
        buf.putBytes(head_bytes);
        buf.putBytes(cl_len_bytes, 0, cl_len);
        if (conn_bytes != null) {
            buf.putBytes(conn_bytes);
        }
        if (type_bytes != null) {
            buf.putBytes(type_bytes);
        }
        if (date_bytes != null) {
            buf.putBytes(date_bytes);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (header_size > 0) {
            putHeaders(buf, encode_bytes_array, header_size);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (write_size > 0) {
            if (isArray) {
                buf.putBytes(contentArray);
            } else {
                if (inline) {
                    att.setLastWriteBuf(ByteBuf.empty());
                }
                ch.write(buf.flip());
                ch.write(contentBuf);
                return null;
            }
        }
        buf.flip();
        return offer ? buf : null;
    }

    private void putHeaders(ByteBuf buf, List<byte[]> encode_bytes_array, int header_size) {
        int j = 0;
        for (int i = 0; i < header_size; i++) {
            buf.putBytes(encode_bytes_array.get(j++));
            buf.putByte((byte) ':');
            buf.putByte(SPACE);
            buf.putBytes(encode_bytes_array.get(j++));
            buf.putByte(R);
            buf.putByte(N);
        }
    }

    public int getBodyLimit() {
        return blimit;
    }

    public int getHeaderLimit() {
        return hlimit;
    }

    public int getHttpFrameStackSize() {
        return fcache;
    }

    @Override
    public String getProtocolId() {
        return "HTTP1.1";
    }

    @Override
    public int getHeaderLength() {
        return 0;
    }

    int onHeaderReadComplete(HttpFrame f) throws IOException {
        int contentLength = 0;
        String clength = f.getRequestHeader(Content_Length);
        String ctype = f.getRequestHeader(Content_Type);
        f.setForm(ctype == null ? false : ctype.startsWith("multipart/form-data;"));
        if (!Util.isNullOrBlank(clength)) {
            contentLength = Integer.parseInt(clength);
            f.setContentLength(contentLength);
        }
        if (contentLength < 1) {
            return decode_state_complate;
        } else {
            if (contentLength > blimit) {
                throw OVER_LIMIT;
            }
            return decode_state_body;
        }
    }

    static void parse_kv(Map<String, String> map, CharSequence line, int start, int end,
            char kvSplitor, char eSplitor) {
        int state_findKey = 0;
        int state_findValue = 1;
        int state = state_findKey;
        int count = end;
        int i = start;
        int ks = start;
        int vs = 0;
        CharSequence key = null;
        CharSequence value = null;
        for (; i != count;) {
            char c = line.charAt(i++);
            if (state == state_findKey) {
                if (c == kvSplitor) {
                    ks = Util.skip(line, ' ', ks);
                    key = line.subSequence(ks, i - 1);
                    state = state_findValue;
                    vs = i;
                    continue;
                }
            } else if (state == state_findValue) {
                if (c == eSplitor) {
                    vs = Util.skip(line, ' ', vs);
                    value = line.subSequence(vs, i - 1);
                    state = state_findKey;
                    ks = i;
                    map.put((String) key, (String) value);
                    continue;
                }
            }
        }
        if (state == state_findValue && end > vs) {
            map.put((String) key, (String) line.subSequence(vs, end));
        }
    }

    protected void parse_line_one(HttpFrame f, CharSequence line) {
        if (line.charAt(0) == 'G' && line.charAt(1) == 'E' && line.charAt(2) == 'T') {
            f.setMethod(HttpMethod.GET);
            parseRequestURL(f, 4, line);
        } else {
            f.setMethod(HttpMethod.POST);
            parseRequestURL(f, 5, line);
        }
    }

    protected void parseRequestURL(HttpFrame f, int skip, CharSequence line) {
        int index = Util.indexOf(line, '?');
        int lastSpace = Util.lastIndexOf(line, ' ');
        if (index > -1) {
            parse_kv(f.getRequestParams(), line, index + 1, lastSpace, '=', '&');
            f.setRequestURL((String) line.subSequence(skip, index));
        } else {
            f.setRequestURL((String) line.subSequence(skip, lastSpace));
        }
    }

    @Override
    public void release(NioEventLoop eventLoop, Frame frame) {
        eventLoop.release(FRAME_CACHE_KEY, frame);
    }

    @SuppressWarnings("unchecked")
    static List<byte[]> getEncodeBytesArray(FastThreadLocal l) {
        return (List<byte[]>) l.getList(encode_bytes_arrays_index);
    }

    protected static String parseBoundary(String contentType) {
        int index = KMP_BOUNDARY.match(contentType);
        if (index != -1) {
            return contentType.substring(index + 9);
        }
        return null;
    }

    private static int read_line(StringBuilder line, ByteBuf src, int abs_pos, int length,
            int limit) throws IOException {
        int maybeRead = limit - length;
        int s_limit = src.absLimit();
        int remaining = s_limit - abs_pos;
        if (remaining > maybeRead) {
            int count = abs_pos + maybeRead;
            for (int i = abs_pos; i < count; i++) {
                byte b = src.absByte(i);
                if (b == N) {
                    line.setLength(line.length() - 1);
                    return i + 1;
                } else {
                    line.append((char) (b & 0xff));
                }
            }
            throw OVER_LIMIT;
        } else {
            for (int i = abs_pos; i < s_limit; i++) {
                byte b = src.absByte(i);
                if (b == N) {
                    line.setLength(line.length() - 1);
                    return i + 1;
                } else {
                    line.append((char) (b & 0xff));
                }
            }
            return -1;
        }
    }

    @Override
    protected Object newAttachment() {
        return new HttpAttachment();
    }

    private static int read_line_range(ByteBuf src, int abs_pos, int length, int limit)
            throws IOException {
        int maybeRead = limit - length;
        int s_limit = src.absLimit();
        int remaining = s_limit - abs_pos;
        if (remaining > maybeRead) {
            int res_p = src.indexOf(N, abs_pos, maybeRead);
            if (res_p == -1) {
                throw OVER_LIMIT;
            }
            return res_p;
        } else {
            return src.indexOf(N, abs_pos, remaining);
        }
    }

    private static boolean start_with(ByteBuf src, int ps, int pe, byte[] match) {
        if (pe - ps < match.length) {
            return false;
        }
        for (int i = 0; i < match.length; i++) {
            if (src.absByte(ps + i) != match[i]) {
                return false;
            }
        }
        return true;
    }

}
