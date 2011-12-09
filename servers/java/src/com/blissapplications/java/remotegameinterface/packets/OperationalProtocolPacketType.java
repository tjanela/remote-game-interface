package com.blissapplications.java.remotegameinterface.packets;

import java.nio.ByteBuffer;

/**
 * User: tjanela
 * Date: 11/27/11
 * Time: 5:49 AM
 */
public enum OperationalProtocolPacketType {
	//Requests
	RegisterDisplayClientRequest	(0x010000),
	UnregisterDisplayClientRequest	(0x010001),
	RegisterControlClientRequest	(0x020000),
	UnregisterControlClientRequest	(0x020001),
	PayloadRequest					(0x030000),
	
	//Responses
	RegisterDisplayClientResponse	(0x01FFFF),
	UnregisterDisplayClientResponse	(0x01FFFE),
	RegisterControlClientResponse	(0x02FFFF),
	UnregisterControlClientResponse	(0x02FFFE),
	PayloadResponse					(0x03FFFF),

	UnknownPacket					(0x000000);

	private int _id;
	private OperationalProtocolPacketType(int id){
		_id = id;
	}

	public int getId(){
		return _id;
	}

	public static byte[] getTypeBytes(OperationalProtocolPacketType type){
		int id = type.getId();

		ByteBuffer idByteBuffer = ByteBuffer.allocate(OperationalProtocolPacket.ID_FIELD_LENGTH);
		idByteBuffer.put((byte)((id & 0x00FF0000) >> 16));
		idByteBuffer.put((byte)((id & 0x0000FF00) >> 8));
		idByteBuffer.put((byte)((id & 0x000000FF)));

		return idByteBuffer.array();
	}

	public static OperationalProtocolPacketType fromInt(int id){
		//Requests
		if(id == RegisterDisplayClientRequest.getId()){
			return RegisterDisplayClientRequest;
		}else if(id == UnregisterDisplayClientRequest.getId()){
			return UnregisterDisplayClientRequest;
		}else if(id == RegisterControlClientRequest.getId()){
			return RegisterControlClientRequest;
		}else if(id == UnregisterControlClientRequest.getId()){
			return UnregisterControlClientRequest;
		}else if((id & PayloadRequest.getId()) == PayloadRequest.getId()){
			return PayloadRequest;
		}
		//Responses
		else if(id == RegisterDisplayClientResponse.getId()){
			return RegisterDisplayClientResponse;
		}else if(id == UnregisterDisplayClientResponse.getId()){
			return UnregisterDisplayClientResponse;
		}else if(id == RegisterControlClientResponse.getId()){
			return RegisterControlClientResponse;
		}else if(id == UnregisterControlClientResponse.getId()){
			return UnregisterControlClientResponse;
		}else if((id & PayloadResponse.getId()) == PayloadResponse.getId()){
			return PayloadResponse;
		}
		return UnknownPacket;
	}

}
