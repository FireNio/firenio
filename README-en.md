
# BaseIO Project

[![Website](https://img.shields.io/badge/website-generallycloud-green.svg)](https://www.generallycloud.com)
[![Maven central](https://img.shields.io/badge/maven%20central-3.2.5.Release-green.svg)](http://mvnrepository.com/artifact/com.generallycloud/baseio-all)
[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/generallycloud/baseio/blob/master/LICENSE.txt)

BaseIO is an io framework which can build network project fast, it based on java nio, it is popular with Developers because of simple and easy of use APIs and high-performance.

## Features

 * support protocol extend, known:
   * Redis protocol(for test), for detail {baseio-test}
   * LineBased protocol, for detail {baseio-test}
   * FixedLength protocol, for detail {baseio-test}
   * HTTP1.1 protocol(lite), for detail: https://www.generallycloud.com/
   * WebSocket protocol, for detail: https://www.generallycloud.com/web-socket/chat/index.html 
   * Protobase(custom) support text and binay and text binay mixed transfer, for detail {baseio-test}
 * easy to support reconnect (easy to support heart beat)
 * supported ssl (jdkssl, openssl)
 * load test
   * over 200W QPS (Http1.1,I7-4790,16.04.1-Ubuntu)  [wrk load test](/baseio-documents/load-test/load-test-http.txt)
 
## Quick Start

 * Maven Dependency

  ```xml  
	<dependency>
		<groupId>com.generallycloud</groupId>
		<artifactId>baseio-all</artifactId>
		<version>3.2.5.RELEASE</version>
	</dependency>  
  ```
  
 * Simple Server:

  ```Java

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                FixedLengthFuture f = (FixedLengthFuture) future;
                future.write("yes server already accept your message:", channel.getCharset());
                future.write(f.getReadText(), channel.getCharset());
                channel.flush(future);
            }
        };
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelContext context = new ChannelContext(8300);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setIoEventHandle(eventHandleAdaptor);
        context.setProtocolCodec(new FixedLengthCodec());
        acceptor.bind();
    }

  ```

 * Simple Client:

  ```Java

    public static void main(String[] args) throws Exception {
        IoEventHandle eventHandleAdaptor = new IoEventHandle() {
            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                FixedLengthFuture f = (FixedLengthFuture) future;
                System.out.println();
                System.out.println("____________________" + f.getReadText());
                System.out.println();
            }

        };
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelContext context = new ChannelContext(8300);
        ChannelConnector connector = new ChannelConnector(context, group);
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.setProtocolCodec(new FixedLengthCodec());
        NioSocketChannel channel = connector.connect();
        FixedLengthFuture future = new FixedLengthFuture();
        future.write("hello server!", channel);
        channel.flush(future);
        ThreadUtil.sleep(100);
        CloseUtil.close(connector);
    }

  ```

###	more samples see project {baseio-test}

## Sample at website:
 * HTTP Demo:https://www.generallycloud.com/index.html
 * WebSocket Chat Demo:https://www.generallycloud.com/web-socket/chat/index.html                                
  (server based on baseio,client based on: https://github.com/socketio/socket.io/ )
 * WebSocket Rumpetroll Demo:https://www.generallycloud.com/web-socket/rumpetroll/index.html                                
  (server based on baseio,client based on:https://github.com/danielmahal/Rumpetroll )

## License

BaseIO is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## To learn more, join this QQ group, more java technique can talk at there.
 * QQ group NO: 540637859
 * Join by click this link: [![img](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=2bd71e10d876bb6035fa0ddc6720b5748fc8985cb666e17157d17bcfbd2bdaef)
 * Scan QR code:<br />  ![image](/baseio-documents/popularize/java-io-group-code-small.png)
