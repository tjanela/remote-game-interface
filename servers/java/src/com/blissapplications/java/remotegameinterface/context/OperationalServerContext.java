package com.blissapplications.java.remotegameinterface.context;

import com.blissapplications.java.remotegameinterface.clientconnections.IClientConnection;
import com.blissapplications.java.remotegameinterface.packets.OperationalProtocolPacket;
import com.sun.tools.javac.util.Pair;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 3:47 AM
 */

public class OperationalServerContext {
	private enum ClientRegistrationReturnCode{
		ClientRegistrationSucceeded,
		ClientRegistrationFailed_AlreadyRegisteredClient,
		ClientRegistrationFailed_UnknownHash
	}
	
	private static class DisplayClientRegistrationReturnObject{
		public ClientRegistrationReturnCode returnCode;
		public OperationalServerHash hash;
		
		public static DisplayClientRegistrationReturnObject returnObject(ClientRegistrationReturnCode returnCode, OperationalServerHash hash){
			DisplayClientRegistrationReturnObject returnObject = new DisplayClientRegistrationReturnObject();
			returnObject.returnCode = returnCode;
			returnObject.hash = hash;
			return returnObject;
		}
	}
	
	private static Logger _logger;
	public List<IClientConnection> _displayClients;
	public List<IClientConnection> _controlClients;

	public Map<OperationalServerHash,Pair<IClientConnection,IClientConnection>> _hashToPairMap;
	public Map<IClientConnection,OperationalServerHash> _controlClientHashes;
	public Map<IClientConnection,OperationalServerHash> _displayClientHashes;

	private static OperationalServerContext _operationalServerContextInstance;

	public ByteBuffer handleClientRequest(IClientConnection client, ByteBuffer rawRequest) throws Exception{

		OperationalProtocolPacket request = OperationalProtocolPacket.decode(rawRequest);
		OperationalProtocolPacket response = null;

		switch (request._packetType){
		case RegisterDisplayClientRequest:
		{
			DisplayClientRegistrationReturnObject returnObject = registerDisplayClient(client);
			 if(returnObject.returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationSucceeded)){
				 response = OperationalProtocolPacket.getRegisterDisplayClientResponse(returnObject.hash);
			 }else if (returnObject.returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient)){
				 response = OperationalProtocolPacket.getRegisterDisplayClientErrorResponse(OperationalProtocolPacket.ERROR_ALREADY_REGISTERED_PAYLOAD);
			 }else if(returnObject.returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationFailed_UnknownHash)){
				 response = OperationalProtocolPacket.getRegisterDisplayClientErrorResponse(OperationalProtocolPacket.ERROR_UNKNOWN_HASH_PAYLOAD);
			 }else {
				 response = OperationalProtocolPacket.getRegisterDisplayClientErrorResponse(OperationalProtocolPacket.ERROR_GENERIC_PAYLOAD);
			 }
			break;
		}
		case UnregisterDisplayClientRequest:
		{
			OperationalServerHash hash = _displayClientHashes.get(client);
			IClientConnection controlClient = getControlClient(hash);
			response = OperationalProtocolPacket.getUnregisterDisplayClientResponse(hash);
			if(controlClient != null){
				controlClient.writeData(OperationalProtocolPacket.encode(response));
			}
			break;
		}
		case RegisterControlClientRequest:
		{
			OperationalServerHash hash = OperationalServerHash.fromByteArray(request.getPayload());
			ClientRegistrationReturnCode returnCode = registerControlClient(hash, client);
			if(returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationSucceeded)){
				 response = OperationalProtocolPacket.getRegisterControlClientResponse(hash);
			 }else if (returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient)){
				 response = OperationalProtocolPacket.getRegisterControlClientErrorResponse(OperationalProtocolPacket.ERROR_ALREADY_REGISTERED_PAYLOAD);
			 }else if (returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationFailed_UnknownHash)){
				 response = OperationalProtocolPacket.getRegisterControlClientErrorResponse(OperationalProtocolPacket.ERROR_UNKNOWN_HASH_PAYLOAD);
			 }else {
				 response = OperationalProtocolPacket.getRegisterDisplayClientErrorResponse(OperationalProtocolPacket.ERROR_GENERIC_PAYLOAD);
			 }
			IClientConnection displayClient = getDisplayClient(hash);
			if(displayClient != null && returnCode.equals(ClientRegistrationReturnCode.ClientRegistrationSucceeded)){
				displayClient.writeData(OperationalProtocolPacket.encode(response));
			}
			break;
		}
		case UnregisterControlClientRequest:
		{
			OperationalServerHash hash = _controlClientHashes.get(client);
			IClientConnection displayClient = getDisplayClient(hash);
			response = OperationalProtocolPacket.getUnregisterControlClientResponse(hash);
			if(displayClient != null){
				displayClient.writeData(OperationalProtocolPacket.encode(response));
			}
			break;
		}
		case PayloadRequest:
		{
			if(isDisplayClient(client)){
				
				OperationalServerHash hash = _displayClientHashes.get(client);
				IClientConnection controlClient = getControlClient(hash);
				//response = OperationalProtocolPacket.getPayloadResponse();
				
				
				if(controlClient != null){
					getLogger().debug(String.format("Payload Request from DisplayClient[%1$s]: %2$s", hash,request.toString()));
					controlClient.writeData(OperationalProtocolPacket.encode(request));
				}else {
          getLogger().debug(String.format("Payload Request from DisplayClient[%1$s] %2$s has no ControlClient", hash, request.toString()));
				}
			}
			else if(isControlClient(client)){
				OperationalServerHash hash = _controlClientHashes.get(client);
				IClientConnection displayClient = getDisplayClient(hash);
				//response = OperationalProtocolPacket.getPayloadResponse();
				if(displayClient != null){
          getLogger().debug(String.format("Payload Request from ControlClient[%1$s]: %2$s", hash, request.toString()));
					displayClient.writeData(OperationalProtocolPacket.encode(request));
				}else {
          getLogger().debug(String.format("Payload Request from ControlClient[%1$s] %2$s has no DisplayClient", hash, request.toString()));
				}
			}
			else{
        getLogger().info("Client with payload request is not known ...");
			}
			break;
		}
		case RegisterDisplayClientResponse:
		case UnregisterDisplayClientResponse:
		case RegisterControlClientResponse:
		case UnregisterControlClientResponse:
		case PayloadResponse:
			break;
		case UnknownPacket:
			break;
		default:
			break;
		}

		return response != null ? OperationalProtocolPacket.encode(response) : null;
	}

	public void handleClientDisconnection(IClientConnection client) throws Exception{
		
		if(isControlClient(client)){
			OperationalServerHash hash = _controlClientHashes.get(client);
      getLogger().info("Handling control client disconnection...");
			if(hash == null){
				throw new Exception("Client not registered!");
			}
			
			IClientConnection displayClient = getDisplayClient(hash);
			
			if(displayClient != null){
				OperationalProtocolPacket packet = OperationalProtocolPacket.getUnregisterDisplayClientRequest(hash);
				displayClient.writeData(OperationalProtocolPacket.encode(packet));
			}
			else {
        getLogger().info("No Display client on handleClientDisconnect for hash: " + hash);
			}
			
			//Remove client from internal data
			_controlClientHashes.remove(hash);
			_controlClients.remove(client);
			
			Pair<IClientConnection, IClientConnection> pair = _hashToPairMap.get(hash);
			pair = Pair.of(pair.fst, null);
			_hashToPairMap.put(hash, pair);
			
		}else if(isDisplayClient(client)){
      getLogger().info("Handling display client disconnection...");
			OperationalServerHash hash = _displayClientHashes.get(client);
			
			if(hash == null){
				throw new Exception("Client not registered!");
			}
			
			IClientConnection controlClient = getControlClient(hash);
			if(controlClient != null){
				OperationalProtocolPacket packet = OperationalProtocolPacket.getUnregisterControlClientRequest(hash);
				controlClient.writeData(OperationalProtocolPacket.encode(packet));
			}
			else {
        getLogger().info("No Control client on handleClientDisconnect for hash: " + hash);
			}
			
			//remove client from control data
			
			_displayClientHashes.remove(hash);
			_displayClients.remove(client);
			
			Pair<IClientConnection, IClientConnection> pair = _hashToPairMap.get(hash);
			pair = Pair.of(null, pair.snd);
			_hashToPairMap.put(hash, pair);
		}else {
      getLogger().info("Disconnect from unknown client...");
		}
	}

	public DisplayClientRegistrationReturnObject registerDisplayClient(IClientConnection client) throws Exception{
		synchronized (this){
			if(!_displayClients.contains(client)){
				OperationalServerHash hash = OperationalServerHash.newHash();

				if(_hashToPairMap.containsKey(hash)){
          getLogger().info(String.format("Map already contains hash: '%1$s'. Setting anyway ",hash));
					Pair<IClientConnection,IClientConnection> pair = _hashToPairMap.get(hash);
					
					if(pair.fst == null){
						pair = Pair.of(client,pair.snd);
						_hashToPairMap.put(hash,pair);

						_displayClientHashes.put(client,hash);
						_displayClients.add(client);
						
						return DisplayClientRegistrationReturnObject.returnObject(ClientRegistrationReturnCode.ClientRegistrationSucceeded, hash);
					}else {
						return DisplayClientRegistrationReturnObject.returnObject(ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient, null);
					}
				}else{
          getLogger().info(String.format("Registering display client with hash %1$s",hash));
					Pair<IClientConnection,IClientConnection> pair = Pair.of(client,null);
					_hashToPairMap.put(hash,pair);

					_displayClientHashes.put(client,hash);
					_displayClients.add(client);
					return DisplayClientRegistrationReturnObject.returnObject(ClientRegistrationReturnCode.ClientRegistrationSucceeded, hash);
				}

			}
			else{
				return DisplayClientRegistrationReturnObject.returnObject(ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient, null);
			}
		}
	}
	public ClientRegistrationReturnCode registerControlClient(OperationalServerHash hash, IClientConnection client){
		synchronized (this){
      getLogger().info(String.format("Registering control client with hash %1$s",hash));
			if(!_controlClients.contains(client)){
				if(_hashToPairMap.containsKey(hash)){
					Pair<IClientConnection,IClientConnection> pair = _hashToPairMap.get(hash);
					if(pair.snd == null){
						pair = Pair.of(pair.fst,client);
						_hashToPairMap.put(hash,pair);

						_controlClientHashes.put(client,hash);
						_controlClients.add(client);
						
						return ClientRegistrationReturnCode.ClientRegistrationSucceeded;
					}
					else{
						return ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient;
					}
				}else{
          getLogger().warn(String.format("Asking to register a control client with no display client. Hash is %1$s. Returning error...", hash));
					return ClientRegistrationReturnCode.ClientRegistrationFailed_UnknownHash;
				}
			}
			else{
        getLogger().warn(String.format("Asking to register an already registered control client. Hash is %1$s. Returning error...",hash));
				return ClientRegistrationReturnCode.ClientRegistrationFailed_AlreadyRegisteredClient;
			}
		}
	}

	private IClientConnection getControlClient(OperationalServerHash hash){
		Pair<IClientConnection, IClientConnection> thePair = _hashToPairMap.get(hash); 
		return thePair != null ? thePair.snd : null;
	} 
	private IClientConnection getDisplayClient(OperationalServerHash hash){
		Pair<IClientConnection, IClientConnection> thePair = _hashToPairMap.get(hash); 
		return thePair != null ? thePair.fst : null;
	} 

	private Boolean isControlClient(IClientConnection connection){
		return _controlClients.contains(connection);
	}
	private Boolean isDisplayClient(IClientConnection connection){
		return _displayClients.contains(connection);
	}

	/*
	 * Singleton
	 */
	private OperationalServerContext(){
		_displayClients = new ArrayList<IClientConnection>();
		_controlClients = new ArrayList<IClientConnection>();
		_displayClientHashes = new HashMap<IClientConnection,OperationalServerHash>();
		_controlClientHashes = new HashMap<IClientConnection,OperationalServerHash>();
		_hashToPairMap = new HashMap<OperationalServerHash,Pair<IClientConnection,IClientConnection>>();
	}
	public static OperationalServerContext getOperationalServerContextInstance(){
		if(_operationalServerContextInstance == null){
			_operationalServerContextInstance = new OperationalServerContext();
		}
		return _operationalServerContextInstance;
	}

  private static Logger getLogger(){
    if(_logger == null){
      _logger = Logger.getLogger(OperationalServerContext.class);
    }
    return _logger;
  }
}
