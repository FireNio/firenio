package com.generallycloud.nio.protocol;

public interface TextReadFuture extends ReadFuture{
	
	public abstract String getReadText();
	
	public abstract String getWriteText();
	
	public abstract StringBuilder getWriteTextBuffer();
	
	public abstract void write(String text);
	
	public abstract void write(char c);
	
	public abstract void write(boolean b);
	
	public abstract void write(int i);
	
	public abstract void write(long l);
	
	public abstract void write(double d);
}
