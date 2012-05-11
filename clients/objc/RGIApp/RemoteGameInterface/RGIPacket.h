//
//  RGIPacket.h
//  RGIApp
//
//  Created by Tiago Janela on 12/3/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import <Foundation/Foundation.h>

#define RGI_MAX_PACKET_SIZE 200

#define ID_FIELD_LENGTH 3
#define MAGIC_FIELD {'[','!','P','U','M','P','!',']'}
#define MAGIC_FIELD_LENGTH 8
#define MIN_PACKET_SIZE (ID_FIELD_LENGTH + MAGIC_FIELD_LENGTH)

typedef enum 
{
	//Requests
	kRGIPacket_RegisterDisplayClientRequest			= 0x010000,
	kRGIPacket_UnregisterDisplayClientRequest		= 0x010001,
	kRGIPacket_RegisterControlClientRequest			= 0x020000,
	kRGIPacket_UnregisterControlClientRequest		= 0x020001,
	kRGIPacket_PayloadRequest										= 0x030000,
	
	//Responses
	kRGIPacket_RegisterDisplayClientResponse		= 0x01FFFF,
	kRGIPacket_UnregisterDisplayClientResponse	= 0x01FFFE,
	kRGIPacket_RegisterControlClientResponse		= 0x02FFFF,
	kRGIPacket_UnregisterControlClientResponse	= 0x02FFFE,
	kRGIPacket_PayloadResponse									= 0x03FFFF,
	
	//Anything else
	kRGIPacket_UnknownDatagram									= 0x000000

}rgiPacketType;

@interface RGIPacket : NSObject
{
@private
	NSMutableData *_packetId;
	NSMutableData *_payload;
	NSMutableData *_magic;
}

@property (nonatomic, copy) NSMutableData *packetId;
@property (nonatomic, copy) NSMutableData *payload;
@property (nonatomic, copy) NSMutableData *magic;

- (NSData*) data;

- (NSString*) stringPayload;

+ (RGIPacket*) packetRequestRegisterDisplayClient;
+ (RGIPacket*) packetRequestUnregisterDisplayClient;

+ (RGIPacket*) packetRequestRegisterControlClient:(NSString *)hash;
+ (RGIPacket*) packetRequestUnregisterControlClient:(NSString *)hash;
+ (RGIPacket*) packetRequestPayload:(NSData *)payload;
+ (RGIPacket*) packetResponsePayload:(NSData *)payload;

+ (NSData*) magic;

+ (rgiPacketType) typeFromData:(NSData*)data;

+ (RGIPacket*) decode:(NSData*) data;
@end
