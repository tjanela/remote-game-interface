package com.blissapplications.java.remotegameinterface.socketworkers;

import com.blissapplications.java.remotegameinterface.clientconnections.IClientConnectionDelegate;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:08 AM
 */
public abstract class ServerSocketWorker implements  Runnable, IClientConnectionDelegate {
	public abstract void exit();
}
