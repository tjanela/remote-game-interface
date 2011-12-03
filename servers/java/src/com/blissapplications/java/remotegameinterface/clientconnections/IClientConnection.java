package com.blissapplications.java.remotegameinterface.clientconnections;

import java.nio.ByteBuffer;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 2:51 AM
 */
public interface IClientConnection {
	public void serveClient() throws Exception;
	public void writeData(ByteBuffer data) throws Exception;
}
