package com.gifisan.nio.server.service;

import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.component.Initializeable;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.server.FilterAcceptor;

public abstract class GenericServlet extends InitializeableImpl implements Initializeable, HotDeploy, FilterAcceptor {

}
