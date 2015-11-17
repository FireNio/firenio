package com.yoocent.mtp.server.context;

import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.server.MTPServer;
import com.yoocent.mtp.server.session.MTPSessionFactory;

public interface ServletContext extends LifeCycle{

	public abstract String getEncoding() ;

	public abstract void setEncoding(String encoding) ;
	
	public abstract MTPServer getServer() ;
	
	public abstract MTPSessionFactory getMTPSessionFactory();
	
	public abstract String getWebAppLocalAddress();
	
}