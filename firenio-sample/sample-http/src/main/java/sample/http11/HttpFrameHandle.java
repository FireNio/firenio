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
package sample.http11;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.firenio.buffer.ByteBuf;
import com.firenio.codec.http11.HttpContentType;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.HttpHeader;
import com.firenio.codec.http11.HttpStatic;
import com.firenio.codec.http11.HttpStatus;
import com.firenio.codec.http11.WebSocketCodec;
import com.firenio.codec.http11.WebSocketFrame;
import com.firenio.common.DateUtil;
import com.firenio.common.FileUtil;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelContext;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

//FIXME writeIndex too large file
public class HttpFrameHandle extends IoEventHandle {

    private Charset                 charset        = Util.UTF8;
    private String                  welcome        = "/";
    private Map<String, HttpEntity> htmlCache      = new HashMap<>();
    private Logger                  logger         = LoggerFactory.getLogger(getClass());
    private ScanFileFilter          scanFileFilter = new IgnoreDotStartFile();

    @Override
    public void accept(Channel ch, Frame frame) throws Exception {
        acceptHtml(ch, frame);
    }

    protected void acceptHtml(Channel ch, Frame frame) throws Exception {
        String     frameName = HttpUtil.getFrameName(ch, frame);
        HttpEntity entity    = null;
        if (frameName.equals("/")) {
            entity = htmlCache.get(welcome);
        }
        if (entity == null) {
            entity = htmlCache.get(frameName);
        }
        HttpStatus status = HttpStatus.C200;
        HttpFrame  f      = (HttpFrame) frame;
        if (entity == null) {
            entity = htmlCache.get("/404.html");
            if (entity == null) {
                printHtml(ch, frame, HttpStatus.C404, "404 page not found");
                return;
            }
        }
        File file = entity.getFile();
        if (file != null && file.lastModified() > entity.getLastModify()) {
            synchronized (entity) {
                if (file.lastModified() > entity.getLastModify()) {
                    reloadEntity(entity, ch.getContext(), status);
                }
            }
            writeAndFlush(ch, f, entity);
            return;
        }
        String ims     = f.getRequestHeader(HttpHeader.If_Modified_Since);
        long   imsTime = -1;
        if (!Util.isNullOrBlank(ims)) {
            imsTime = DateUtil.get().parseHttp(ims).getTime();
        }
        if (imsTime < entity.getLastModifyGTMTime()) {
            writeAndFlush(ch, f, entity);
            return;
        }
        f.setStatus(HttpStatus.C304);
        ch.writeAndFlush(f);
    }

    public void destroy(ChannelContext context) {
    }

    @Override
    public void exceptionCaught(Channel ch, Frame frame, Throwable ex) {
        logger.error(ex.getMessage(), ex);
        frame.release();
        if (!ch.isOpen()) {
            return;
        }
        if (ch.getCodec() instanceof WebSocketCodec) {
            WebSocketFrame f = new WebSocketFrame();
            f.setString((ex.getClass() + ex.getMessage()), ch);
            try {
                ch.writeAndFlush(f);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("            <div>oops, server threw an inner exception, the stack trace is :</div>\n");
            builder.append("            <div style=\"font-family:serif;color:#5c5c5c;\">\n");
            builder.append("            -------------------------------------------------------</BR>\n");
            builder.append("            ");
            builder.append(ex.toString());
            builder.append("</BR>\n");
            StackTraceElement[] es = ex.getStackTrace();
            for (StackTraceElement e : es) {
                builder.append("                &emsp;at ");
                builder.append(e.toString());
                builder.append("</BR>\n");
            }
            try {
                printHtml(ch, frame, HttpStatus.C500, builder.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Charset getCharset() {
        return charset;
    }

    private HttpContentType getContentType(String fileName, Map<String, HttpContentType> mapping) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return HttpContentType.text_plain_utf8;
        }
        String          sub_fix     = fileName.substring(index + 1);
        HttpContentType contentType = mapping.get(sub_fix);
        if (contentType == null) {
            contentType = HttpContentType.text_plain_utf8;
        }
        return contentType;
    }

    protected Map<String, HttpEntity> getHtmlCache() {
        return htmlCache;
    }

    public ScanFileFilter getScanFileFilter() {
        return scanFileFilter;
    }

    public void initialize(ChannelContext context, String rootPath, boolean prodMode) throws Exception {
        String welcome  = context.getProperties().getProperty("app.welcome");
        String userPath = context.getProperties().getProperty("app.webRoot");
        String path;
        if (prodMode) {
            path = Util.isNullOrBlank(userPath) ? rootPath + "/app/html" : userPath;
        } else {
            path = rootPath + "/target/classes/app/html";
        }
        File rootFile = new File(path);
        if (!Util.isNullOrBlank(welcome)) {
            this.welcome = welcome;
        }
        logger.info("html path: {}", rootFile.getAbsolutePath());
        Map<String, HttpContentType> mapping = new HashMap<>();
        mapping.put("htm", HttpContentType.text_html_utf8);
        mapping.put("html", HttpContentType.text_html_utf8);
        mapping.put("js", HttpContentType.application_js_utf8);
        mapping.put("css", HttpContentType.text_css_utf8);
        mapping.put("png", HttpContentType.image_png);
        mapping.put("jpg", HttpContentType.image_jpeg);
        mapping.put("jpeg", HttpContentType.image_jpeg);
        mapping.put("gif", HttpContentType.image_gif);
        mapping.put("txt", HttpContentType.text_plain_utf8);
        mapping.put("ico", HttpContentType.image_png);
        if (rootFile.exists()) {
            scanFolder(scanFileFilter, rootFile, mapping, "");
        }
    }

    protected void printHtml(Channel ch, Frame frame, HttpStatus status, String content) throws Exception {
        HttpFrame     f       = (HttpFrame) frame;
        StringBuilder builder = new StringBuilder(HttpUtil.HTML_HEADER);
        builder.append("        <div style=\"margin-left:20px;\">\n");
        builder.append("            ");
        builder.append(content);
        builder.append("            </div>\n");
        builder.append("        </div>\n");
        builder.append(HttpUtil.HTML_POWER_BY);
        builder.append(HttpUtil.HTML_BOTTOM);
        byte[] data = builder.toString().getBytes(ch.getCharset());
        f.setContent(data);
        f.setStatus(status);
        f.setContentType(HttpContentType.text_html_utf8);
        ch.writeAndFlush(f);
    }

    private void reloadEntity(HttpEntity entity, ChannelContext context, HttpStatus status) throws IOException {
        File file = entity.getFile();
        entity.setBinary(FileUtil.readBytesByFile(file));
        entity.setLastModify(file.lastModified());
    }

    private void scanFolder(ScanFileFilter filter, File file, Map<String, HttpContentType> mapping, String path) {
        if (filter.filter(file)) {
            return;
        }
        if (file.isFile()) {
            HttpContentType contentType = getContentType(file.getName(), mapping);
            HttpEntity      entity      = new HttpEntity();
            entity.setContentType(contentType);
            entity.setFile(file);
            htmlCache.put(path, entity);
            logger.info("mapping static url:{}", path);
        } else if (file.isDirectory()) {
            String staticName = path;
            if ("".equals(staticName)) {
                staticName = "/";
            }
            File[] fs = file.listFiles();
            if (fs == null) {
                return;
            }
            StringBuilder b = new StringBuilder(HttpUtil.HTML_HEADER);
            b.append("      <div style=\"margin-left:20px;\">\n");
            b.append("          Index of " + staticName + "\n");
            b.append("      </div>\n");
            b.append("      <hr>\n");
            if (!"/".equals(staticName)) {
                int    index = staticName.lastIndexOf("/");
                String parentStaticName;
                if (index == 0) {
                    parentStaticName = "..";
                } else {
                    parentStaticName = staticName.substring(0, index);
                }
                b.append("      <p>\n");
                b.append("          <a href=\"" + parentStaticName + "\">&lt;dir&gt;..</a>\n");
                b.append("      </p>\n");
            }
            StringBuilder db = new StringBuilder();
            StringBuilder fb = new StringBuilder();
            for (File f : fs) {
                if (filter.filter(f)) {
                    continue;
                }
                String url = path + "/" + f.getName();
                scanFolder(filter, f, mapping, url);
                if (f.isDirectory()) {
                    String a = buildLabelA(url, "&lt;dir&gt;" + f.getName());
                    db.append("     <p>\n");
                    db.append("         ");
                    db.append(a);
                    db.append("     </p>\n");
                } else {
                    String a = buildLabelA(url, f.getName());
                    fb.append("     <p>\n");
                    fb.append("         ");
                    fb.append(a);
                    fb.append("     <p>\n");
                }
            }
            b.append(db);
            b.append(fb);
            b.append("      <hr>\n");
            b.append(HttpUtil.HTML_BOTTOM);
            HttpEntity entity = new HttpEntity();
            entity.setContentType(HttpContentType.text_html_utf8);
            entity.setFile(file);
            entity.setLastModify(Util.now_f());
            entity.setBinary(b.toString().getBytes(charset));
            htmlCache.put(staticName, entity);
        }
    }

    private static String buildLabelA(String url, String content) {
        return "<a href=\"" + url + "\">" + content + "</a>\n";
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setDefaultResponseHeaders(HttpFrame f) {
        if (getCharset() == Util.GBK) {
            f.setContentType(HttpContentType.text_plain_gbk);
        } else {
            f.setContentType(HttpContentType.text_plain_utf8);
        }
        f.setResponseHeader(HttpHeader.Server, HttpStatic.server_firenio_bytes);
    }

    public void setScanFileFilter(ScanFileFilter scanFileFilter) {
        if (scanFileFilter != null) {
            this.scanFileFilter = scanFileFilter;
        }
    }

    private void writeAndFlush(Channel ch, HttpFrame frame, HttpEntity entity) throws Exception {
        frame.setContentType(entity.getContentType());
        frame.setResponseHeader(HttpHeader.Last_Modified, entity.getLastModifyGTMBytes());
        frame.setContent(entity.content.duplicate());
        ch.writeAndFlush(frame);
    }

    public interface ScanFileFilter {
        boolean filter(File file);
    }

    static class HttpEntity {

        private ByteBuf         content;
        private HttpContentType contentType;
        private File            file;
        private long            lastModify;
        private String          lastModifyGTM;
        private byte[]          lastModifyGTMBytes;
        private long            lastModifyGTMTime;

        ByteBuf getContent() {
            return content;
        }

        HttpContentType getContentType() {
            return contentType;
        }

        File getFile() {
            return file;
        }

        long getLastModify() {
            return lastModify;
        }

        String getLastModifyGTM() {
            return lastModifyGTM;
        }

        byte[] getLastModifyGTMBytes() {
            return lastModifyGTMBytes;
        }

        long getLastModifyGTMTime() {
            return lastModifyGTMTime;
        }

        void setBinary(byte[] readBytesByFile) {
            this.content = ByteBuf.wrapAuto(readBytesByFile);
        }

        void setContentType(HttpContentType contentType) {
            this.contentType = contentType;
        }

        void setFile(File file) {
            this.file = file;
        }

        void setLastModify(long lastModify) {
            DateUtil format = DateUtil.get();
            this.lastModify = lastModify;
            this.lastModifyGTMBytes = format.formatHttpBytes(lastModify);
            this.lastModifyGTM = new String(lastModifyGTMBytes);
            this.lastModifyGTMTime = format.parseHttp(lastModifyGTM).getTime();
        }

    }

    class IgnoreDotStartFile implements ScanFileFilter {

        @Override
        public boolean filter(File file) {
            return file.getName().startsWith(".");
        }

    }

}
