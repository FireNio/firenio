package com.gifisan.mtp.server.context;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.server.Attributes;
import com.gifisan.mtp.server.MTPServer;
import com.gifisan.mtp.server.session.MTPSessionFactory;

public interface ServletContext extends Attributes,LifeCycle{

	public abstract String getEncoding() ;

	public abstract void setEncoding(String encoding) ;
	
	public abstract MTPServer getServer() ;
	
	public abstract MTPSessionFactory getMTPSessionFactory();
	
	public abstract String getWebAppLocalAddress();
	
}