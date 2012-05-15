//
//  RGIRemoteGameInterface.h
//  RGIRemoteGameInterface
//
//  Created by Tiago Janela on 12/3/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "RGIPacket.h"
#import "GCDAsyncSocket.h"

typedef enum{
	kRGIClientType_Display,
	kRGIClientType_Control
}rgiClientType;

typedef enum 
{
	kRGIError_UnknownHash,
	kRGIError_AlreadyUsedHash,
	kRGIError_ApplicationError
} rgiError;

@class RGIRemoteGameInterface;

@protocol RGIRemoteGameInterfaceDelegate <NSObject>

- (void) remoteGameInterfaceDidConnectSuccessfuly:(RGIRemoteGameInterface*)remoteGameInterface;
- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didNotConnectWithError:(NSError*)error;

- (void) remoteGameInterfaceControlClientRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface;
- (void) remoteGameInterfaceControlClientDidNotRegister:(RGIRemoteGameInterface*)remoteGameInterface withError:(rgiError)error;
- (void) remoteGameInterfaceControlClientDisconnected:(RGIRemoteGameInterface *)remoteGameInterface;

- (void) remoteGameInterfaceDisplayClientRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface withHash:(NSString*)hash;
- (void) remoteGameInterfaceDisplayClientDidNotRegister:(RGIRemoteGameInterface*)remoteGameInterface withError:(rgiError)error;
- (void) remoteGameInterfaceDisplayClientDisconnected:(RGIRemoteGameInterface *)remoteGameInterface;

- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didReceivePacket:(RGIPacket*)packet;

@end

@interface RGIRemoteGameInterface : NSObject
{
	NSMutableSet *_delegates;	
	GCDAsyncSocket *_clientSocket;
	rgiClientType _clientType;
	BOOL _socketConnected;
}

+ (RGIRemoteGameInterface*) sharedInterface;

- (void) addDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate;
- (void) removeDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate;

- (void) connectToServer:(NSString*)host port:(NSNumber*)port andClientType:(rgiClientType)clientType;
- (void) disconnectFromServer;

- (void) registerWithHash:(NSString*)hash;
- (void) register;

- (void) sendPayload:(NSData*)payload;
- (void) sendStringPayload:(NSString*)payload;

@end
