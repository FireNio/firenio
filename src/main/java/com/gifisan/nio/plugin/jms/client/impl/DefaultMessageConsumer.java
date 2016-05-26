package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.WaiterOnReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.decode.DefaultMessageDecoder;
import com.gifisan.nio.plugin.jms.decode.MessageDecoder;
import com.gifisan.nio.plugin.jms.server.JMSConsumerServlet;
import com.gifisan.nio.plugin.jms.server.JMSTransactionServlet;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;

public class DefaultMessageConsumer implements MessageConsumer {

	private MessageDecoder	messageDecoder			= new DefaultMessageDecoder();
	private boolean		sendReceiveCommand		= true;
	private boolean		sendSubscribeCommand	= true;
	private ClientSession	session				= null;
	private String			SERVICE_NAME			= JMSConsumerServlet.SERVICE_NAME;


	public DefaultMessageConsumer(ClientSession session) {
		this.session = session;
		this.session.onStreamRead(SERVICE_NAME, new ConsumerStreamAcceptor());
	}

	public boolean beginTransaction() throws JMSException {
		return transactionVal("begin");
	}

	private boolean transactionVal(String action) throws JMSException {
		try {

			WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

			session.listen(JMSTransactionServlet.SERVICE_NAME, onReadFuture);

			session.write(JMSTransactionServlet.SERVICE_NAME, action);

			if (onReadFuture.await(3000)) {

				ReadFuture future = onReadFuture.getReadFuture();

				RESMessage message = RESMessageDecoder.decode(future.getText());

				if (message.getCode() == 0) {
					return true;
				} else {
					throw new JMSException(message.getDescription());
				}
			}

			throw JMSException.TIME_OUT;
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
	}

	public boolean commit() throws JMSException {
		return transactionVal("commit");
	}

	public boolean rollback() throws JMSException {
		return transactionVal("rollback");
	}

	// TODO complete this 考虑收到失败message的处理
	// TODO cancel receive
	public void receive(OnMessage onMessage) throws JMSException {

		sendReceiveCommandCallback(onMessage);
	}

	// TODO complete this 考虑收到失败message的处理
	// TODO cancel subscribe
	public void subscribe(OnMessage onMessage) throws JMSException {

		sendSubscribeCommandCallback(onMessage);
	}

	private void sendReceiveCommandCallback(OnMessage onMessage) throws JMSException {
		if (sendReceiveCommand) {
			
			checkLoginState();
			
			try {

				session.listen("JMSConsumerServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

				session.write("JMSConsumerServlet", null);

				sendReceiveCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}
		}
	}
	
	private void checkLoginState() throws JMSException{
		
		if (session.getAuthority() == null) {
			throw new JMSException("not login");
		}
	}

	private void sendSubscribeCommandCallback(OnMessage onMessage) throws JMSException {
		if (sendSubscribeCommand) {
			
			checkLoginState();
			
			try {

				session.listen("JMSSubscribeServlet", new ConsumerOnReadFuture(onMessage, messageDecoder));

				session.write("JMSSubscribeServlet", null);

				sendSubscribeCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}

		}
	}
}
