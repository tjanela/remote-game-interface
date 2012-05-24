package com.blissapplications.java.remotegameinterface.engine;

public interface IRemoteGameInterfaceEngineDelegate 
{
	void didConfigure();
	void didNotConfigure(RemoteGameInterfaceError reason);
	void didConnect();
	void didCheckState();
	void didNotConnect(Exception ex);
	void didDisconnect(Exception ex);
	void didRegister();
	void didNotRegister(Exception ex);
	
	void didReceiveHandshakeResponse();
	void didReceiveScore(float score);
	void didReceiveFinish(float score);
}
