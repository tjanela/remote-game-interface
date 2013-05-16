//
//  RGIPacket.m
//  RGIApp
//
//  Created by Tiago Janela on 12/3/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import "RGIPacket.h"

@interface RGIPacket()

- (id) initWithId:(rgiPacketType)packetId payloadString:(NSString*)payload;
- (id) initWithId:(rgiPacketType)packetId payloadData:(NSData*)payload;

@end

@implementation RGIPacket

+ (rgiPacketType) typeFromData:(NSData*)data{
	if(!data || [data length] != ID_FIELD_LENGTH){
		return kRGIPacket_UnknownDatagram;
	}
	else{
		UInt8 *bytes = (UInt8*)[data bytes];
		UInt32 decodedPacketId = bytes[0] << 16 | bytes[1] << 8 | bytes[2];
		rgiPacketType packetId = (rgiPacketType) decodedPacketId;
		return packetId;
	}
}

+ (NSData*) magic{
	char magicField[MAGIC_FIELD_LENGTH] = MAGIC_FIELD;
	return [NSMutableData dataWithBytes:(&magicField) length:MAGIC_FIELD_LENGTH];
}

+ (RGIPacket*) decode:(NSData*) data{
	
	NSData *packetIdData = [data subdataWithRange:NSMakeRange(0, ID_FIELD_LENGTH)];
	NSData *payload = [data subdataWithRange:NSMakeRange(ID_FIELD_LENGTH, [data length] - ID_FIELD_LENGTH - MAGIC_FIELD_LENGTH)];
	NSData *magic = [data subdataWithRange:NSMakeRange([data length] - MAGIC_FIELD_LENGTH, MAGIC_FIELD_LENGTH)];
	
	if(![magic isEqualToData:[RGIPacket magic]]){
		return nil;
	}
	
	rgiPacketType packetId = [RGIPacket typeFromData:packetIdData];
	
	return [[[RGIPacket alloc] initWithId:packetId payloadData:payload] autorelease];
	
}

@synthesize packetId = _packetId;
@synthesize payload = _payload;
@synthesize magic = _magic;

- (NSData*) data{
	NSMutableData *data = [NSMutableData data];
	[data appendData:_packetId];
	[data appendData:_payload];
	[data appendData:_magic];
	return data;
}

- (NSString*) stringPayload{
	if(!_payload){
		return nil;
	}
	NSString *stringPayload = [[[NSString alloc] initWithData:_payload encoding:NSUTF8StringEncoding] autorelease];
	return stringPayload;
}

- (void)dealloc {
	[_packetId release];
	_packetId = nil;
	[_payload release];
	_payload = nil;
	[_magic release];
	_magic = nil;
	[super dealloc];
}

- (id) initWithId:(rgiPacketType)packetId payloadData:(NSData*)payload{
	self = [super init];
	if(self){
		
		UInt8 idArray[3] = { (packetId & 0x00FF0000) >> 16, (packetId & 0x0000FF00) >> 8, (packetId & 0x000000FF) };
		self.packetId = [NSMutableData dataWithBytes:&idArray length:3];
		self.payload = [NSMutableData dataWithData:payload];
		char magicField[MAGIC_FIELD_LENGTH] = MAGIC_FIELD;
		self.magic = [NSMutableData dataWithBytes:(&magicField) length:MAGIC_FIELD_LENGTH];
		
	}
	return self;

}

- (id) initWithId:(rgiPacketType)packetId payloadString:(NSString*)payload{
	NSData *data = [NSMutableData dataWithData:[payload dataUsingEncoding:NSUTF8StringEncoding]];
	self = [self initWithId:packetId payloadData:data];
	if(self){
		
	}
	return self;
}

- (NSString *)description{
	UInt8* packetIdBytes = (UInt8*) [_packetId bytes];
	UInt32 packetType = packetIdBytes[0] << 16 | packetIdBytes[1] << 8 | packetIdBytes[2];
	
	NSString *payloadString = [[[NSString alloc] initWithData:_payload encoding:NSUTF8StringEncoding] autorelease];
	NSString *magicString = [[[NSString alloc] initWithData:_magic encoding:NSUTF8StringEncoding] autorelease];
	return [NSString stringWithFormat:@"<0x%lX|%@|%@>",packetType, payloadString, magicString];
}

+ (RGIPacket*) packetRequestRegisterDisplayClient{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_RegisterDisplayClientRequest payloadString:nil];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

+ (RGIPacket*) packetRequestUnregisterDisplayClient{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_UnregisterDisplayClientRequest payloadString:nil];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

+ (RGIPacket*) packetRequestRegisterControlClient:(NSString *)hash{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_RegisterControlClientRequest payloadString:hash];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

+ (RGIPacket*) packetRequestUnregisterControlClient:(NSString *)hash{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_UnregisterControlClientRequest payloadString:hash];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

+ (RGIPacket*) packetRequestPayload:(NSData *)data{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_PayloadRequest payloadData:data];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

+ (RGIPacket*) packetResponsePayload:(NSData *)data{
	RGIPacket *packet = [[RGIPacket alloc] initWithId:kRGIPacket_PayloadResponse payloadData:data];
	NSLog(@"Created packet: %@",packet);
	return [packet autorelease];
}

@end
