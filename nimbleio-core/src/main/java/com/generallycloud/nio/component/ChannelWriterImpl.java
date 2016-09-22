package com.generallycloud.nio.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.WriterOverflowException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.component.concurrent.ReentrantList;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.configuration.ServerConfiguration;

//FIXME 问题好像出在这里
//FIXME 如果当前edp网速良好则多执行几次write
public class ChannelWriterImpl implements ChannelWriter {

	private ListQueue<IOWriteFuture>			writerQueue;
	private Map<Integer, List<IOWriteFuture>>	sleepChannels	= new HashMap<Integer, List<IOWriteFuture>>();
	private Logger							logger		= LoggerFactory.getLogger(ChannelWriterImpl.class);
	private ReentrantList<ChannelWriteEvent>	events		= new ReentrantList<ChannelWriteEvent>();
	private NIOContext						context		= null;

	public ChannelWriterImpl(NIOContext context) {

		this.context = context;

		ServerConfiguration configuration = context.getServerConfiguration();

		int capacity = configuration.getSERVER_CHANNEL_QUEUE_SIZE();

		this.writerQueue = new ListQueueABQ<IOWriteFuture>(capacity);
	}

	public void fire(ChannelWriteEvent event) {
		events.add(event);
	}

	public void offer(IOWriteFuture future) {

		if (!this.writerQueue.offer(future)) {

			future.onException(WriterOverflowException.INSTANCE);
		}
	}

	public void wekeupSocketChannel(SocketChannel channel) {

		Integer channelID = channel.getChannelID();

		List<IOWriteFuture> list = sleepChannels.get(channelID);

		if (list == null) {
			return;
		}

		for (IOWriteFuture f : list) {

			this.offer(f);
		}

		sleepChannels.remove(channelID);
	}

	private void sleepWriter(SocketChannel channel, IOWriteFuture future) {

		Integer channelID = channel.getChannelID();

		List<IOWriteFuture> list = sleepChannels.get(channelID);

		if (list == null) {

			list = new ArrayList<IOWriteFuture>();

			sleepChannels.put(channelID, list);
		}

		list.add(future);
	}

	private void fireEvents(List<ChannelWriteEvent> events) {

		for (ChannelWriteEvent e : events) {

			e.handle(this);
		}

		events.clear();
	}

	public void loop() {

		List<ChannelWriteEvent> events = this.events.getSnapshot();

		if (!events.isEmpty()) {

			fireEvents(events);
		}

		IOWriteFuture futureFromQueue = writerQueue.poll(16);

		if (futureFromQueue == null) {

			return;
		}

		SocketChannel channel = futureFromQueue.getSocketChannel();

		if (!channel.isOpened()) {

			futureFromQueue.onException(new DisconnectException("disconnected"));

			return;
		}

		if (channel.isNetworkWeak()) {

			this.sleepWriter(channel, futureFromQueue);

			return;
		}

		try {

			IOWriteFuture futureFromChannel = channel.getCurrentWriteFuture();

			if (futureFromChannel != null) {

				doWriteFutureFromChannel(futureFromChannel, futureFromQueue, channel);

				return;
			}

			doWriteFutureFromQueue(futureFromQueue, channel);

		} catch (IOException e) {
			logger.debug(e);

			CloseUtil.close(channel);

			futureFromQueue.onException(e);

		} catch (Throwable e) {
			logger.debug(e);

			CloseUtil.close(channel);

			futureFromQueue.onException(new IOException(e));
		}
	}

	// write future from Channel
	private void doWriteFutureFromChannel(IOWriteFuture futureFromChannel, IOWriteFuture futureFromQueue,
			SocketChannel channel) throws IOException {

		if (futureFromChannel.write()) {

			channel.setCurrentWriteFuture(null);

			futureFromChannel.onSuccess();

			doWriteFutureFromQueue(futureFromQueue, channel);

			return;
		}

		if (!writerQueue.offer(futureFromQueue)) {

			logger.debug("give up {}", futureFromQueue);

			futureFromChannel.onException(WriterOverflowException.INSTANCE);
		}
	}

	private void doWriteFutureFromQueue(IOWriteFuture futureFromQueue, SocketChannel channel) throws IOException {

		if (futureFromQueue.write()) {

			futureFromQueue.onSuccess();

		} else {

			channel.setCurrentWriteFuture(futureFromQueue);

			offer(futureFromQueue);
		}
	}

	public void stop() {
		for(;writerQueue.size() > 0;){
			ThreadUtil.sleep(8);
		}
	}

	public String toString() {

		IOService service = context.getTCPService();

		return service.getServiceDescription() + "(writer)";
	}

	public interface ChannelWriteEvent {

		void handle(ChannelWriter channelWriter);
	}
}
