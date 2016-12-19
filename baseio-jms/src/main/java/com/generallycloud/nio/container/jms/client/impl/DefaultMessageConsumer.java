package com.generallycloud.nio.container.jms.client.impl;

import java.io.IOException;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.RESMessageDecoder;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.client.MessageConsumer;
import com.generallycloud.nio.container.jms.client.OnMessage;
import com.generallycloud.nio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.nio.container.jms.decode.MessageDecoder;
import com.generallycloud.nio.container.jms.server.MQTransactionServlet;

public class DefaultMessageConsumer implements MessageConsumer {

	private MessageDecoder	messageDecoder			= new DefaultMessageDecoder();
	private boolean		needSendReceiveCommand	= true;
	private boolean		needSendSubscribeCommand	= true;
	private FixedSession	session;

	public DefaultMessageConsumer(FixedSession session) {
		this.session = session;
	}

	@Override
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

			ProtobaseReadFuture future = (ProtobaseReadFuture) onReadFuture.getReadFuture();

			RESMessage message = RESMessageDecoder.decode(future.getReadText());

			if (message.getCode() == 0) {
				return true;
			} else {
				throw new MQException(message.getDescription());
			}

		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}
	}

	@Override
	public boolean commit() throws MQException {
		return transactionVal("commit");
	}

	@Override
	public boolean rollback() throws MQException {
		return transactionVal("rollback");
	}

	@Override
	public void receive(OnMessage onMessage) throws MQException {

		sendReceiveCommandCallback(onMessage);
	}

	@Override
	public void subscribe(OnMessage onMessage) throws MQException {

		sendSubscribeCommandCallback(onMessage);
	}

	private void sendReceiveCommandCallback(OnMessage onMessage) throws MQException {

		if (!needSendReceiveCommand) {
			return;
		}

		checkLoginState();

		try {

			session.listen("MQConsumerServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

			session.write("MQConsumerServlet", null);

			needSendReceiveCommand = false;
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

		if (!needSendSubscribeCommand) {
			return;
		}

		checkLoginState();

		try {

			session.listen("MQSubscribeServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

			session.write("MQSubscribeServlet", null);

			needSendSubscribeCommand = false;
		} catch (IOException e) {
			throw new MQException(e);
		}
	}
}
