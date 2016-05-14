package com.gifisan.nio.server.service;

import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.component.Initializeable;
import com.gifisan.nio.server.FilterAcceptor;

public interface NIOFilter extends Initializeable, HotDeploy, FilterAcceptor {

}
