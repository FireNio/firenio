package com.generallycloud.nio;

import java.util.Set;

public abstract interface Attributes{
	  
	  public abstract Object removeAttribute(Object key);
	  
	  public abstract void setAttribute(Object key, Object value);
	  
	  public abstract Object getAttribute(Object key);
	  
	  public abstract Set<Object> getAttributeNames();
	  
	  public abstract void clearAttributes();


}