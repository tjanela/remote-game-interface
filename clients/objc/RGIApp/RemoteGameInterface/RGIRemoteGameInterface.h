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

@class RGIRemoteGameInterface;

@protocol RGIRemoteGameInterfaceDelegate <NSObject>

- (void) remoteGameInterfaceDidConnectSuccessfuly:(RGIRemoteGameInterface*)remoteGameInterface;
- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didNotConnectWithError:(NSError*)error;

- (void) remoteGameInterfaceRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface;

- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didReceivePacket:(RGIPacket*)packet;

@end

@interface RGIRemoteGameInterface : NSObject
{
	NSMutableSet *_delegates;	
	GCDAsyncSocket *_clientSocket;
}

+ (RGIRemoteGameInterface*) sharedInterface;

- (void) addDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate;
- (void) removeDelegate:(id<RGIRemoteGameInterfaceDelegate>)delegate;

- (void) connectToServer;
- (void) registerWithHash:(NSString*)hash;
- (void) sendPayload:(NSData*)payload;

@end
