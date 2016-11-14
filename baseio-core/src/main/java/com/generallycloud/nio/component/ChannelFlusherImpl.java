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

	private BaseContext						context		= null;

	public ChannelFlusherImpl(BaseContext context) {
		this.context = context;
	}

	public void loop() {

		List<ChannelFlusherEvent> events = this.events.getSnapshot();

		if (!events.isEmpty()) {

			fireEvents(events);
		}

		// doLoop(channels.poll(16));

		doLoop(channels);
	}

	private void doLoop(BufferedArrayList<SocketChannel> channels) {

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

				channels.offer(ch);

				continue;
			}

			if (ch.needFlush()) {
				channels.offer(ch);
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

		// for (; channels.size() > 0;) {
		// ThreadUtil.sleep(8);
		// }
	}

	public void fire(ChannelFlusherEvent event) {
		events.add(event);
	}

	public void offer(SocketChannel channel) {
		// channels.offer(channel);
		channels.offer(channel);
	}

	public void wekeupSocketChannel(SocketChannel channel) {
		sleepChannels.remove(channel.getChannelID());
		// channels.offer(channel);
		channels.offer(channel);
	}

	public void startup() throws Exception {

	}

	public String toString() {

		ChannelService service = context.getSocketChannelService();

		return service.getServiceDescription() + "(writer)";
	}

}
