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
package com.generallycloud.baseio.container.http11.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.codec.http11.HttpHeaderDateFormat;
import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.codec.http11.future.HttpHeader;
import com.generallycloud.baseio.codec.http11.future.HttpStatus;
import com.generallycloud.baseio.codec.http11.future.ServerHttpFuture;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.http11.HtmlUtil;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceFilter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.NamedFuture;

//FIXME limit too large file
public class FutureAcceptorHttpFilter extends FutureAcceptorServiceFilter {

    private Logger                  logger     = LoggerFactory.getLogger(getClass());

    private Map<String, HttpEntity> html_cache = new HashMap<>();

    @Override
    protected void accept404(SocketSession session, NamedFuture future, String serviceName)
            throws IOException {

        HttpEntity entity = html_cache.get(serviceName);

        HttpStatus status = HttpStatus.C200;

        ServerHttpFuture f = (ServerHttpFuture) future;

        if (entity == null) {
            f.setStatus(HttpStatus.C404);
            entity = html_cache.get("/404.html");
            if (entity == null) {
                super.accept404(session, f, serviceName);
                return;
            }
        }

        File file = entity.getFile();

        if (file != null && file.lastModified() > entity.getLastModify()) {

            synchronized (entity) {

                reloadEntity(entity, session.getContext(), status);
            }

            flush(session, f, entity);

            return;
        }

        String ims = f.getRequestHeader(HttpHeader.IF_MODIFIED_SINCE);
        long imsTime = -1;
        if (!StringUtil.isNullOrBlank(ims)) {
            imsTime = HttpHeaderDateFormat.getFormat().parse(ims).getTime();
        }

        if (imsTime < entity.getLastModifyGTMTime()) {
            flush(session, f, entity);
            return;
        }

        f.setStatus(HttpStatus.C304);

        session.flush(f);
    }

    private void flush(SocketSession session, ServerHttpFuture future, HttpEntity entity) {
        future.setResponseHeader(HttpHeader.CONTENT_TYPE, entity.getContentType());
        future.setResponseHeader(HttpHeader.LAST_MODIFIED, entity.getLastModifyGTM());
        future.write(entity.getBinary());
        session.flush(future);
    }

    private void reloadEntity(HttpEntity entity, SocketChannelContext context, HttpStatus status)
            throws IOException {
        File file = entity.getFile();
        entity.setBinary(FileUtil.readBytesByFile(file));
        entity.setLastModify(file.lastModified());
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {

        String rootPath = context.getAppLocalAddress();

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
        mapping.put("ico", HttpFuture.CONTENT_TYPE_IMAGE_ICON);

        if (rootFile.exists()) {
            scanFolder(context.getChannelContext(), rootFile, rootPath, mapping, "");
        }

        super.initialize(context, config);
    }

    private void scanFolder(SocketChannelContext context, File file, String root,
            Map<String, String> mapping, String path) throws IOException {

        if (file.isFile()) {

            String contentType = getContentType(file.getName(), mapping);

            String fileName = file.getCanonicalPath().replace("\\", "/");

            HttpEntity entity = new HttpEntity();

            entity.setContentType(contentType);
            entity.setFile(file);

            html_cache.put(path, entity);

            LoggerUtil.prettyLog(logger, "mapping static :{}@{}", path, fileName);

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

            b.append("		<div style=\"margin-left:20px;\">\n");
            b.append("			Index of " + staticName + "\n");
            b.append("		</div>\n");
            b.append("		<hr>\n");

            if (!"/".equals(staticName)) {
                int index = staticName.lastIndexOf("/");
                String parentStaticName;
                if (index == 0) {
                    parentStaticName = "..";
                } else {
                    parentStaticName = staticName.substring(0, index);
                }
                b.append("		<p>\n");
                b.append("			<a href=\"" + parentStaticName + "\">&lt;dir&gt;..</a>\n");
                b.append("		</p>\n");
            }

            StringBuilder db = new StringBuilder();
            StringBuilder fb = new StringBuilder();

            for (File f : fs) {

                String staticName1 = path + "/" + f.getName();

                scanFolder(context, f, root, mapping, staticName1);

                if (f.isDirectory()) {
                    if ("/lib".equals(staticName1)) {
                        continue;
                    }
                    String a = "<a href=\"" + staticName1 + "\">&lt;dir&gt;" + f.getName()
                            + "</a>\n";
                    db.append("		<p>\n");
                    db.append("			" + a);
                    db.append("		</p>\n");
                } else {
                    String a = "<a href=\"" + staticName1 + "\">" + f.getName() + "</a>\n";
                    fb.append("		<p>\n");
                    fb.append("			" + a);
                    fb.append("		<p>\n");
                }
            }

            b.append(db);
            b.append(fb);

            b.append("		<hr>\n");
            b.append(HtmlUtil.HTML_BOTTOM);

            HttpEntity entity = new HttpEntity();

            entity.setContentType(HttpFuture.CONTENT_TYPE_TEXT_HTML);
            entity.setFile(file);
            entity.setLastModify(System.currentTimeMillis());
            entity.setBinary(b.toString().getBytes(context.getEncoding()));

            html_cache.put(staticName, entity);
        }

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

}
