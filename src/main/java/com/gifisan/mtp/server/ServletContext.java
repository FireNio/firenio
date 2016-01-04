package com.gifisan.mtp.server;

import java.nio.charset.Charset;

import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.server.session.MTPSessionFactory;

public interface ServletContext extends Attributes,LifeCycle{

	public abstract Charset getEncoding() ;

	public abstract void setEncoding(Charset encoding) ;
	
	public abstract MTPServer getServer() ;
	
	public abstract MTPSessionFactory getMTPSessionFactory();
	
	public abstract String getWebAppLocalAddress();
	
}