package com.generallycloud.nio.rtp;

import com.generallycloud.nio.configuration.ServerConfiguration;

public class RtpContext {
	
	

	private ServerConfiguration socketChannelConfig;
	
	private ServerConfiguration datagramChannelConfig;

	public ServerConfiguration getSocketChannelConfig() {
		return socketChannelConfig;
	}

	public void setSocketChannelConfig(ServerConfiguration socketChannelConfig) {
		this.socketChannelConfig = socketChannelConfig;
	}

	public ServerConfiguration getDatagramChannelConfig() {
		return datagramChannelConfig;
	}

	public void setDatagramChannelConfig(ServerConfiguration datagramChannelConfig) {
		this.datagramChannelConfig = datagramChannelConfig;
	}
	

}
