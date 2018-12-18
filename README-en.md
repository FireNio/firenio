
# BaseIO Project

[![Website](https://img.shields.io/badge/website-firenio-green.svg)](https://www.firenio.com)
[![Maven central](https://img.shields.io/badge/maven-3.2.9.BETA-green.svg)](http://mvnrepository.com/artifact/com.firenio/baseio-all)
[![License](https://img.shields.io/badge/License-Apache%202.0-585ac2.svg)](https://github.com/firenio/baseio/blob/master/LICENSE.txt)

BaseIO is an io framework which can build network project fast, it based on java nio, it is popular with Developers because of simple and easy of use APIs and high-performance.

## Features

 * support protocol extend, known:
   * Redis protocol(for test), for detail {baseio-test}
   * LineBased protocol, for detail {baseio-test}
   * FixedLength protocol, for detail {baseio-test}
   * HTTP1.1 protocol(lite), for detail: https://www.firenio.com/
   * WebSocket protocol, for detail: https://www.firenio.com/web-socket/chat/index.html 
   * Protobase(custom) support text and binay and text binay mixed transfer, for detail {baseio-test}
 * easy to support reconnect (easy to support heart beat)
 * supported ssl (jdkssl, openssl)
 * load test
   * [tfb benchmark](https://www.techempower.com/benchmarks/#section=test&runid=89191c04-89d9-4f7a-8da0-5d7b493f3d35&hw=ph&test=plaintext)
 
## Quick Start

 * Maven Dependency

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

###	more samples see project {baseio-test}

## Sample at website:
 * HTTP Demo:https://www.firenio.com/index.html
 * WebSocket Chat Demo:https://www.firenio.com/web-socket/chat/index.html                                
  (server based on baseio,client based on: https://github.com/socketio/socket.io/ )
 * WebSocket Rumpetroll Demo:https://www.firenio.com/web-socket/rumpetroll/index.html                                
  (server based on baseio,client based on:https://github.com/danielmahal/Rumpetroll )

## License

BaseIO is released under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

## To learn more, join this QQ group, more java technique can talk at there.
 * QQ group NO: 540637859
 * Join by click this link: [![img](http://pub.idqqimg.com/wpa/images/group.png)](http://shang.qq.com/wpa/qunwpa?idkey=2bd71e10d876bb6035fa0ddc6720b5748fc8985cb666e17157d17bcfbd2bdaef)
 * Scan QR code:<br />  ![image](/baseio-doc/popularize/java-io-group-code-small.png)
