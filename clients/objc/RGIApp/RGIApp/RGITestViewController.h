//
//  RGITestViewController.h
//  RGIApp
//
//  Created by Tiago Janela on 12/4/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RGIRemoteGameInterface.h"

typedef enum {
	kRGIControlClientStatus_Disconnected = 0,
	kRGIControlClientStatus_Connecting = 1,
	kRGIControlClientStatus_Connected = 2,
	kRGIControlClientStatus_Registering = 3,
	kRGIControlClientStatus_Registered = 4,
	kRGIControlClientStatus_Disconnecting = 5
} rgiControlClientStatus;

@interface RGITestViewController : UIViewController
<
	RGIRemoteGameInterfaceDelegate
>
{
	UILabel *_stateLabel;
	UITextView *_textView;
	UIButton *_connectButton;
	
	rgiControlClientStatus _clientStatus;
}
@end
