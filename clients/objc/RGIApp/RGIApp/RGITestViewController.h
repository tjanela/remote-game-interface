//
//  RGITestViewController.h
//  RGIApp
//
//  Created by Tiago Janela on 12/4/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RGIRemoteGameInterface.h"

@interface RGITestViewController : UIViewController
<
	RGIRemoteGameInterfaceDelegate
>
{
	UILabel *_stateLabel;
	UITextView *_textView;
	UIButton *_connectButton;
}
@end
