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
package com.generallycloud.baseio.container.http11;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.codec.http11.HttpHeader;
import com.generallycloud.baseio.codec.http11.HttpStatic;
import com.generallycloud.baseio.codec.http11.HttpStatus;
import com.generallycloud.baseio.codec.http11.ServerHttpFuture;
import com.generallycloud.baseio.codec.http11.WebSocketFuture;
import com.generallycloud.baseio.common.DateUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.ContainerIoEventHandle;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.NamedFuture;

//FIXME limit too large file
public class HttpFutureAcceptor extends ContainerIoEventHandle {

    private Map<String, HttpEntity> htmlCache = new HashMap<>();
    private HttpSessionManager      httpSessionManager;
    private Logger                  logger    = LoggerFactory.getLogger(getClass());

    @Override
    public void accept(NioSocketChannel channel, Future future) throws Exception {
        acceptHtml(channel, (NamedFuture) future);
    }
    
    protected void printHtml(NioSocketChannel channel, Future future, String content) {
        if (channel.isClosed()) {
            return;
        }
        if (future instanceof WebSocketFuture) {
            future.write(content, channel);
            channel.flush(future);
            return;
        }
        ServerHttpFuture f = new ServerHttpFuture(channel.getContext());
        StringBuilder builder = new StringBuilder(HtmlUtil.HTML_HEADER);
        builder.append("        <div style=\"margin-left:20px;\">\n");
        builder.append("            ");
        builder.append(content);
        builder.append("            </div>\n");
        builder.append("        </div>\n");
        builder.append(HtmlUtil.HTML_POWER_BY);
        builder.append(HtmlUtil.HTML_BOTTOM);
        f.write(builder.toString(), channel.getCharset());
        f.setStatus(HttpStatus.C500);
        f.setResponseHeader(HttpHeader.Content_Type_Bytes, HttpStatic.html_utf8_bytes);
        channel.flush(f);
    }

    protected void acceptHtml(NioSocketChannel channel, NamedFuture future) throws IOException {
        HttpEntity entity = htmlCache.get(future.getFutureName());
        HttpStatus status = HttpStatus.C200;
        ServerHttpFuture f = (ServerHttpFuture) future;
        if (entity == null) {
            f.setStatus(HttpStatus.C404);
            entity = htmlCache.get("/404.html");
            if (entity == null) {
                printHtml(channel, future, "404 page not found");
                return;
            }
        }
        File file = entity.getFile();
        if (file != null && file.lastModified() > entity.getLastModify()) {
            synchronized (entity) {
                reloadEntity(entity, channel.getContext(), status);
            }
            flush(channel, f, entity);
            return;
        }
        String ims = f.getRequestHeader(HttpHeader.Low_If_Modified_Since);
        long imsTime = -1;
        if (!StringUtil.isNullOrBlank(ims)) {
            imsTime = DateUtil.get().parseHttp(ims).getTime();
        }
        if (imsTime < entity.getLastModifyGTMTime()) {
            flush(channel, f, entity);
            return;
        }
        f.setStatus(HttpStatus.C304);
        channel.flush(f);
    }

    @Override
    protected void destroy(ChannelContext context, boolean redeploy) {
        LifeCycleUtil.stop(httpSessionManager);
        super.destroy(context, redeploy);
    }

    @Override
    public void exceptionCaught(NioSocketChannel channel, Future future, Exception ex) {
        logger.error(ex.getMessage(), ex);
        StringBuilder builder = new StringBuilder();
        builder.append(
                "            <div>oops, server threw an inner exception, the stack trace is :</div>\n");
        builder.append("            <div style=\"font-family:serif;color:#5c5c5c;\">\n");
        builder.append(
                "            -------------------------------------------------------</BR>\n");
        builder.append("            ");
        builder.append(ex.toString());
        builder.append("</BR>\n");
        StackTraceElement[] es = ex.getStackTrace();
        for (StackTraceElement e : es) {
            builder.append("                &emsp;at ");
            builder.append(e.toString());
            builder.append("</BR>\n");
        }
        printHtml(channel, future, builder.toString());
    }

    private void flush(NioSocketChannel channel, ServerHttpFuture future, HttpEntity entity) {
        future.setResponseHeader(HttpHeader.Content_Type_Bytes, entity.getContentTypeBytes());
        future.setResponseHeader(HttpHeader.Last_Modified_Bytes, entity.getLastModifyGTMBytes());
        future.write(entity.getBinary());
        channel.flush(future);
    }

    private ApplicationIoEventHandle getApplicationIoEventHandle(ChannelContext context) {
        return (ApplicationIoEventHandle) context.getIoEventHandle();
    }

    private String getContentType(String fileName, Map<String, String> mapping) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return HttpFuture.CONTENT_TYPE_TEXT_PLAIN;
        }
        String subfix = fileName.substring(index + 1);
        String contentType = mapping.get(subfix);
        if (contentType == null) {
            contentType = HttpFuture.CONTENT_TYPE_TEXT_PLAIN;
        }
        return contentType;
    }

    protected Map<String, HttpEntity> getHtmlCache() {
        return htmlCache;
    }

    public HttpSessionManager getHttpSessionManager() {
        return httpSessionManager;
    }

    @Override
    protected void initialize(ChannelContext context, boolean redeploy) throws Exception {
        initializeHtml(context);
        initializeSessionManager(context);
        super.initialize(context, redeploy);
    }

    private void initializeHtml(ChannelContext context) throws Exception {
        ApplicationIoEventHandle handle = getApplicationIoEventHandle(context);
        String rootPath = handle.getAppLocalAddress();
        File rootFile = new File(rootPath);
        Map<String, String> mapping = new HashMap<>();

        mapping.put("htm", HttpFuture.CONTENT_TYPE_TEXT_HTML);
        mapping.put("html", HttpFuture.CONTENT_TYPE_TEXT_HTML);
        mapping.put("js", HttpFuture.CONTENT_APPLICATION_JAVASCRIPT);
        mapping.put("css", HttpFuture.CONTENT_TYPE_TEXT_CSS);
        mapping.put("png", HttpFuture.CONTENT_TYPE_IMAGE_PNG);
        mapping.put("jpg", HttpFuture.CONTENT_TYPE_IMAGE_JPEG);
        mapping.put("jpeg", HttpFuture.CONTENT_TYPE_IMAGE_JPEG);
        mapping.put("gif", HttpFuture.CONTENT_TYPE_IMAGE_GIF);
        mapping.put("txt", HttpFuture.CONTENT_TYPE_TEXT_PLAIN);
        mapping.put("ico", HttpFuture.CONTENT_TYPE_IMAGE_PNG);

        if (rootFile.exists()) {
            scanFolder(context, htmlCache, rootFile, rootPath, mapping, "");
        }
    }

    private void initializeSessionManager(ChannelContext context) throws Exception {
        ApplicationIoEventHandle handle = getApplicationIoEventHandle(context);
        if (handle.getConfiguration().isEnableHttpSession()) {
            httpSessionManager = new DefaultHttpSessionManager();
            httpSessionManager.startup("http-channel-manager");
        } else {
            httpSessionManager = new FakeHttpSessionManager();
        }
    }

    private void reloadEntity(HttpEntity entity, ChannelContext context, HttpStatus status)
            throws IOException {
        File file = entity.getFile();
        entity.setBinary(FileUtil.readBytesByFile(file));
        entity.setLastModify(file.lastModified());
    }

    private void scanFolder(ChannelContext context, Map<String, HttpEntity> htmlCache, File file,
            String root, Map<String, String> mapping, String path) throws IOException {
        if (file.isFile()) {
            String contentType = getContentType(file.getName(), mapping);
            HttpEntity entity = new HttpEntity();
            entity.setContentType(contentType);
            entity.setFile(file);
            htmlCache.put(path, entity);
            LoggerUtil.prettyLog(logger, "mapping static url:{}", path);
        } else if (file.isDirectory()) {
            String staticName = path;
            if ("/lib".equals(staticName)) {
                return;
            }
            if ("".equals(staticName)) {
                staticName = "/";
            }
            File[] fs = file.listFiles();
            StringBuilder b = new StringBuilder(HtmlUtil.HTML_HEADER);
            b.append("      <div style=\"margin-left:20px;\">\n");
            b.append("          Index of " + staticName + "\n");
            b.append("      </div>\n");
            b.append("      <hr>\n");
            if (!"/".equals(staticName)) {
                int index = staticName.lastIndexOf("/");
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
                String staticName1 = path + "/" + f.getName();
                scanFolder(context, htmlCache, f, root, mapping, staticName1);
                if (f.isDirectory()) {
                    if ("/lib".equals(staticName1)) {
                        continue;
                    }
                    String a = "<a href=\"" + staticName1 + "\">&lt;dir&gt;" + f.getName()
                            + "</a>\n";
                    db.append("     <p>\n");
                    db.append("         " + a);
                    db.append("     </p>\n");
                } else {
                    String a = "<a href=\"" + staticName1 + "\">" + f.getName() + "</a>\n";
                    fb.append("     <p>\n");
                    fb.append("         " + a);
                    fb.append("     <p>\n");
                }
            }
            b.append(db);
            b.append(fb);
            b.append("      <hr>\n");
            b.append(HtmlUtil.HTML_BOTTOM);
            HttpEntity entity = new HttpEntity();
            entity.setContentType(HttpFuture.CONTENT_TYPE_TEXT_HTML);
            entity.setFile(file);
            entity.setLastModify(System.currentTimeMillis());
            entity.setBinary(b.toString().getBytes(context.getCharset()));
            htmlCache.put(staticName, entity);
        }
    }

}
