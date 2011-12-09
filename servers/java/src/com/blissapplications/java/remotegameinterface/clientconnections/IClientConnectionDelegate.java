package com.blissapplications.java.remotegameinterface.clientconnections;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 2:57 AM
 */
public interface IClientConnectionDelegate {
	public void clientServed(IClientConnection servedClient);
	public void clientDisconnected(IClientConnection disconnectedClient);
}
