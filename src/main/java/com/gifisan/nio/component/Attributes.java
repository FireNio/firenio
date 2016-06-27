package com.gifisan.nio.component;

import java.util.Set;

import com.gifisan.nio.Attachment;

public abstract interface Attributes extends Attachment{
	  
	  public abstract Object removeAttribute(Object key);
	  
	  public abstract void setAttribute(Object key, Object value);
	  
	  public abstract Object getAttribute(Object key);
	  
	  public abstract Set<Object> getAttributeNames();
	  
	  public abstract void clearAttributes();


}