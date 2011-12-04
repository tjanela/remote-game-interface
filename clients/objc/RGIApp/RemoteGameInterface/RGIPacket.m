//
//  RGIPacket.m
//  RGIApp
//
//  Created by Tiago Janela on 12/3/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import "RGIPacket.h"

@implementation RGIPacket

@synthesize packetId = _packetId;
@synthesize payload = _payload;
@synthesize magic = _magic;

- (void)dealloc {
	[_packetId release];
	_packetId = nil;
	[_payload release];
	_payload = nil;
	[_magic release];
	_magic = nil;
	[super dealloc];
}

@end
