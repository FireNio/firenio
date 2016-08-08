package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

import com.gifisan.nio.Looper;

public interface SelectorLoop extends SelectionAcceptor, Looper {

	public abstract void register(NIOContext context, SelectableChannel channel) throws IOException;
}
