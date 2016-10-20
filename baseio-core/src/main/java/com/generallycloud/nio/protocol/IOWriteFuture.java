package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.IOSession;

public interface IOWriteFuture extends WriteFuture ,Linkable<IOWriteFuture> {

	public abstract boolean write() throws IOException;

	public abstract IOSession getIOSession();

	public IOWriteFuture duplicate(IOSession session);

	public abstract void onException(Exception e);

	public abstract void onSuccess();
}
