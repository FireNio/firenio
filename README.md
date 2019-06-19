
# FireNio Project

[![Website](https://img.shields.io/badge/website-firenio-green.svg)](https://www.firenio.com)
[![Maven central](https://img.shields.io/badge/maven-1.2.3-green.svg)](http://mvnrepository.com/artifact/com.firenio/firenio-all)
[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/firenio/firenio/blob/master/LICENSE.txt)

FireNio是基于java nio开发的一款可快速构建网络通讯项目的异步IO框架，其以简单易用的API和优良的性能深受开发者喜爱。

## 项目特色

 * 支持协议扩展，已知的扩展协议有：
   * LengthValue协议，支持传输文本数据
   * HTTP1.1协议(lite)，示例： https://www.firenio.com/
   * WebSocket协议，示例： https://www.firenio.com/web-socket/chat/index.html 
   * Protobase(自定义协议)，支持传输文本或二进制数据
 * 轻松实现断线重连(轻松实现心跳机制)
 * 支持SSL(jdkssl,openssl)
 * 压力测试
   * [tfb benchmark](https://www.techempower.com/benchmarks/#section=test&runid=76a34044-54d6-4349-adfe-863c2d5ae756&hw=ph&test=plaintext)
 
## 快速入门

 * Maven引用：

  ```xml  
	<dependency>
		<groupId>com.firenio</groupId>
		<artifactId>firenio-all</artifactId>
		<version>1.2.3</version>
	</dependency>  
  ```
  
 * Simple Server:

  ```Java

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                String text = f.getStringContent();
                f.setContent(ch.allocate());
                f.write("yes server already accept your message:", ch);
                f.write(text, ch);
                ch.writeAndFlush(f);
            }
        };
        ChannelAcceptor context = new ChannelAcceptor(8300);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.addProtocolCodec(new LengthValueCodec());
        context.bind();
    }

  ```

 * Simple Client:

  ```Java

    public static void main(String[] args) throws Exception {
        ChannelConnector context = new ChannelConnector("127.0.0.1", 8300);
        IoEventHandle eventHandle = new IoEventHandle() {
            @Override
            public void accept(Channel ch, Frame f) throws Exception {
                System.out.println();
                System.out.println("____________________" + f.getStringContent());
                System.out.println();
                context.close();
            }
        };

        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new LengthValueCodec());
        Channel ch = context.connect(3000);
        LengthValueFrame frame = new LengthValueFrame();
        frame.setString("hello server!");
        ch.writeAndFlush(frame);
    }

  ```

###	更多样例详见 {firenio-test}

## 演示及用例
 * HTTP Demo：https://www.firenio.com/index.html
 * WebSocket聊天室 Demo：https://www.firenio.com/web-socket/chat/index.html                                
  (后端基于firenio，前端基于：https://github.com/socketio/socket.io/ )
 * WebSocket小蝌蚪 Demo：https://www.firenio.com/web-socket/rumpetroll/index.html                                
  (后端基于firenio，前端基于：https://github.com/danielmahal/Rumpetroll )

## License

FireNio is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## 了解更多，加入该项目QQ群，更多java相关技术均可在此讨论
 * QQ群号码：540637859
 * 点击链接加入：[![img](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=2bd71e10d876bb6035fa0ddc6720b5748fc8985cb666e17157d17bcfbd2bdaef)
 * 扫码加入：<br />  ![image](/firenio-doc/java-io-group-code-small.png)
