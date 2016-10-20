package com.likemessage.server;

import java.sql.SQLException;

import com.generallycloud.nio.common.database.DataBaseContext;
import com.generallycloud.nio.common.database.SessionQuery;

public abstract class AbstractService {

	protected SessionQuery	query	;
	
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
