package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

//FIXME 有的连接会断掉，但是没有执行session close，这些连接莫名其妙断掉的，
//发送消息后服务端收不到，也不会回复
//改进心跳机制，服务端客户端均要发出心跳
//客户端心跳：task&request超时即断开链接
//服务端心跳：session-manager监测上次收到回报时间，长时间没有交互则发出心跳包，
//下次循环时检测是否收到心跳
public interface SocketChannel extends DuplexChannel {

	public abstract void setCurrentWriteFuture(IOWriteFuture future);

	public abstract IOWriteFuture getCurrentWriteFuture();

	public abstract boolean isNetworkWeak();

	public abstract void updateNetworkState(int length);

	public abstract void wakeup() throws IOException;

	public abstract IOReadFuture getReadFuture();

	public abstract void setReadFuture(IOReadFuture future);

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;

	public abstract void offer(IOWriteFuture future);

	public abstract boolean isBlocking();
	
	public abstract ProtocolEncoder getProtocolEncoder() ;

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder) ;

	public abstract ProtocolDecoder getProtocolDecoder() ;

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder) ;
	
	public abstract ProtocolFactory getProtocolFactory() ;

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory) ;
}
