//
//  RGIRemoteGameInterface.m
//  RGIRemoteGameInterface
//
//  Created by Tiago Janela on 12/3/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import "RGIRemoteGameInterface.h"

#define RGI_SOCKET_TIMEOUT 2

#define RGI_REGISTER_CONTROL_CLIENT_TAG kRGIPacket_RegisterControlClientRequest
#define RGI_REGISTER_DISPLAY_CLIENT_TAG kRGIPacket_RegisterDisplayClientRequest

#define RGI_SEND_PAYLOAD_TAG kRGIPacket_PayloadRequest

@implementation RGIRemoteGameInterface


- (void) addDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate{
	[_delegates addObject:delegate];
}

- (void) removeDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate{
	[_delegates removeObject:delegate];
}

- (void) registerWithHash:(NSString*)hash{
	RGIPacket *packet = [RGIPacket packetRequestRegisterControlClient:hash];
	NSData *packetData = [packet data];
	[_clientSocket writeData:packetData withTimeout:RGI_SOCKET_TIMEOUT tag:RGI_REGISTER_CONTROL_CLIENT_TAG];
}

- (void) register{
	RGIPacket *packet = [RGIPacket packetRequestRegisterDisplayClient];
	NSData *packetData = [packet data];
	[_clientSocket writeData:packetData withTimeout:RGI_SOCKET_TIMEOUT tag:RGI_REGISTER_DISPLAY_CLIENT_TAG];
}

- (void) sendPayload:(NSData*)payload{
	RGIPacket *packet = [RGIPacket packetRequestPayload:payload];
	NSData *packetData = [packet data];
	[_clientSocket writeData:packetData withTimeout:RGI_SOCKET_TIMEOUT tag:RGI_SEND_PAYLOAD_TAG];
}

- (void) sendStringPayload:(NSString*)payload
{
	[self sendPayload:[payload dataUsingEncoding:NSUTF8StringEncoding]];
}

- (void)connectToServer:(NSString*)host port:(NSNumber*)port andClientType:(rgiClientType)clientType{
	if(_clientSocket != nil){
		[_clientSocket disconnect];
		[_clientSocket release];
	}
	
	_clientType = clientType;
	
	_clientSocket = [[GCDAsyncSocket alloc]initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
	NSError *error = nil;
	[_clientSocket connectToHost:host onPort:[port intValue] withTimeout:5 error:&error];
	
	if(error != nil){
		NSLog(@"Error while connecting: %@", error);
		for (id<RGIRemoteGameInterfaceDelegate> delegate in _delegates) {
			[delegate remoteGameInterface:self didNotConnectWithError:error];
		}
	}
	
	[_clientSocket readDataToData:[RGIPacket magic] withTimeout:-1 maxLength:RGI_MAX_PACKET_SIZE tag:0];
}

- (void) disconnectFromServer{
	[_clientSocket disconnectAfterWriting];
	for (id<RGIRemoteGameInterfaceDelegate> delegate in _delegates) {
		
	}
}

#pragma mark - Control Client Packet Handling

- (void) handleControlClientPacket:(RGIPacket*)packet{
	if([RGIPacket typeFromData:packet.packetId] == kRGIPacket_RegisterControlClientResponse)
	{
		if(packet.payload == nil || [packet.payload length] == 0){
			for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
				[delegate remoteGameInterfaceControlClientDidNotRegister:self withError:kRGIError_ApplicationError];
			}
		}else {
			NSString *payload = [[[NSString alloc] initWithData:packet.payload encoding:NSUTF8StringEncoding]autorelease];
			if([@"UNKNOWN_HASH" isEqualToString:payload]){
				rgiError error = kRGIError_UnknownHash;	
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceControlClientDidNotRegister:self withError:error];
				}
			}
			else if([@"ERROR" isEqualToString:payload]){
				rgiError error = kRGIError_ApplicationError;	
				
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceControlClientDidNotRegister:self withError:error];
				}
			}else if([@"ALREADY_REGISTERED" isEqualToString:payload]){
				rgiError error = kRGIError_AlreadyUsedHash;	
				
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceControlClientDidNotRegister:self withError:error];
				}
			}else{
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceControlClientRegisteredSuccessfully:self];
				}	
			}
		}
	}
	
	if([RGIPacket typeFromData:packet.packetId] == kRGIPacket_UnregisterControlClientRequest){
		for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
			[delegate remoteGameInterfaceControlClientDisconnected:self];
		}	
	}
	
}

#pragma mark - Display Client Packet Handling

- (void) handleDisplayClientPacket:(RGIPacket*)packet
{
	rgiPacketType packetType = [RGIPacket typeFromData:packet.packetId];
	if(packetType == kRGIPacket_RegisterDisplayClientResponse){
		for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
			NSString *hash = [packet stringPayload];
			
			if(!hash || hash.length ==0){
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceDisplayClientDidNotRegister:self withError:kRGIError_ApplicationError];
				}
			}
			else 
			{
				for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
					[delegate remoteGameInterfaceDisplayClientRegisteredSuccessfully:self withHash:hash];
				}
			}
		}
	}else if(packetType == kRGIPacket_UnregisterDisplayClientRequest){
		for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
			[delegate remoteGameInterfaceDisplayClientDisconnected:self];
		}
	}
}

#pragma mark - GCDAsyncSocketDelegate

/**
 * This method is called immediately prior to socket:didAcceptNewSocket:.
 * It optionally allows a listening socket to specify the socketQueue for a new accepted socket.
 * If this method is not implemented, or returns NULL, the new accepted socket will create its own default queue.
 * 
 * Since you cannot autorelease a dispatch_queue,
 * this method uses the "new" prefix in its name to specify that the returned queue has been retained.
 * 
 * Thus you could do something like this in the implementation:
 * return dispatch_queue_create("MyQueue", NULL);
 * 
 * If you are placing multiple sockets on the same queue,
 * then care should be taken to increment the retain count each time this method is invoked.
 * 
 * For example, your implementation might look something like this:
 * dispatch_retain(myExistingQueue);
 * return myExistingQueue;
 **/
- (dispatch_queue_t)newSocketQueueForConnectionFromAddress:(NSData *)address onSocket:(GCDAsyncSocket *)sock{
	return NULL;
}

/**
 * Called when a socket accepts a connection.
 * Another socket is automatically spawned to handle it.
 * 
 * You must retain the newSocket if you wish to handle the connection.
 * Otherwise the newSocket instance will be released and the spawned connection will be closed.
 * 
 * By default the new socket will have the same delegate and delegateQueue.
 * You may, of course, change this at any time.
 **/
- (void)socket:(GCDAsyncSocket *)sock didAcceptNewSocket:(GCDAsyncSocket *)newSocket{
}

/**
 * Called when a socket connects and is ready for reading and writing.
 * The host parameter will be an IP address, not a DNS name.
 **/
- (void)socket:(GCDAsyncSocket *)sock didConnectToHost:(NSString *)host port:(uint16_t)port{
	for (id<RGIRemoteGameInterfaceDelegate> delegate in _delegates) {
		[delegate remoteGameInterfaceDidConnectSuccessfuly:self];
	}
}

/**
 * Called when a socket has completed reading the requested data into memory.
 * Not called if there is an error.
 **/
- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag{
	@synchronized(self){
		
		RGIPacket *packet = [RGIPacket decode:data];
		if(packet != nil){
			NSLog(@"Received packet: %@",packet);
			if(_clientType == kRGIClientType_Control){
				[self handleControlClientPacket:packet];
			}else if(_clientType == kRGIClientType_Display){
				[self handleDisplayClientPacket:packet];
			}else{
				NSAssert(false,@"Unknown Client Type! Failing early...");
			}
			for(id<RGIRemoteGameInterfaceDelegate> delegate in _delegates){
				[delegate remoteGameInterface:self didReceivePacket:packet];
			}
		}else {
			NSLog(@"Couldn't decode packet!");		
		}
		
		[_clientSocket readDataToData:[RGIPacket magic] withTimeout:-1 maxLength:RGI_MAX_PACKET_SIZE tag:tag];
	}
}

/**
 * Called when a socket has read in data, but has not yet completed the read.
 * This would occur if using readToData: or readToLength: methods.
 * It may be used to for things such as updating progress bars.
 **/
- (void)socket:(GCDAsyncSocket *)sock didReadPartialDataOfLength:(NSUInteger)partialLength tag:(long)tag{
	//NSLog(@"socket:didReadPartialDataOfLength:tag:");
}

/**
 * Called when a socket has completed writing the requested data. Not called if there is an error.
 **/
- (void)socket:(GCDAsyncSocket *)sock didWriteDataWithTag:(long)tag{
	//NSLog(@"socket:didWriteDataWithTag:withTag:");
}

/**
 * Called when a socket has written some data, but has not yet completed the entire write.
 * It may be used to for things such as updating progress bars.
 **/
- (void)socket:(GCDAsyncSocket *)sock didWritePartialDataOfLength:(NSUInteger)partialLength tag:(long)tag{
	//NSLog(@"socket:didWritePartialDataOfLength:tag:");
}

/**
 * Called if a read operation has reached its timeout without completing.
 * This method allows you to optionally extend the timeout.
 * If you return a positive time interval (> 0) the read's timeout will be extended by the given amount.
 * If you don't implement this method, or return a non-positive time interval (<= 0) the read will timeout as usual.
 * 
 * The elapsed parameter is the sum of the original timeout, plus any additions previously added via this method.
 * The length parameter is the number of bytes that have been read so far for the read operation.
 * 
 * Note that this method may be called multiple times for a single read if you return positive numbers.
 **/
- (NSTimeInterval)socket:(GCDAsyncSocket *)sock shouldTimeoutReadWithTag:(long)tag
								 elapsed:(NSTimeInterval)elapsed
							 bytesDone:(NSUInteger)length{
	//NSLog(@"socket:shouldTimeoutReadWithTag:elapsed:bytesDone:");
	return -1;
}

/**
 * Called if a write operation has reached its timeout without completing.
 * This method allows you to optionally extend the timeout.
 * If you return a positive time interval (> 0) the write's timeout will be extended by the given amount.
 * If you don't implement this method, or return a non-positive time interval (<= 0) the write will timeout as usual.
 * 
 * The elapsed parameter is the sum of the original timeout, plus any additions previously added via this method.
 * The length parameter is the number of bytes that have been written so far for the write operation.
 * 
 * Note that this method may be called multiple times for a single write if you return positive numbers.
 **/
- (NSTimeInterval)socket:(GCDAsyncSocket *)sock shouldTimeoutWriteWithTag:(long)tag
								 elapsed:(NSTimeInterval)elapsed
							 bytesDone:(NSUInteger)length{
	//NSLog(@"socket:shouldTimeoutWriteWithTag:elapsed:bytesDone:");
	return -1;
}

/**
 * Conditionally called if the read stream closes, but the write stream may still be writeable.
 * 
 * This delegate method is only called if autoDisconnectOnClosedReadStream has been set to NO.
 * See the discussion on the autoDisconnectOnClosedReadStream method for more information.
 **/
- (void)socketDidCloseReadStream:(GCDAsyncSocket *)sock{
	//NSLog(@"socketDidCloseReadStream:");
}

/**
 * Called when a socket disconnects with or without error.
 * 
 * If you call the disconnect method, and the socket wasn't already disconnected,
 * this delegate method will be called before the disconnect method returns.
 **/
- (void)socketDidDisconnect:(GCDAsyncSocket *)sock withError:(NSError *)err{
	//NSLog(@"socketDidDisconnect:withError:");
	if(err){
		for (id<RGIRemoteGameInterfaceDelegate> delegate in _delegates) {
			[delegate remoteGameInterface:self didNotConnectWithError:err];
		}
	}
	
	[_clientSocket release];
	_clientSocket = nil;
}

/**
 * Called after the socket has successfully completed SSL/TLS negotiation.
 * This method is not called unless you use the provided startTLS method.
 * 
 * If a SSL/TLS negotiation fails (invalid certificate, etc) then the socket will immediately close,
 * and the socketDidDisconnect:withError: delegate method will be called with the specific SSL error code.
 **/
- (void)socketDidSecure:(GCDAsyncSocket *)sock{
	//NSLog(@"socketDidSecure:");
}

#pragma mark - Lifecycle 

- (id)init
{
  self = [super init];
	if (self) {
		_delegates  = [[NSMutableSet alloc] init];
  }
	
  return self;
}

static RGIRemoteGameInterface* _sharedInterface;

+ (RGIRemoteGameInterface*) sharedInterface {
	if(!_sharedInterface){
		_sharedInterface = [NSAllocateObject([self class], 0, NULL) init];
	}
	return _sharedInterface;
}

@end
