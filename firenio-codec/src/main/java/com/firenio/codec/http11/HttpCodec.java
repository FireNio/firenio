/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.codec.http11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.firenio.Develop;
import com.firenio.buffer.ByteBuf;
import com.firenio.collection.ByteTree;
import com.firenio.collection.IntMap;
import com.firenio.common.ByteUtil;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.FastThreadLocal;
import com.firenio.component.Frame;
import com.firenio.component.NioEventLoop;
import com.firenio.component.ProtocolCodec;

/**
 * @author wangkai
 */
public class HttpCodec extends ProtocolCodec {

    static final byte[]      CONTENT_LENGTH_MATCH      = ByteUtil.b("Content-Length:");
    static final int         decode_state_body         = 2;
    static final int         decode_state_complete     = 3;
    static final int         decode_state_header       = 1;
    static final int         decode_state_line_one     = 0;
    static final int         encode_bytes_arrays_index = nextIndexedVariablesIndex();
    static final int         content_len_index         = nextIndexedVariablesIndex();
    static final String      FRAME_CACHE_KEY           = "_HTTP_FRAME_CACHE_KEY";
    static final byte        N                         = '\n';
    static final IOException OVER_LIMIT                = EXCEPTION("over limit");
    static final IOException ILLEGAL_METHOD            = EXCEPTION("illegal http method");
    static final byte        R                         = '\r';
    static final byte        SPACE                     = ' ';
    static final int         NUM_GET                   = ByteUtil.getInt("GET ".getBytes(), 0);
    static final int         NUM_POST                  = ByteUtil.getInt("POST".getBytes(), 0);

    private final int        blimit;
    private final byte[][]   cl_bytes = new byte[1024][];
    private final int        hlimit;
    private final int        fcache;
    private final boolean    lite;
    private final boolean    inline;
    private final ByteBuffer cl_buf;
    private final ByteTree   cached_urls;

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
        this(server, frameCache, 1024 * 8, 1024 * 256, lite, inline, null);
    }

    public HttpCodec(String server, int frameCache, boolean lite, boolean inline, ByteTree cachedUrls) {
        this(server, frameCache, 1024 * 8, 1024 * 256, lite, inline, cachedUrls);
    }

    public HttpCodec(String server, int fcache, int hlimit, int blimit, boolean lite, boolean inline, ByteTree cachedUrls) {
        this.lite = lite;
        this.inline = inline;
        this.hlimit = hlimit;
        this.blimit = blimit;
        this.fcache = fcache;
        this.cached_urls = cachedUrls;
        ByteBuffer temp = ByteBuffer.allocate(128);
        if (server == null) {
            temp.put(ByteUtil.b("\r\nContent-Length: "));
        } else {
            temp.put(ByteUtil.b("\r\nServer: " + server + "\r\nContent-Length: "));
        }
        cl_buf = temp.duplicate();
        cl_buf.flip();
        int p = temp.position();
        for (int i = 0; i < cl_bytes.length; i++) {
            temp.clear().position(p);
            temp.put(String.valueOf(i).getBytes());
            temp.flip();
            cl_bytes[i] = new byte[temp.limit()];
            temp.get(cl_bytes[i]);
        }
    }

    private static String parse_url(ByteBuf src, int url_start, int url_end) {
        StringBuilder line = FastThreadLocal.get().getStringBuilder();
        for (int i = url_start; i < url_end; i++) {
            line.append((char) (src.absByte(i) & 0xff));
        }
        return line.toString();
    }

    static void parse_kv(Map<String, String> map, CharSequence line, int start, int end, char kvSplitor, char eSplitor) {
        boolean      find_key = true;
        int          i        = start;
        int          ks       = start;
        int          vs       = 0;
        CharSequence key      = null;
        CharSequence value    = null;
        for (; i != end; ) {
            char c = line.charAt(i++);
            if (find_key) {
                if (c == kvSplitor) {
                    ks = Util.skip(line, ' ', ks);
                    key = line.subSequence(ks, i - 1);
                    find_key = false;
                    vs = i;
                }
            } else {
                if (c == eSplitor) {
                    vs = Util.skip(line, ' ', vs);
                    value = line.subSequence(vs, i - 1);
                    find_key = true;
                    ks = i;
                    map.put((String) key, (String) value);
                }
            }
        }
        if (!find_key && end > vs) {
            map.put((String) key, (String) line.subSequence(vs, end));
        }
    }

    protected static void parse_url(HttpFrame f, int skip, CharSequence line) {
        int index     = Util.indexOf(line, '?');
        int lastSpace = Util.lastIndexOf(line, ' ');
        if (index > -1) {
            parse_kv(f.getRequestParams(), line, index + 1, lastSpace, '=', '&');
            f.setRequestURL((String) line.subSequence(skip, index));
        } else {
            f.setRequestURL((String) line.subSequence(skip, lastSpace));
        }
    }

    @SuppressWarnings("unchecked")
    static List<byte[]> getEncodeBytesArray(FastThreadLocal l) {
        return (List<byte[]>) l.getList(encode_bytes_arrays_index);
    }

    private static int read_line(StringBuilder line, ByteBuf src, int abs_pos, int length, int limit) throws IOException {
        int maybeRead = limit - length;
        int s_limit   = src.absLimit();
        int remaining = s_limit - abs_pos;
        if (remaining > maybeRead) {
            int i = read_line(line, src, abs_pos, abs_pos + maybeRead);
            if (i == -1) {
                throw OVER_LIMIT;
            }
            return i;
        } else {
            return read_line(line, src, abs_pos, s_limit);
        }
    }

    private static int read_line(StringBuilder line, ByteBuf src, int abs_pos, int abs_limit) {
        for (int i = abs_pos; i < abs_limit; i++) {
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

    private static int read_line_range(ByteBuf src, int abs_pos, int length, int limit) throws IOException {
        int maybeRead = limit - length;
        int s_limit   = src.absLimit();
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

    HttpFrame new_frame() {
        return new HttpFrame();
    }

    private HttpFrame alloc_frame(NioEventLoop el) {
        if (fcache > 0) {
            Frame res = (Frame) el.getCache(FRAME_CACHE_KEY, fcache);
            if (res == null) {
                return new_frame();
            } else {
                return (HttpFrame) res.reset();
            }
        }
        return new_frame();
    }

    private int decode_lite(ByteBuf src, HttpFrame f) throws IOException {
        int decode_state = f.getDecodeState();
        int abs_pos      = src.absPos();
        int h_len        = f.getHeaderLength();
        if (decode_state == decode_state_line_one) {
            int l_end = src.indexOf(N);
            if (l_end == -1) {
                return decode_state_line_one;
            } else {
                h_len += (l_end - abs_pos);
                decode_state = decode_state_header;
                int url_start = abs_pos;
                int num       = src.getInt(abs_pos);
                if (num == NUM_GET) {
                    f.setMethod(HttpMethod.GET);
                    url_start += 4;
                } else if (num == NUM_POST) {
                    f.setMethod(HttpMethod.POST);
                    url_start += 5;
                } else {
                    throw ILLEGAL_METHOD;
                }
                int url_end = l_end - 10;
                int url_len = url_end - url_start;
                int qmark   = src.indexOf((byte) '?', url_start, url_len);
                if (qmark == -1) {
                    String url;
                    if (cached_urls != null) {
                        url = cached_urls.getString(src, url_start, url_len);
                        if (url == null) {
                            url = parse_url(src, url_start, url_end);
                        }
                    } else {
                        url = parse_url(src, url_start, url_end);
                    }
                    f.setRequestURL(url);
                } else {
                    StringBuilder line = FastThreadLocal.get().getStringBuilder();
                    for (int i = url_start; i < url_end; i++) {
                        line.append((char) (src.absByte(i) & 0xff));
                    }
                    int re_qmark = qmark - url_start;
                    parse_kv(f.getRequestParams(), line, re_qmark + 1, line.length(), '=', '&');
                    f.setRequestURL((String) line.subSequence(0, re_qmark));
                }
                abs_pos = l_end + 1;
            }
        }
        if (decode_state == decode_state_header) {
            for (; ; ) {
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
                        decode_state = decode_state_complete;
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
                            int cp  = ps + CONTENT_LENGTH_MATCH.length;
                            int cps = ByteUtil.skip(src, cp, pe, SPACE);
                            if (cps == -1) {
                                throw OVER_LIMIT;
                            }
                            int ct_len = 0;
                            for (int i = cps; i < pe; i++) {
                                ct_len = (src.absByte(i) - '0') + ct_len * 10;
                            }
                            f.setContentLength(ct_len);
                        }
                    }
                }
            }
        }
        return decode_state;
    }

    private int decode_full(ByteBuf src, HttpFrame f) throws IOException {
        StringBuilder line         = FastThreadLocal.get().getStringBuilder();
        int           decode_state = f.getDecodeState();
        int           h_len        = f.getHeaderLength();
        int           abs_pos      = src.absPos();
        if (decode_state == decode_state_line_one) {
            int l_end = read_line(line, src, abs_pos, 0, hlimit);
            if (l_end == -1) {
                return decode_state_line_one;
            } else {
                abs_pos = l_end;
                h_len += line.length();
                decode_state = decode_state_header;
                parse_line_one(f, line);
            }
        }
        if (decode_state == decode_state_header) {
            for (; ; ) {
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
                    decode_state = header_complete(f);
                    break;
                } else {
                    int p = Util.indexOf(line, ':');
                    if (p == -1) {
                        continue;
                    }
                    int    rp    = Util.skip(line, ' ', p + 1);
                    String name  = line.substring(0, p);
                    String value = line.substring(rp);
                    f.setReadHeader(name, value);
                }
            }
        }
        return decode_state;
    }

    @Override
    public Frame decode(Channel ch, ByteBuf src) throws Exception {
        boolean        remove = false;
        HttpAttachment att    = (HttpAttachment) ch.getAttachment();
        HttpFrame      f      = att.getUncompleteFrame();
        if (f == null) {
            f = alloc_frame(ch.getEventLoop());
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
            decode_state = decode_remain_body(ch, src, f);
        }
        if (decode_state == decode_state_complete) {
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

    int decode_remain_body(Channel ch, ByteBuf src, HttpFrame f) {
        int contentLength = f.getContentLength();
        int remain        = src.remaining();
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
            return decode_state_complete;
        }
    }

    private byte[] get_c_len_buf(FastThreadLocal l) {
        byte[] bb = (byte[]) l.getIndexedVariable(content_len_index);
        if (bb == null) {
            int limit = cl_buf.limit();
            bb = new byte[cl_buf.limit() + 16];
            cl_buf.get(bb, 0, limit);
            cl_buf.clear().limit(limit);
            l.setIndexedVariable(content_len_index, bb);
        }
        return bb;
    }

    @Override
    public ByteBuf encode(final Channel ch, Frame frame) {
        boolean         inline        = this.inline;
        HttpFrame       f             = (HttpFrame) frame;
        FastThreadLocal l             = FastThreadLocal.get();
        HttpAttachment  att           = (HttpAttachment) ch.getAttachment();
        List<byte[]>    bytes_array   = getEncodeBytesArray(l);
        Object          content       = f.getContent();
        ByteBuf         content_buf   = null;
        byte[]          content_array = null;
        byte[]          head_bytes    = f.getStatus().getLine();
        byte[]          conn_bytes    = f.getConnection().getLine();
        byte[]          type_bytes    = f.getContentType().getLine();
        byte[]          date_bytes    = f.getDate();
        boolean         is_array      = false;
        int             write_size    = 0;
        if (content instanceof ByteBuf) {
            content_buf = ((ByteBuf) content).flip();
            write_size = content_buf.limit();
        } else if (content instanceof byte[]) {
            is_array = true;
            content_array = (byte[]) content;
            write_size = content_array.length;
        }
        byte[] cl_len_bytes;
        int    cl_len;
        if (write_size < 1024) {
            cl_len_bytes = cl_bytes[write_size];
            cl_len = cl_len_bytes.length;
        } else {
            cl_len_bytes = get_c_len_buf(l);
            int tmp_len = cl_buf.limit();
            int len_idx = Util.valueOf(write_size, cl_len_bytes);
            int num_len = cl_len_bytes.length - len_idx;
            System.arraycopy(cl_len_bytes, len_idx, cl_len_bytes, tmp_len, num_len);
            cl_len = tmp_len + num_len;
        }
        int            hlen    = head_bytes.length;
        int            tlen    = type_bytes == null ? 0 : type_bytes.length;
        int            clen    = conn_bytes == null ? 0 : conn_bytes.length;
        int            dlen    = date_bytes == null ? 0 : date_bytes.length;
        int            len     = hlen + cl_len + dlen + 2 + clen + tlen;
        int            h_size  = 0;
        IntMap<byte[]> headers = f.getResponseHeaders();
        if (headers != null) {
            for (headers.scan(); headers.hasNext(); ) {
                byte[] k = HttpHeader.get(headers.nextKey()).getBytes();
                byte[] v = headers.value();
                h_size++;
                bytes_array.add(k);
                bytes_array.add(v);
                len += 4;
                len += k.length;
                len += v.length;
            }
        }
        len += 2;
        if (is_array) {
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
        if (h_size > 0) {
            put_headers(buf, bytes_array, h_size);
        }
        buf.putByte(R);
        buf.putByte(N);
        if (write_size > 0) {
            if (is_array) {
                buf.putBytes(content_array);
            } else {
                if (inline) {
                    att.setLastWriteBuf(ByteBuf.empty());
                }
                ch.write(buf.flip());
                ch.write(content_buf);
                return null;
            }
        }
        buf.flip();
        return offer ? buf : null;
    }

    private void put_headers(ByteBuf buf, List<byte[]> encode_bytes_array, int header_size) {
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

    int header_complete(HttpFrame f) throws IOException {
        int    contentLength = 0;
        String c_length      = f.getRequestHeader(HttpHeader.Content_Length);
        String c_type        = f.getRequestHeader(HttpHeader.Content_Type);
        f.setForm(c_type != null && c_type.startsWith("multipart/form-data;"));
        if (!Util.isNullOrBlank(c_length)) {
            contentLength = Integer.parseInt(c_length);
            f.setContentLength(contentLength);
        }
        if (contentLength < 1) {
            return decode_state_complete;
        } else {
            if (contentLength > blimit) {
                throw OVER_LIMIT;
            }
            return decode_state_body;
        }
    }

    protected void parse_line_one(HttpFrame f, CharSequence line) throws IOException {
        if (line.charAt(0) == 'G' && line.charAt(1) == 'E' && line.charAt(2) == 'T') {
            f.setMethod(HttpMethod.GET);
            parse_url(f, 4, line);
        } else if (line.charAt(0) == 'P' && line.charAt(1) == 'O' && line.charAt(2) == 'S' && line.charAt(3) == 'T') {
            f.setMethod(HttpMethod.POST);
            parse_url(f, 5, line);
        } else {
            throw ILLEGAL_METHOD;
        }
    }

    @Override
    public void release(NioEventLoop eventLoop, Frame frame) {
        eventLoop.release(FRAME_CACHE_KEY, frame);
    }

    @Override
    protected Object newAttachment() {
        return new HttpAttachment();
    }

}
