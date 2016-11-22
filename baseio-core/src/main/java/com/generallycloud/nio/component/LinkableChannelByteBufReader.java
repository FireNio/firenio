package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;

public abstract class LinkableChannelByteBufReader implements ChannelByteBufReader{

	private Linkable<ChannelByteBufReader> next;
	
	public Linkable<ChannelByteBufReader> getNext() {
		return next;
	}

	public void setNext(Linkable<ChannelByteBufReader> next) {
		this.next = next;
	}

	public ChannelByteBufReader getValue() {
		return this;
	}
	
}
