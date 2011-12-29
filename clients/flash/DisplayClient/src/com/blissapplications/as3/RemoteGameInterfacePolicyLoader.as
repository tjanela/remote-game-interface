﻿package com.blissapplications.as3 {		import flash.net.Socket;	import flash.system.Security;	import flash.utils.Timer;	import flash.events.Event;	import flash.events.ProgressEvent;	import flash.events.IOErrorEvent;	import flash.events.SecurityErrorEvent;	import flash.utils.ByteArray;	import com.junkbyte.console.Cc;		public class RemoteGameInterfacePolicyLoader extends Socket {		public var _policyLoaderDelegate:IRemoteGameInterfacePolicyLoaderDelegate;				public function set policyLoaderDelegate(newPolicyLoaderDelegate:IRemoteGameInterfacePolicyLoaderDelegate):void 	{	       _policyLoaderDelegate = newPolicyLoaderDelegate;	}	public function get policyLoaderDelegate():IRemoteGameInterfacePolicyLoaderDelegate 	{	       return _policyLoaderDelegate;	}				public function RemoteGameInterfacePolicyLoader() {			configureListeners();		}				public function loadPolicy(host:String, port:int){			Cc.log("Connecting to " + host + ":" + port);			Security.loadPolicyFile("xmlsocket://" + host + ":" + port);			connect(host,port);		}				public function configureListeners():void{			Cc.log("Configuring policyLoader Listeners");			addEventListener(Event.CLOSE, socketCloseHandler);			addEventListener(Event.CONNECT, socketConnectHandler);			addEventListener(IOErrorEvent.IO_ERROR, socketIOErrorHandler);			addEventListener(ProgressEvent.SOCKET_DATA, socketDataHandler);			addEventListener(SecurityErrorEvent.SECURITY_ERROR, socketSecurityErrorHandler);		}				public function socketConnectHandler(e:Event):void{			Cc.log("Send dummy");			_policyLoaderDelegate.policyLoaded();		}				public function socketCloseHandler(e:Event):void{					}				public function socketDataHandler(e:ProgressEvent):void{			}				/* Error handlers */		public function socketIOErrorHandler(e:IOErrorEvent):void {    	Cc.log("Error '" + e + "'. Giving up...");			_policyLoaderDelegate.policyError(this,new Error(e.toString()));		}				public function socketSecurityErrorHandler(e:SecurityErrorEvent):void {    	//Cc.log("Security Error '" + e + "'. Giving up...");			_policyLoaderDelegate.policyError(this,new Error(e.toString()));		}	}	}