
# BaseIO Project

[![Website](https://img.shields.io/badge/website-generallycloud-green.svg)](https://www.generallycloud.com)
[![Maven central](https://img.shields.io/badge/maven%20central-3.2.2.RELEASE-green.svg)](http://mvnrepository.com/artifact/com.generallycloud/baseio-all)
[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/generallycloud/baseio/blob/master/LICENSE.txt)

BaseIO is an io framework which can build network project fast, it based on java nio/aio, it is popular with Developers because of simple and easy of use APIs and high-performance.

## Features

 * easy to support reconnect (easy to support heart beat)
 * simple application container
   * simple hot deploy , eg: https://www.generallycloud.com/system-redeploy
   * support deploy http , micro service (depend on your protocol)
 * easy to supprot load balance, known:
   * virtual node based on hash
   * loop balance node 
 * support protocol extend, known:
   * Redis protocol, for detail {baseio-test}
   * Protobuf protocol, for detail {baseio-test}
   * LineBased protocol, for detail {baseio-test}
   * FixedLength protocol, for detail {baseio-test}
   * HTTP1.1 protocol, for detail: https://www.generallycloud.com/
   * WebSocket protocol, for detail: https://www.generallycloud.com/web-socket/chat/index.html 
   * Protobase(custom) support text and binay and text binay mixed transfer, for detail {baseio-test}
 * load test
   * over 200W QPS (Http1.1,I7-4790,16.04.1-Ubuntu)  [wrk load test](/baseio-documents/load-test/load-test-http.txt)
 
## Quick Start

 * Maven Dependency

  ```xml  
	<dependency>
		<groupId>com.generallycloud</groupId>
		<artifactId>baseio-all</artifactId>
		<version>3.2.2.RELEASE</version>
	</dependency>  
  ```
  
 * A simple server:

  ```Java

    public static void main(String[] args) throws Exception {
        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                future.write("yes server already accept your message:");
                future.write(future.getReadText());
                session.flush(future);
            }
        };
        SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));
        //use java aio
        //		SocketChannelContext context = new AioSocketChannelContext(new ServerConfiguration(18300));
        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.setProtocolFactory(new FixedLengthProtocolFactory());
        acceptor.bind();
    }

  ```

 * A simple client:

  ```Java

    public static void main(String[] args) throws Exception {
        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                System.out.println();
                System.out.println("____________________" + future.getReadText());
                System.out.println();
            }
        };
        SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration("localhost", 18300));
        //use java aio
        //		SocketChannelContext context = new AioSocketChannelContext(new ServerConfiguration(18300));
        SocketChannelConnector connector = new SocketChannelConnector(context);
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.setProtocolFactory(new FixedLengthProtocolFactory());
        SocketSession session = connector.connect();
        FixedLengthFuture future = new FixedLengthFutureImpl(context);
        future.write("hello server!");
        session.flush(future);
        ThreadUtil.sleep(100);
        CloseUtil.close(connector);
    }

  ```

###	more samples {baseio-test}

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
