package com.generallycloud.nio.component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.concurrent.BufferedArrayList;
import com.generallycloud.nio.component.concurrent.ReentrantList;

public class ChannelFlusherImpl implements ChannelFlusher {

	private Map<Integer, SocketChannel>		sleepChannels	= new HashMap<Integer, SocketChannel>();

	private BufferedArrayList<SocketChannel>	channels		= new BufferedArrayList<SocketChannel>();

	private ReentrantList<ChannelFlusherEvent>	events		= new ReentrantList<ChannelFlusherEvent>();

	private NIOContext						context		= null;
	
	public ChannelFlusherImpl(NIOContext context) {
		this.context = context;
	}

	public void loop() {

		List<ChannelFlusherEvent> events = this.events.getSnapshot();

		if (!events.isEmpty()) {

			fireEvents(events);
		}

		List<SocketChannel> chs = channels.getBuffer();

		if (chs.size() == 0) {
			// FIXME wait ?
			ThreadUtil.sleep(16);
			return;
		}

		for (SocketChannel ch : chs) {

			if (ch == null) {
				System.out.println(chs.size());
			}
			
			if (!ch.isOpened()) {
				continue;
			}

			boolean flush;

			try {

				flush = ch.flush();

			} catch (IOException e) {

				CloseUtil.close(ch);

				continue;
			}

			if (!flush) {

				if (ch.isNetworkWeak()) {

					sleepChannels.put(ch.getChannelID(), ch);

					continue;
				}

				channels.safeAdd(ch);

				continue;
			}

			if (ch.getWriteFutureSize() > 0) {
				channels.safeAdd(ch);
			}
		}
	}

	private void fireEvents(List<ChannelFlusherEvent> events) {

		for (ChannelFlusherEvent e : events) {

			e.handle(this);
		}

		events.clear();
	}

	public void stop() {
		for (; channels.getBufferSize() > 0;) {
			ThreadUtil.sleep(8);
		}
	}

	public void fire(ChannelFlusherEvent event) {
		events.add(event);
	}

	public void offer(SocketChannel channel) {
		channels.safeAdd(channel);
	}

	public void wekeupSocketChannel(SocketChannel channel) {
		sleepChannels.remove(channel.getChannelID());
		channels.safeAdd(channel);
	}

	public String toString() {

		IOService service = context.getTCPService();

		return service.getServiceDescription() + "(writer)";
	}

}
