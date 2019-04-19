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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.firenio.buffer.ByteBuf;
import com.firenio.codec.http11.HttpContentType;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.common.Util;
import com.firenio.component.Channel;

import sample.http11.EmojiUtil;
import sample.http11.HttpFrameAcceptor;
import sample.http11.HttpUtil;

@Service("/test-emoji")
public class TestEmojiServlet extends HttpFrameAcceptor {

    @Override
    protected void doAccept(Channel ch, HttpFrame frame) throws Exception {

        String emoji = EmojiUtil.EMOJI_ALL;

        StringBuilder builder = new StringBuilder(HttpUtil.HTML_HEADER);

        builder.append("\t\t<div id=\"container\" style=\"width: 90%;margin-left: auto;margin-right: auto;margin-top: 10px;font-size: 26px;color: rgb(175, 46, 46);\">\n");

        List<String> emojiList = EmojiUtil.bytes2Emojis(emoji.getBytes(Util.UTF8));

        String limitStr = frame.getRequestParam("limit");
        int    limit;
        if (Util.isNullOrBlank(limitStr)) {
            limit = emojiList.size();
        } else {
            limit = Integer.parseInt(limitStr);
            if (limit > emojiList.size()) {
                limit = emojiList.size();
            }
        }

        List<String> rgbList = getRGBList(String.valueOf(new Random().nextInt(56) + 200), emojiList.size());

        for (int i = 0; i < limit; i++) {
            builder.append("\t\t\t<span style=\"color:" + rgbList.get(i) + ";\">");
            builder.append(emojiList.get(i));
            builder.append("</span>\n");
        }

        builder.append("\t\t</div>\n");
        builder.append(HttpUtil.HTML_POWER_BY);
        //        builder.append(getScript());
        builder.append(HttpUtil.HTML_BOTTOM);
        ByteBuf buf = ByteBuf.wrap(builder.toString().getBytes(ch.getCharset()));
        buf.position(buf.limit());
        frame.setContent(buf);
        frame.setContentType(HttpContentType.text_html_utf8);
        ch.writeAndFlush(frame);
    }

    private List<String> getRGBList(String r, int size) {
        List<String> list = new ArrayList<>(size);
        int          i    = 0;
        int          b    = 255;
        for (int g = 255; g >= 0; ) {
            if (b < 100) {
                b = 0;
                for (; b <= 255; b += 5) {
                    if (i++ > size) {
                        return list;
                    }
                    list.add("rgb(" + r + "," + g + "," + b + ")");
                }
            } else {
                b = 255;
                for (; b >= 0; b -= 5) {
                    if (i++ > size) {
                        return list;
                    }
                    list.add("rgb(" + r + "," + g + "," + b + ")");
                }
            }
            g -= 6;
        }

        return list;
    }

    private String getScript() {
        return "\t\t<script>\n" + "\t\t\tvar container = document.getElementById(\"container\");\n" + "\t\t\tvar children = container.children;\n" + "\t\t\tvar forward = true;\n" + "\t\t\tfunction changeColor(children){\n" + "\t\t\t	var index = children[0].style.color.indexOf(\",\");\n" + "\t\t\t	var r = children[0].style.color.substring(4,index);\n" + "\t\t\t	r = getNextR(parseInt(r));\n" + "\t\t\t	for(var i=0;i<children.length;i++){\n" + "\t\t\t		var node = children[i];\n" + "\t\t\t		var rgb = \"rgb(\"+r+node.style.color.substring(index);\n" + "\t\t\t		node.style.color = rgb;\n" + "\t\t\t	}\n" + "\t\t\t	setTimeout(\"changeColor(children)\",16);\n" + "\t\t\t}\n" + "\t\t\tfunction getNextR(r){\n" + "\t\t\t	if(r == 0){\n" + "\t\t\t		forward = true;\n" + "\t\t\t		return 1;\n" + "\t\t\t	}else if(r >= 255){\n" + "\t\t\t		forward = false;\n" + "\t\t\t		r = 254;\n" + "\t\t\t	}\n" + "\t\t\t	if(forward){\n" + "\t\t\t		return r + 1;\n" + "\t\t\t	}else{\n" + "\t\t\t		return r - 1;\n" + "\t\t\t	}\n" + "\t\t\t}\n" + "\t\t\twindow.onload = function(){\n" + "\t\t\t	changeColor(children);\n" + "\t\t\t};\n" + "\t\t</script>\n";
    }

}
