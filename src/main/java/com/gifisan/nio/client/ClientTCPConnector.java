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
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.Connector;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPSelectorLoop;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.TaskExecutor;
import com.gifisan.nio.plugin.jms.client.impl.ConsumerStreamAcceptor;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;
import com.gifisan.nio.server.service.impl.SYSTEMAuthorityServlet;
import com.gifisan.security.Authority;

public class ClientTCPConnector implements Connector {

	private AtomicBoolean		connected		= new AtomicBoolean(false);
	private ClientContext		context		= null;
	private ClientTCPEndPoint	endPoint		= null;
	private AtomicBoolean		keepAlive		= new AtomicBoolean(false);
	private Selector			selector		= null;
	private InetSocketAddress	serverAddress	= null;
	private TaskExecutor		taskExecutor	= null;
	private TCPSelectorLoop		selectorLoop	= null;
	private ClientEndPointWriter	endPointWriter	= null;
	private ClientUDPConnector	udpConnector	= null;
	private String				machineType	= null;

	protected TCPSelectorLoop getSelectorLoop() {
		return selectorLoop;
	}

	protected EndPointWriter getEndPointWriter() {
		return endPointWriter;
	}

	public ClientTCPConnector(String host, int port,String machineType) {
		this.context = new ClientContext(host, port);
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
			LifeCycleUtil.stop(selectorLoop);
			LifeCycleUtil.stop(endPointWriter);
			CloseUtil.close(udpConnector);
			CloseUtil.close(endPoint);
		}
	}

	public void connect() throws IOException {
		if (connected.compareAndSet(false, true)) {
			try {
				this.context.start();
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}

			this.connect0();

			try {

				this.endPointWriter.start();

				this.selectorLoop.start();
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}
		}
	}

	private void finishConnect(Selector selector) throws IOException {
		Iterator<SelectionKey> iterator = select(0);
		finishConnect(iterator);
	}

	private void connect0() throws IOException {
		this.serverAddress = new InetSocketAddress(context.getServerHost(), context.getServerPort());
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
			endPointWriter.setEndPoint(this.endPoint);
			this.selectorLoop = new ClientSelectorManagerLoop(context, selector, endPointWriter);
			selectionKey.attach(endPoint);
		}
	}

	public ClientSession getClientSession() throws IOException {
		return endPoint.getSession();
	}

	protected ClientContext getContext() {
		return context;
	}

	public String getServerHost() {
		return context.getServerHost();
	}

	public int getServerPort() {
		return context.getServerPort();
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
		TouchDistantJob job = new TouchDistantJob(endPointWriter, endPoint, this.getClientSession());
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
				param.put("password", password);
				param.put("MACHINE_TYPE", machineType);

				String paramString = JSONObject.toJSONString(param);

				ReadFuture future = session.request(SYSTEMAuthorityServlet.SERVICE_NAME, paramString);

				RESMessage message = RESMessageDecoder.decode(future.getText());

				if (message.getCode() == 0) {

					String text = message.getDescription();

					String[] strs = text.split(";");

					Authority authority = new Authority(username, strs[0]);

					((ProtectedClientSession) session).setAuthority(authority);

					((ProtectedClientSession) session).setSessionID(strs[1]);

					((ProtectedClientSession) session).setMachineType(machineType);
				}

				return message;
			} catch (IOException e) {
				return new RESMessage(400, e.getMessage());
			}
		}

		return RESMessage.R_SUCCESS;
	}

	public boolean login(String username, String password) {

		RESMessage message = login4RES(username, password);

		return message.getCode() == 0;
	}

	public void logout() {
		// TODO complete logout

	}

	public String toString() {
		return "TCP:Connector@" + endPoint.toString();
	}

}
