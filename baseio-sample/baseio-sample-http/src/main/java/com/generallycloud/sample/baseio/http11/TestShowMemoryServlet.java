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
package com.generallycloud.sample.baseio.http11;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.generallycloud.baseio.buffer.ByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.baseio.codec.http11.HttpFuture;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.container.http11.HtmlUtil;
import com.generallycloud.baseio.container.http11.HttpFutureAcceptor;
import com.generallycloud.baseio.container.http11.HttpFutureAcceptorService;
import com.generallycloud.baseio.container.http11.HttpSession;

@Service("/test-show-memory")
public class TestShowMemoryServlet extends HttpFutureAcceptorService {

    @Override
    protected void doAccept(HttpSession session, HttpFuture future) throws Exception {

        TestWebSocketChatServlet chatServlet = ContextUtil.getBean(TestWebSocketChatServlet.class);
        TestWebSocketRumpetrollServlet rumpetrollServlet = ContextUtil
                .getBean(TestWebSocketRumpetrollServlet.class);

        WebSocketMsgAdapter chatMsgAdapter = chatServlet.getMsgAdapter();
        WebSocketMsgAdapter rumpetrollMsgAdapter = rumpetrollServlet.getMsgAdapter();

        SocketChannelContext context = session.getIoSession().getContext();
        HttpFutureAcceptor httpContext = session.getContext();

        BigDecimal time = new BigDecimal(System.currentTimeMillis() - context.getStartupTime());
        BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
        BigDecimal hour = time.divide(anHour, 3, RoundingMode.HALF_UP);

        ByteBufAllocatorManager allocator = context.getByteBufAllocatorManager();

        String allocatorDes = "unpooled";

        if (allocator instanceof PooledByteBufAllocatorManager) {
            allocatorDes = ((PooledByteBufAllocatorManager) allocator).toDebugString();
        }

        Configuration configuration = context.getConfiguration();

        int SERVER_CORE_SIZE = configuration.getCoreSize();
        int SERVER_MEMORY_POOL_CAPACITY = configuration.getMemoryPoolCapacity()
                * SERVER_CORE_SIZE;
        int SERVER_MEMORY_POOL_UNIT = configuration.getMemoryPoolUnit();

        double MEMORY_POOL_SIZE = new BigDecimal(
                SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
                        .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();

        int M = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();
        StringBuilder builder = new StringBuilder(HtmlUtil.HTML_HEADER);

        builder.append("		<div style=\"margin-left:20px;\">\n");
        builder.append("服务器内存使用情况：</BR>\n");
        builder.append("虚拟机占用内存：");
        builder.append(runtime.totalMemory() / M);
        builder.append("M;\n</BR>已占用内存：");
        builder.append((runtime.totalMemory() - runtime.freeMemory()) / M);
        builder.append("M;\n</BR>空闲内存：");
        builder.append(runtime.freeMemory() / M);
        builder.append("M;\n</BR>内存池大小：");
        builder.append(MEMORY_POOL_SIZE);
        builder.append("M;\n</BR>内存池状态（Heap）：");
        builder.append(allocatorDes);
        builder.append("\n</BR>聊天室（WebSocket）客户端数量：");
        builder.append(chatMsgAdapter.getClientSize());
        builder.append("\n</BR>小蝌蚪（WebSocket）客户端数量：");
        builder.append(rumpetrollMsgAdapter.getClientSize());
        builder.append("\n</BR>服务器当前连接数（io-session）：");
        builder.append(context.getSessionManager().getManagedSessionSize());
        builder.append(";\n</BR>服务器当前会话数（http-session）：");
        builder.append(httpContext.getHttpSessionManager().getManagedSessionSize());
        builder.append(";\n</BR>服务运行时间：");
        builder.append(hour + "H;");
        builder.append("		</div>\n");

        builder.append(HtmlUtil.HTML_POWER_BY);
        builder.append(HtmlUtil.HTML_BOTTOM);

        future.write(builder.toString(),session.getEncoding());

        future.setResponseHeader("Content-Type", HttpFuture.CONTENT_TYPE_TEXT_HTML);

        session.flush(future);
    }

}
