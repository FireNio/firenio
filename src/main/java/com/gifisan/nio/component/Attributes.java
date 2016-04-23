package com.gifisan.nio.component;

import java.util.Set;

import com.gifisan.nio.Attachment;

public abstract interface Attributes extends Attachment{
	  
	  public abstract Object removeAttribute(String key);
	  
	  public abstract void setAttribute(String key, Object value);
	  
	  public abstract Object getAttribute(String key);
	  
	  public abstract Set<String> getAttributeNames();
	  
	  public abstract void clearAttributes();


}