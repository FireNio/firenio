package com.generallycloud.nio.container.http11;

import com.generallycloud.nio.container.authority.AuthorityContext;
import com.generallycloud.nio.container.authority.SYSTEMAuthorityServlet;

public class HttpAuthorityContext extends AuthorityContext {

	protected SYSTEMAuthorityServlet createSYSTEMAuthorityServlet() {
		return new HttpAuthorityServlet();
	}

}
