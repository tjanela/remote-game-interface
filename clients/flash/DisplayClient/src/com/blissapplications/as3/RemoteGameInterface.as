﻿package com.blissapplications.as3 {	  import flash.events.TimerEvent;	import flash.utils.Timer;	import flash.system.Security;  import flash.events.EventDispatcher;	import flash.events.Event;		import com.junkbyte.console.Cc;	import flash.utils.ByteArray;			public class RemoteGameInterface extends EventDispatcher implements IRemoteGameInterfaceSocketDelegate {		public static var STATE_DISCONNECTED:String = "RGI:STATE_DISCONNECTED"; 		public static var STATE_CONNECTED:String 		= "RGI:STATE_CONNECTED"; 		public static var STATE_REGISTERED:String 	= "RGI:STATE_REGISTERED"; 		public static var STATE_HANDSHAKED:String 	= "RGI:STATE_HANDSHAKED"; 		public static var STATE_ACTIVATED:String 		= "RGI:STATE_ACTIVATED"; 		public static var STATE_DEACTIVATED:String 	= "RGI:STATE_DEACTIVATED"; 				public static var ACTION_CONNECT:String = "RGI:ACTION_CONNECT";		public static var ACTION_REGISTER:String = "RGI:ACTION_REGISTER";		public static var ACTION_HANDSHAKE:String = "RGI:ACTION_HANDSHAKE";		public static var ACTION_ACTIVATE:String = "RGI:ACTION_ACTIVATE";		public static var ACTION_SCORE:String = "RGI:ACTION_SCORE";		public static var ACTION_CONTROL_DATA:String = "RGI:ACTION_CONTROL_DATA";		public static var ACTION_DEACTIVATE:String = "RGI:ACTION_DEACTIVATE";		public static var ACTION_REPLAY:String = "RGI:ACTION_REPLAY";		public static var ACTION_UNREGISTER:String = "RGI:ACTION_UNREGISTER";		public static var ACTION_DISCONNECT:String = "RGI:ACTION_DISCONNECT";		var _state:String;		var _host:String;		var _port:int;		var _policyPort:int;				var _socket:RemoteGameInterfaceSocket;		var _connectTimer:Timer;				var _hash:String;		public function RemoteGameInterface(host:String, port:int) {			_host = host;			_port = port;			_policyPort = port + 1;		}				public function executeAction(action:String, data:Object = null):void{			Cc.log("Executing Action '" +action + "'...");			switch(action){				case ACTION_CONNECT:				{					connectToServer();					break;				}				case ACTION_REGISTER:				{					registerWithServer();					break;				}				case ACTION_HANDSHAKE:				{					respondToHandshake();					break;				}				case ACTION_ACTIVATE:				{					sendActivateToken();					break;				}				case ACTION_SCORE:				{					sendScore( int(data));					break;				}				case ACTION_CONTROL_DATA:				{					dispatchControl(data);					break;				}				case ACTION_DEACTIVATE:				{					sendFinish(int(data));					break;				}				case ACTION_REPLAY:				{					dispatchReplay(data)					break;				}				case ACTION_UNREGISTER:				{					unregisterWithServer();					break;				}				case ACTION_DISCONNECT:				{					disconnectFromServer();					break;				}			}		}				//Socket Connect and Policy stuff Hack				private function connectToServer(){			Cc.log("Connecting to Policy Server '" + _host + ":" + _policyPort + "'...");			Security.loadPolicyFile("xmlsocket://" + _host + ":" + _policyPort);						_connectTimer = new Timer(1000,5);			_connectTimer.start();			_connectTimer.addEventListener(TimerEvent.TIMER, timerHandler);      _connectTimer.addEventListener(TimerEvent.TIMER_COMPLETE, completeHandler);		}				private function disconnectFromServer(){			Cc.log("Disconnecting from Remote Interface Server...");						_socket.close();		}						private function resetState(){			Cc.log("Resetting state...");			disconnectFromServer();			_state = STATE_DISCONNECTED;		}				private function timerHandler(e:TimerEvent):void{    	Cc.log("Trying to connect to Remote Interface Server '" + _host + ":" + _port + "'...");			_socket = new RemoteGameInterfaceSocket();			_socket.socketDelegate = this;			try{				_socket.connect(_host,_port);				_connectTimer.stop();			}catch(e:Error){				Cc.log("Coulnd't connect. Error: '" + e + "'.");			}			Cc.log("trying to connect");    }    private function completeHandler(e:TimerEvent):void {    	Cc.log("Couldn't connect to Remote Interface Server. Giving up...");		}				//RemoteGameInterfaceProtocol				private function registerWithServer():void{			var packet:RemoteGameInterfacePacket = new RemoteGameInterfacePacket(RemoteGameInterfacePacket.REQUEST_REGISTER_DISPLAY_CLIENT);			_socket.sendPacket(packet);		}				private function unregisterWithServer():void{			var payload:ByteArray = new ByteArray();			payload.writeMultiByte(_hash,"UTF-8");			var packet:RemoteGameInterfacePacket = new RemoteGameInterfacePacket(RemoteGameInterfacePacket.REQUEST_UNREGISTER_DISPLAY_CLIENT, payload);			_socket.sendPacket(packet);		}				private function respondToHandshake():void{			var handshake:ByteArray = new ByteArray();			handshake.writeMultiByte("OLEH","UTF-8");			var packet:RemoteGameInterfacePacket = new RemoteGameInterfacePacket(RemoteGameInterfacePacket.RESPONSE_PAYLOAD, handshake);			packet.sendToSocket(_socket);		}				private function sendActivateToken():void{			sendScore(0);		}				private function sendScore(score:int):void{			var payload:ByteArray = new ByteArray();			payload.writeMultiByte("SCORE:"+score,"UTF-8");			var activateToken:RemoteGameInterfacePacket = new RemoteGameInterfacePacket(RemoteGameInterfacePacket.REQUEST_PAYLOAD,payload);			activateToken.sendToSocket(_socket);		}				private function dispatchControl(data:Object):void{					}				private function dispatchReplay(data:Object):void{					}				private function sendFinish(score:int){			var payload:ByteArray = new ByteArray();			payload.writeMultiByte("FINISH:"+score,"UTF-8");			var finishToken:RemoteGameInterfacePacket = new RemoteGameInterfacePacket(RemoteGameInterfacePacket.REQUEST_PAYLOAD,payload);			finishToken.sendToSocket(_socket);		}				//IRemoteGameInterfaceSocketDelegate				public function socketError(socket:RemoteGameInterfaceSocket, err:Error):void{			var newEvent:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.ERROR, err);			dispatchEvent(newEvent);		}				public function socketConnected(socket:RemoteGameInterfaceSocket):void{			Cc.log("Connected to Remote Interface Server server! Sending REQUEST_REGISTER_DISPLAY_CLIENT packet...");			var newEvent:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.CONNECTED);			dispatchEvent(newEvent);		}				public function socketDidReceivePacket(socket:RemoteGameInterfaceSocket, packet:RemoteGameInterfacePacket):void{			Cc.log("Received packet: " + packet);			if(packet._id == RemoteGameInterfacePacket.RESPONSE_REGISTER_DISPLAY_CLIENT){				var newEvent1:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.REGISTERED, packet._payload);				dispatchEvent(newEvent1);			}			else if(packet._id == RemoteGameInterfacePacket.RESPONSE_REGISTER_CONTROL_CLIENT){				var newEvent2:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.CONTROL_CLIENT_REGISTERED, packet._payload);				dispatchEvent(newEvent2);			}			else if(packet._id == RemoteGameInterfacePacket.RESPONSE_UNREGISTER_CONTROL_CLIENT){				var newEvent3:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.CONTROL_CLIENT_UNREGISTERED, packet._payload);				dispatchEvent(newEvent3);			}			else if(packet._id == RemoteGameInterfacePacket.REQUEST_PAYLOAD){				var payloadAsString:String = packet._payload.readUTF();				if(payloadAsString == "HELO"){					executeAction(ACTION_HANDSHAKE,payloadAsString);					var newEvent4:RemoteGameInterfaceEvent = new RemoteGameInterfaceEvent(RemoteGameInterfaceEvent.HANDSHAKED, packet._payload);					dispatchEvent(newEvent4);				}				else if(payloadAsString.search("CONTROL:") != 0){					executeAction(ACTION_CONTROL_DATA,payloadAsString);				}				else if(payloadAsString.search("PLAY:") != 0){					executeAction(ACTION_REPLAY,payloadAsString);				}			}		}			}	}