
# BaseIO Project

[![Website](https://img.shields.io/badge/website-firenio-green.svg)](https://www.firenio.com)
[![Maven central](https://img.shields.io/badge/maven-3.2.9.BETA-green.svg)](http://mvnrepository.com/artifact/com.firenio/baseio-all)
[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/firenio/baseio/blob/master/LICENSE.txt)

BaseIO是基于java nio开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 项目特色

 * 支持协议扩展，已知的扩展协议有：
   * Redis协议(仅作测试)，示例：详见 {baseio-test}
   * LineBased协议(基于换行符的消息分割)，示例：详见 {baseio-test}
   * FixedLength协议(固定长度报文头)，支持传输文本和二进制数据
   * HTTP1.1协议(lite)，示例： https://www.firenio.com/
   * WebSocket协议，示例： https://www.firenio.com/web-socket/chat/index.html 
   * Protobase(自定义协议)，支持传输文本和二进制数据及混合数据
 * 轻松实现断线重连(轻松实现心跳机制)
 * 支持SSL(jdkssl,openssl)
 * 压力测试
   * [tfb benchmark](https://www.techempower.com/benchmarks/#section=test&runid=89191c04-89d9-4f7a-8da0-5d7b493f3d35&hw=ph&test=plaintext)
 
## 快速入门

 * Maven引用：

  ```xml  
	<dependency>
		<groupId>com.firenio</groupId>
		<artifactId>baseio-all</artifactId>
		<version>3.2.9.BETA-4</version>
	</dependency>  
  ```
  
 * Simple Server:

  ```Java

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                FixedLengthFrame f = (FixedLengthFrame) frame;
                frame.write("yes server already accept your message:", channel.getCharset());
                frame.write(f.getReadText(), channel.getCharset());
                channel.flush(frame);
            }
        };
        ChannelAcceptor context = new ChannelAcceptor(8300);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandle);
        context.setProtocolCodec(new FixedLengthCodec());
        context.bind();
    }

  ```

 * Simple Client:

  ```Java

        ChannelConnector context = new ChannelConnector(8300);

        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                FixedLengthFrame f = (FixedLengthFrame) frame;
                System.out.println();
                System.out.println("____________________" + f.getReadText());
                System.out.println();
                context.close();
            }
        };

        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new FixedLengthCodec());
        context.connect((ch, ex) -> {
            FixedLengthFrame frame = new FixedLengthFrame();
            frame.write("hello server!", ch);
            ch.flush(frame);
        });
    }

  ```

###	更多样例详见 {baseio-test}

## 演示及用例
 * HTTP Demo：https://www.firenio.com/index.html
 * WebSocket聊天室 Demo：https://www.firenio.com/web-socket/chat/index.html                                
  (后端基于baseio，前端基于：https://github.com/socketio/socket.io/ )
 * WebSocket小蝌蚪 Demo：https://www.firenio.com/web-socket/rumpetroll/index.html                                
  (后端基于baseio，前端基于：https://github.com/danielmahal/Rumpetroll )

## License

BaseIO is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## 了解更多，加入该项目QQ群，更多java相关技术均可在此讨论
 * QQ群号码：540637859
 * 点击链接加入：[![img](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=2bd71e10d876bb6035fa0ddc6720b5748fc8985cb666e17157d17bcfbd2bdaef)
 * 扫码加入：<br />  ![image](/baseio-doc/popularize/java-io-group-code-small.png)
