package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.AbstractTCPSelectionAlpha;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;

public class TCPSelectionAcceptor extends AbstractTCPSelectionAlpha {

	private Selector				selector;
	private NIOContext				context			;
	private EndPointWriter			endPointWriter		;
	private int					this_core_index	;
	private int					next_core_index	;
	private CoreProcessors			processors		;
//	private Logger					logger			= LoggerFactory.getLogger(TCPSelectionAcceptor.class);

	public TCPSelectionAcceptor(NIOContext context,CoreProcessors processors) {
		super(context);
		
		this.context = context;
		
		this.processors = processors;
		
		ReentrantLock lock = processors.getReentrantLock();
		
		lock.lock();
		
		try{
			
			this.this_core_index = processors.getCurrentCoreIndex();
			
			this.next_core_index = this_core_index + 1;
			
			if (next_core_index == processors.getCoreSize()) {
				next_core_index = 0;
			}
			
			processors.setCurrentCoreIndex(next_core_index);
		}finally{
			
			lock.unlock();
		}
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		SocketChannel channel;
		
		CoreProcessors processors = this.processors;
		
		ReentrantLock lock = processors.getReentrantLock();
		
		lock.lock();
		
		try{
			
			int core_index = processors.getCurrentCoreIndex();
			
			if (this_core_index != core_index) {
				return;
			}

			// 返回为之创建此键的通道。
			ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
			// 此方法返回的套接字通道（如果有）将处于阻塞模式。
			channel = server.accept();
			// 前面已经有人获取到channel，而且把core_index+1，
			//这里虽然匹配到core，但是是拿不到channel的，有待改进
			if (channel == null) {
//				System.out.println(core_index);
//				System.out.println(this_core_index);
//				Exception e = new Exception("core_index error");
//				logger.error(e.getMessage(), e);
				return;
			}
			
			processors.setCurrentCoreIndex(next_core_index);
			
		}finally{
			
			lock.unlock();
		}
		
		// 配置为非阻塞
		channel.configureBlocking(false);
		// 注册到selector，等待连接
		SelectionKey sk = channel.register(selector, SelectionKey.OP_READ);
		// 绑定EndPoint到SelectionKey
		attachEndPoint(context, endPointWriter, sk);

		// logger.debug("__________________chanel____gen____{}", channel);

	}
	
	public EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public void setEndPointWriter(EndPointWriter endPointWriter) {
		this.endPointWriter = endPointWriter;
	}

	protected void setSelector(Selector selector) {
		this.selector = selector;
	}

}
