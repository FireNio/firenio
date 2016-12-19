package com.generallycloud.nio.buffer;

public class ByteBufUnit {

	protected int		index		= 0;
	protected int		blockBegin	= 0;
	protected int		blockEnd		= 0;
	protected boolean	free			= true;
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append("index=")
			.append(index)
			.append(",free=")
			.append(free)
			.append(",begin=")
			.append(blockBegin)
			.append(",end=")
			.append(blockEnd)
			.toString();
	}
}
