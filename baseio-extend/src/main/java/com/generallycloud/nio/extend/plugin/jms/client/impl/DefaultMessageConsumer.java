package com.generallycloud.nio.extend.plugin.jms.client.impl;

import java.io.IOException;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.RESMessageDecoder;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.client.MessageConsumer;
import com.generallycloud.nio.extend.plugin.jms.client.OnMessage;
import com.generallycloud.nio.extend.plugin.jms.decode.DefaultMessageDecoder;
import com.generallycloud.nio.extend.plugin.jms.decode.MessageDecoder;
import com.generallycloud.nio.extend.plugin.jms.server.MQTransactionServlet;

public class DefaultMessageConsumer implements MessageConsumer {

	private MessageDecoder	messageDecoder			= new DefaultMessageDecoder();
	private boolean		sendReceiveCommand		= true;
	private boolean		sendSubscribeCommand	= true;
	private FixedSession	session;

	public DefaultMessageConsumer(FixedSession session) {
		this.session = session;
	}

	public boolean beginTransaction() throws MQException {
		return transactionVal("begin");
	}

	private boolean transactionVal(String action) throws MQException {
		try {

			WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

			session.listen(MQTransactionServlet.SERVICE_NAME, onReadFuture);

			session.write(MQTransactionServlet.SERVICE_NAME, action);

			if (onReadFuture.await(3000)) {
				throw MQException.TIME_OUT;
			}
			
			NIOReadFuture future = (NIOReadFuture) onReadFuture.getReadFuture();

			RESMessage message = RESMessageDecoder.decode(future.getText());

			if (message.getCode() == 0) {
				return true;
			} else {
				throw new MQException(message.getDescription());
			}
			
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}
	}

	public boolean commit() throws MQException {
		return transactionVal("commit");
	}

	public boolean rollback() throws MQException {
		return transactionVal("rollback");
	}

	public void receive(OnMessage onMessage) throws MQException {

		sendReceiveCommandCallback(onMessage);
	}

	public void subscribe(OnMessage onMessage) throws MQException {

		sendSubscribeCommandCallback(onMessage);
	}

	private void sendReceiveCommandCallback(OnMessage onMessage) throws MQException {
		
		if (sendReceiveCommand) {
			return;
		}
		
		checkLoginState();

		try {

			session.listen("MQConsumerServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

			session.write("MQConsumerServlet", null);

			sendReceiveCommand = false;
		} catch (IOException e) {
			throw new MQException(e);
		}
	}

	private void checkLoginState() throws MQException {
		if (session.getAuthority() == null) {
			throw new MQException("not login");
		}
	}

	private void sendSubscribeCommandCallback(OnMessage onMessage) throws MQException {
		
		if (!sendSubscribeCommand) {
			return;
		}

		checkLoginState();

		try {

			session.listen("MQSubscribeServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

			session.write("MQSubscribeServlet", null);

			sendSubscribeCommand = false;
		} catch (IOException e) {
			throw new MQException(e);
		}
	}
}
