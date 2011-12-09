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
	private static Logger _logger = Logger.getLogger(OperationalServerContext.class);
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
			OperationalServerHash hash = registerDisplayClient(client);
			response = OperationalProtocolPacket.getRegisterDisplayClientResponse(hash);
			break;
		}
		case UnregisterDisplayClientRequest:
		{
			OperationalServerHash hash = _displayClientHashes.get(client);
			IClientConnection controlClient = getControlClient(hash);
			response = OperationalProtocolPacket.getUnregisterDisplayClientResponse(hash);
			controlClient.writeData(OperationalProtocolPacket.encode(response));
			break;
		}
		case RegisterControlClientRequest:
		{
			OperationalServerHash hash = OperationalServerHash.fromByteArray(request.getPayload());
			registerControlClient(hash, client);
			response = OperationalProtocolPacket.getRegisterControlClientResponse(hash);
			IClientConnection displayClient = getDisplayClient(hash);
			displayClient.writeData(OperationalProtocolPacket.encode(response));
			break;
		}
		case UnregisterControlClientRequest:
		{
			OperationalServerHash hash = _controlClientHashes.get(client);
			IClientConnection displayClient = getDisplayClient(hash);
			response = OperationalProtocolPacket.getUnregisterControlClientResponse(hash);
			displayClient.writeData(OperationalProtocolPacket.encode(response));
			break;
		}
		case PayloadRequest:
		{
			if(isDisplayClient(client)){
				OperationalServerHash hash = _displayClientHashes.get(client);
				IClientConnection controlClient = getControlClient(hash);
				response = OperationalProtocolPacket.getPayloadResponse();
				controlClient.writeData(OperationalProtocolPacket.encode(request));
			}
			else if(isControlClient(client)){
				OperationalServerHash hash = _controlClientHashes.get(client);
				IClientConnection displayClient = getDisplayClient(hash);
				response = OperationalProtocolPacket.getPayloadResponse();
				displayClient.writeData(OperationalProtocolPacket.encode(request));
			}
			else{
				_logger.info("Huchy mama...");
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

		return OperationalProtocolPacket.encode(response);
	}

	public void handleClientDisconnection(IClientConnection client) throws Exception{
		if(isControlClient(client)){
			OperationalServerHash hash = _controlClientHashes.get(client);
			
			if(hash == null){
				throw new Exception("Client not registered!");
			}
			
			IClientConnection displayClient = getDisplayClient(hash);
			OperationalProtocolPacket packet = OperationalProtocolPacket.getUnregisterDisplayClientRequest(hash);
			displayClient.writeData(OperationalProtocolPacket.encode(packet));
		}else if(isDisplayClient(client)){
			OperationalServerHash hash = _displayClientHashes.get(client);
			
			if(hash == null){
				throw new Exception("Client not registered!");
			}
			
			IClientConnection controlClient = getControlClient(hash);
			OperationalProtocolPacket packet = OperationalProtocolPacket.getUnregisterControlClientRequest(hash);
			controlClient.writeData(OperationalProtocolPacket.encode(packet));
		}else {
			_logger.info("Disconnect from unknown client...");
		}
	}

	public OperationalServerHash registerDisplayClient(IClientConnection client) throws Exception{
		synchronized (this){
			if(!_displayClients.contains(client)){
				OperationalServerHash hash = OperationalServerHash.newHash();

				if(_hashToPairMap.containsKey(hash)){
					_logger.info(String.format("Map already contains hash: '%1$s'. Setting anyway ",hash));
					Pair<IClientConnection,IClientConnection> pair = _hashToPairMap.get(hash);
					pair = Pair.of(client,pair.snd);
					_hashToPairMap.put(hash,pair);
				}else{
					_logger.info(String.format("Registering display client with hash %1$s",hash));
					Pair<IClientConnection,IClientConnection> pair = Pair.of(client,null);
					_hashToPairMap.put(hash,pair);
				}

				_displayClientHashes.put(client,hash);
				_displayClients.add(client);
				return hash;

			}
			else{
				throw new Exception("Already registered display client");
			}
		}
	}
	public void registerControlClient(OperationalServerHash hash, IClientConnection client){
		synchronized (this){
			_logger.info(String.format("Registering control client with hash %1$s",hash));
			if(!_controlClients.contains(client)){
				if(_hashToPairMap.containsKey(hash)){
					Pair<IClientConnection,IClientConnection> pair = _hashToPairMap.get(hash);
					pair = Pair.of(pair.fst,client);
					_hashToPairMap.put(hash,pair);
				}else{
					_logger.warn(String.format("Asking to register a control client with no display client. Hash is %1$s Proceeding...", hash));
					Pair<IClientConnection,IClientConnection> pair = Pair.of(null,client);
					_hashToPairMap.put(hash,pair);
				}
				_controlClientHashes.put(client,hash);
				_controlClients.add(client);
			}
			else{
				_logger.warn(String.format("Asking to register an already registered control client. Hash is %1$s. Proceeding...",hash));
			}
		}
	}

	private IClientConnection getControlClient(OperationalServerHash hash){
		return _hashToPairMap.get(hash).snd;
	} 
	private IClientConnection getDisplayClient(OperationalServerHash hash){
		return _hashToPairMap.get(hash).fst;
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
}
