package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class JMSConsumerServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSConsumerServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) throws Exception {
		
		MQContext context = getMQContext();
		
		Parameters parameters = future.getParameters();
		
		String queueName = parameters.getParameter("queueName");
		
		if (attachment.containsQueueName(queueName)) {
			return;
		}

		attachment.addQueueName(queueName);

		context.addReceiver(queueName);
		
		getMQContext().pollMessage(session, future, attachment);

	}
}
