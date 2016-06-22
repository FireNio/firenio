package com.gifisan.nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.BeanUtil;
import com.gifisan.nio.common.ClassUtil;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.IOConnector;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolDecoder;
import com.gifisan.nio.component.protocol.DefaultTCPProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.concurrent.TaskExecutor;
import com.gifisan.nio.concurrent.UniqueThread;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;
import com.gifisan.nio.server.configuration.ServerConfiguration;
import com.gifisan.nio.server.service.impl.SYSTEMAuthorityServlet;
import com.gifisan.security.Authority;

public class TCPConnector implements IOConnector {

	private AtomicBoolean		connected				= new AtomicBoolean(false);
	private NIOContext			context				= null;
	private ClientTCPEndPoint	endPoint				= null;
	private AtomicBoolean		keepAlive				= new AtomicBoolean(false);
	private Selector			selector				= null;
	private InetSocketAddress	serverAddress			= null;
	private TaskExecutor		taskExecutor			= null;
	private TCPSelectorLoop		selectorLoop			= null;
	private ClientEndPointWriter	endPointWriter			= null;
	private UniqueThread		endPointWriterThread	= new UniqueThread();
	private UniqueThread		selectorLoopThread		= new UniqueThread();
	private UDPConnector		udpConnector			= null;
	private String				machineType			= null;
	private ProtocolDecoder		protocolDecoder		= null;
	private ProtocolEncoder		protocolEncoder		= null;
	private IOEventHandle		ioEventHandle			= null;

	protected TCPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	protected EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public TCPConnector(IOEventHandle ioEventHandle, String machineType) {
		this.ioEventHandle = ioEventHandle;
		this.machineType = machineType;
	}

	public TCPConnector(ProtocolDecoder protocolDecoder, ProtocolEncoder protocolEncoder, IOEventHandle ioEventHandle,
			String machineType) {
		this.machineType = machineType;
	}

	public void close() throws IOException {

		Thread thread = Thread.currentThread();

		if (selectorLoop.isMonitor(thread)) {
			throw new IllegalStateException("not allow to close on future callback");
		}

		if (connected.compareAndSet(true, false)) {
			LifeCycleUtil.stop(context);
			LifeCycleUtil.stop(taskExecutor);
			
			selectorLoopThread.stop();
			endPointWriterThread.stop();
			
			CloseUtil.close(udpConnector);
			CloseUtil.close(endPoint);
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {

			if (protocolEncoder == null) {
				protocolEncoder = new DefaultTCPProtocolEncoder();
			}

			if (protocolDecoder == null) {
				protocolDecoder = new DefaultTCPProtocolDecoder();
			}

			this.context = new DefaultNIOContext(protocolDecoder, protocolEncoder, ioEventHandle);

			this.context.setTCPIOService(this);
			
			try {
				this.context.start();
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}

			this.connect0();

			this.endPointWriterThread.start(endPointWriter, endPointWriter.toString());

			this.selectorLoopThread.start(selectorLoop, selectorLoop.toString());
		}
	}

	private void finishConnect(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
	}

	private void connect0() throws IOException {

		ServerConfiguration configuration = context.getServerConfiguration();

		String SERVER_HOST = configuration.getSERVER_HOST();

		int SERVER_PORT = configuration.getSERVER_PORT();

		this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);

		SocketChannel channel = SocketChannel.open();

		channel.configureBlocking(false);

		selector = Selector.open();

		channel.register(selector, SelectionKey.OP_CONNECT);

		channel.connect(serverAddress);

		finishConnect(selector);
	}

	private void finishConnect(Iterator<SelectionKey> iterator) throws IOException {

		for (; iterator.hasNext();) {

			SelectionKey selectionKey = iterator.next();

			iterator.remove();

			finishConnect0(selectionKey);
		}
	}

	private void finishConnect0(SelectionKey selectionKey) throws IOException {

		SocketChannel channel = (SocketChannel) selectionKey.channel();

		// does it need connection pending ?
		if (selectionKey.isConnectable() && channel.isConnectionPending()) {

			channel.finishConnect();

			channel.register(selector, SelectionKey.OP_READ);

			this.endPointWriter = new ClientEndPointWriter();

			this.endPoint = new ClientTCPEndPoint(context, selectionKey, this);

			this.endPointWriter.setEndPoint(endPoint);

			this.selectorLoop = new ClientSelectorManagerLoop(context, selector, endPointWriter);

			selectionKey.attach(endPoint);
		}
	}

	public ClientSession getClientSession() {
		return endPoint.getSession();
	}

	protected NIOContext getContext() {
		return context;
	}

	/**
	 * 每分钟一个心跳包
	 */
	public void keepAlive() {

		this.keepAlive(60 * 1000);
	}

	public void keepAlive(long checkInterval) {
		if (keepAlive.compareAndSet(false, true)) {
			try {
				this.startTouchDistantJob(checkInterval);
			} catch (Exception e) {

			}
		}
	}

	private Iterator<SelectionKey> select(long timeout) throws IOException {
		selector.select(timeout);
		Set<SelectionKey> selectionKeys = selector.selectedKeys();
		return selectionKeys.iterator();
	}

	private void startTouchDistantJob(long checkInterval) throws Exception {
		TouchDistantJob job = new TouchDistantJob(endPointWriter, endPoint);
		this.taskExecutor = new TaskExecutor(job, "touch-distant-task", checkInterval);
		this.taskExecutor.start();
	}

	private AtomicBoolean	logined	= new AtomicBoolean(false);

	public RESMessage login4RES(String username, String password) {

		if (logined.compareAndSet(false, true)) {

			try {

				ClientSession session = endPoint.getSession();

				Map<String, Object> param = new HashMap<String, Object>();
				param.put("username", username);
				param.put("password", MD5Token.getInstance().getLongToken(password, Encoding.DEFAULT));
				param.put("MACHINE_TYPE", machineType);

				String paramString = JSONObject.toJSONString(param);

				ReadFuture future = session.request(SYSTEMAuthorityServlet.SERVICE_NAME, paramString);

				RESMessage message = RESMessageDecoder.decode(future.getText());

				if (message.getCode() == 0) {

					JSONObject o = (JSONObject) message.getData();

					String className = o.getString("className");

					Class clazz = ClassUtil.forName(className);

					Authority authority = (Authority) BeanUtil.map2Object(o, clazz);

					((ProtectedClientSession) session).setAuthority(authority);

					((ProtectedClientSession) session).setSessionID(authority.getSessionID());

					((ProtectedClientSession) session).setMachineType(machineType);
				} else {

					logined.compareAndSet(true, false);
				}

				return message;
			} catch (Exception e) {
				logined.compareAndSet(true, false);
				return new RESMessage(400, e.getMessage());
			}
		}

		return RESMessage.SUCCESS;
	}

	public boolean login(String username, String password) {

		RESMessage message = login4RES(username, password);

		return message.getCode() == 0;
	}

	public void logout() {
		// TODO complete logout

	}

	public boolean isLogined() {
		return logined.get();
	}

	public String toString() {
		return "TCP:Connector@" + endPoint.toString();
	}

}
