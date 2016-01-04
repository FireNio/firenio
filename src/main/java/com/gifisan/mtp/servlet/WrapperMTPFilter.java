package com.gifisan.mtp.servlet;

import com.gifisan.mtp.LifeCycle;

public interface WrapperMTPFilter extends LifeCycle , MTPFilter{

	public abstract WrapperMTPFilter nextFilter();
	
	public abstract void setNextFilter(WrapperMTPFilter filter);
	
}
