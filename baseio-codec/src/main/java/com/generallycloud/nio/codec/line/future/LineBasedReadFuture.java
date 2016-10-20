package com.generallycloud.nio.codec.line.future;

import java.nio.charset.Charset;

import com.generallycloud.nio.component.BufferedOutputStream;

public interface LineBasedReadFuture {

	public static final byte	LINE_BASE	= '\n';

	public abstract String getText();

	public abstract String getText(Charset encoding);

	public abstract BufferedOutputStream getOutputStream();
}
