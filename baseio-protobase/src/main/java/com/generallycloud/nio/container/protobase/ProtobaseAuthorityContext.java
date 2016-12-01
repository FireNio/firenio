package com.generallycloud.nio.container.protobase;

import com.generallycloud.nio.container.authority.AuthorityContext;
import com.generallycloud.nio.container.authority.SYSTEMAuthorityServlet;

public class ProtobaseAuthorityContext extends AuthorityContext {

	protected SYSTEMAuthorityServlet createSYSTEMAuthorityServlet() {
		return new ProtobaseAuthorityServlet();
	}

}
