package com.likemessage.server;

import java.sql.SQLException;

import com.gifisan.database.DataBaseContext;
import com.gifisan.database.SessionQuery;

public abstract class AbstractService {

	protected SessionQuery	query	= null;
	
	protected AbstractService(DataBaseContext context) throws SQLException {
		this.query = new SessionQuery(context);
	}

	public void open() throws SQLException{
		this.query.open();
	}
	
	public void close(){
		this.query.close();
	}
}
