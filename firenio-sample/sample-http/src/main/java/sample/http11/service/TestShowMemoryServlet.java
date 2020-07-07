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
package sample.http11.service;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.firenio.buffer.ByteBufAllocatorGroup;
import com.firenio.codec.http11.HttpContentType;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.collection.DelayedQueue.DelayTask;
import com.firenio.common.DateUtil;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelContext;
import com.firenio.component.NioEventLoopGroup;

import sample.http11.HttpFrameAcceptor;
import sample.http11.HttpUtil;

@Service("/test-show-memory")
public class TestShowMemoryServlet extends HttpFrameAcceptor {

    @Autowired
    ApplicationContext context;

    @Override
    protected void doAccept(Channel ch, HttpFrame f) throws Exception {
        TestWebSocketChatServlet       chatServlet       = context.getBean(TestWebSocketChatServlet.class);
        TestWebSocketRumpetrollServlet rumpetrollServlet = context.getBean(TestWebSocketRumpetrollServlet.class);

        WebSocketMsgAdapter chatMsgAdapter       = chatServlet.getMsgAdapter();
        WebSocketMsgAdapter rumpetrollMsgAdapter = rumpetrollServlet.getMsgAdapter();

        ChannelContext context = ch.getContext();

        String kill = f.getRequestParam("kill");
        if (!Util.isNullOrBlank(kill)) {
            Integer id       = Integer.valueOf(kill, 16);
            Channel close_ch = CountChannelListener.chs.get(id);
            if (close_ch != null) {
                close_ch.getEventLoop().schedule(new DelayTask(10) {
                    @Override
                    public void run() {
                        Util.close(close_ch);
                    }
                });
            }
        }

        BigDecimal time   = new BigDecimal(Util.now_f() - context.getStartupTime());
        BigDecimal anHour = new BigDecimal(60 * 60 * 1000);
        BigDecimal hour   = time.divide(anHour, 3, RoundingMode.HALF_UP);

        NioEventLoopGroup group = ch.getEventLoop().getGroup();

        ByteBufAllocatorGroup allocator = group.getAllocatorGroup();

        String allocatorDes = "unpooled";

        if (allocator != null) {
            StringBuilder builder = new StringBuilder();
            String[]      res     = allocator.toDebugString();
            for (int i = 0; i < res.length; i++) {
                builder.append("<BR/>\n");
                builder.append(res[i]);
            }
            allocatorDes = builder.toString();
        }

        int  M                           = 1024 * 1024;
        int  eventLoopSize               = group.getEventLoopSize();
        int  SERVER_MEMORY_POOL_UNIT     = group.getMemoryUnit();
        long SERVER_MEMORY_POOL_CAPACITY = group.getMemoryCapacity();

        double MEMORY_POOL_SIZE = SERVER_MEMORY_POOL_CAPACITY / (M * 1d);
        MEMORY_POOL_SIZE = new BigDecimal(MEMORY_POOL_SIZE).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        Runtime       runtime = Runtime.getRuntime();
        StringBuilder builder = new StringBuilder(HttpUtil.HTML_HEADER);

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
        builder.append("M;\n</BR>内存池状态：");
        builder.append(allocatorDes);
        builder.append("\n</BR>聊天室（WebSocket）客户端数量：");
        builder.append(chatMsgAdapter.getClientSize());
        builder.append("\n</BR>小蝌蚪（WebSocket）客户端数量：");
        builder.append(rumpetrollMsgAdapter.getClientSize());
        builder.append("\n</BR>服务器当前连接数（io-channel）：");
        builder.append(CountChannelListener.chs.size());
        for (Channel s : CountChannelListener.chs.values()) {
            builder.append("\n</BR>");
            builder.append(s);
            builder.append(", opened: ");
            builder.append(DateUtil.get().formatYyyy_MM_dd_HH_mm_ss(new Date(s.getCreationTime())));
        }
        builder.append(";\n</BR>服务运行时间：");
        builder.append(hour + "H;");
        builder.append("		</div>\n");

        builder.append(HttpUtil.HTML_POWER_BY);
        builder.append(HttpUtil.HTML_BOTTOM);

        f.setString(builder.toString(), ch);
        f.setContentType(HttpContentType.text_html_utf8);

        ch.writeAndFlush(f);
    }

}
