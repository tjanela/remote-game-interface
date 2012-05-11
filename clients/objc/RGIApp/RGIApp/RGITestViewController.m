//
//  RGITestViewController.m
//  RGIApp
//
//  Created by Tiago Janela on 12/4/11.
//  Copyright (c) 2011 Bliss Applications. All rights reserved.
//

#import "RGITestViewController.h"


@implementation RGITestViewController

#pragma mark - RGIRemoteGameInterfaceDelegate

- (void) remoteGameInterfaceDidConnectSuccessfuly:(RGIRemoteGameInterface*)remoteGameInterface{
	_stateLabel.text = @"Connected!";
	[_connectButton setTitle:@"Register" forState:UIControlStateNormal];
	[_textView becomeFirstResponder];
	_clientStatus = kRGIControlClientStatus_Connected;
}

- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didNotConnectWithError:(NSError*)error{
	_stateLabel.text = @"Not Connected!";
	[_connectButton setTitle:@"Connect" forState:UIControlStateNormal];
	NSLog(@"Error: %@",error);
}

- (void) remoteGameInterfaceControlClientRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface{
	_stateLabel.text = @"Registered!";
	[_connectButton setTitle:@"Handshake" forState:UIControlStateNormal];
	_clientStatus = kRGIControlClientStatus_Registered;
}


- (void) remoteGameInterfaceControlClientDidNotRegister:(RGIRemoteGameInterface*)remoteGameInterface withError:(rgiError)error{}
- (void) remoteGameInterfaceControlClientDisconnected:(RGIRemoteGameInterface *)remoteGameInterface{}


- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didReceivePacket:(RGIPacket*)packet{
	NSLog(@"Did receive packet: %@",packet);
	
	NSString *payloadString = [[[NSString alloc] initWithData:packet.payload encoding:NSUTF8StringEncoding] autorelease];
	
	if(_clientStatus == kRGIControlClientStatus_Handshaking){
		if([@"OLEH" isEqualToString:payloadString]){
			_clientStatus = kRGIControlClientStatus_Handshaked;
			_stateLabel.text = @"Handshaked!";
			[_connectButton setTitle:@"Activate" forState:UIControlStateNormal];
		}
	}
	else if(_clientStatus == kRGIControlClientStatus_Activating){
		if ([@"SCORE:0" isEqualToString:payloadString]) {
			_clientStatus = kRGIControlClientStatus_Activated;
			_stateLabel.text = @"Activated - Score: 0";

			[[UIAccelerometer sharedAccelerometer] setUpdateInterval:1.0/10.0];
			[[UIAccelerometer sharedAccelerometer] setDelegate:self];
			
		}
	}
	else if(_clientStatus == kRGIControlClientStatus_Activated){
		if([payloadString hasPrefix:@"FINISH:"]){
			_clientStatus = kRGIControlClientStatus_Deactivated;
			_stateLabel.text = [NSString stringWithFormat:@"Deactivated - Final Score: %@",[payloadString stringByReplacingOccurrencesOfString:@"FINISH:" withString:@""]];
			[_connectButton setTitle:@"Replay" forState:UIControlStateNormal];
		} else if([payloadString hasPrefix:@"SCORE:"]){
			_stateLabel.text = [NSString stringWithFormat:@"Activated - Score: %@",[payloadString stringByReplacingOccurrencesOfString:@"SCORE:" withString:@""]];
		}
	}
	
}

- (void) remoteGameInterfaceDisplayClientRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface withHash:(NSString*)hash{}
- (void) remoteGameInterfaceDisplayClientDidNotRegister:(RGIRemoteGameInterface*)remoteGameInterface withError:(rgiError)error{}
- (void) remoteGameInterfaceDisplayClientDisconnected:(RGIRemoteGameInterface *)remoteGameInterface{}

#pragma mark - UIAccelerometerDelegate

- (void) generate:(id)sender{
	if(_clientStatus == kRGIControlClientStatus_Activated){
		NSString *payload = [NSString stringWithFormat:@"<CONTROL>AX:%@|AY:%@|C1:0|C2:0", [NSNumber numberWithFloat:(rand() * 1.0f / RAND_MAX)],[NSNumber numberWithFloat:(rand() * 1.0f / RAND_MAX)]];
		[[RGIRemoteGameInterface sharedInterface] sendPayload:[payload dataUsingEncoding:NSUTF8StringEncoding]];
	}
	else {
		[sender invalidate];
	}
}

- (void)accelerometer:(UIAccelerometer *)accelerometer didAccelerate:(UIAcceleration *)acceleration{
	if(_clientStatus == kRGIControlClientStatus_Activated){
		NSString *payload = [NSString stringWithFormat:@"<CONTROL>AX:%f|AY:%f|C1:0|C2:0",acceleration.x,acceleration.y];
		[[RGIRemoteGameInterface sharedInterface] sendPayload:[payload dataUsingEncoding:NSUTF8StringEncoding]];
	}
}

#pragma mark - RGI Glue

- (void) connectButton_touchUpInside:(id)sender {
	switch(_clientStatus){
		case kRGIControlClientStatus_Disconnected:
		{
			NSLog(@"Connecting...");
			_clientStatus = kRGIControlClientStatus_Connecting;
		_stateLabel.text = @"Connecting...";
		[[RGIRemoteGameInterface sharedInterface] connectToServer:@"192.168.4.150" port:[NSNumber numberWithInt:1935] andClientType:kRGIClientType_Control];
			break;
		}
		case kRGIControlClientStatus_Connected:{
			NSLog(@"Registering with hash '%@'...",_textView.text);
			[[NSUserDefaults standardUserDefaults] setValue:_textView.text forKey:@"LastHash"];
			_clientStatus = kRGIControlClientStatus_Registering;
			_stateLabel.text = [NSString stringWithFormat:@"Registering with hash '%@'...",_textView.text] ;
			[[RGIRemoteGameInterface sharedInterface] registerWithHash:_textView.text];
			
		}
		case kRGIControlClientStatus_Registered:{
			NSLog(@"Sending handshake payload...");
			_clientStatus = kRGIControlClientStatus_Handshaking;
			_stateLabel.text = @"Handshaking...";
			[[RGIRemoteGameInterface sharedInterface] sendPayload:[@"HELO" dataUsingEncoding:NSUTF8StringEncoding]];
			break;
		}
		case kRGIControlClientStatus_Handshaked:{
			NSLog(@"Sending activate payload...");
			_stateLabel.text = @"Activating...";
			[[RGIRemoteGameInterface sharedInterface] sendPayload:[@"START" dataUsingEncoding:NSUTF8StringEncoding]];
			_clientStatus = kRGIControlClientStatus_Activating;
			break;
		}
		case kRGIControlClientStatus_Deactivated:{
			NSLog(@"Sending replay payload...");
			_stateLabel.text = @"Replaying...";
			[[RGIRemoteGameInterface sharedInterface] sendPayload:[@"REPLAY:1|NAME:tjanela" dataUsingEncoding:NSUTF8StringEncoding]];
			_clientStatus = kRGIControlClientStatus_Activating;
			break;
		}
		default:
		{
			break;
		}
	}
}

#pragma mark - Object lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
			[[RGIRemoteGameInterface sharedInterface] addDelegate:self];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle


// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
	[super loadView];
	
	CGSize screenSize = [[UIScreen mainScreen] bounds].size;
	
	_connectButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
	[_connectButton setTitle:@"Connect" forState:UIControlStateNormal];
	[_connectButton addTarget:self action:@selector(connectButton_touchUpInside:) forControlEvents:UIControlEventTouchUpInside];
	_connectButton.frame = CGRectMake(0, 0, screenSize.width, 40);
	[self.view addSubview:_connectButton];
	
	_textView = [[[UITextView alloc] initWithFrame:CGRectMake(0, 40, screenSize.width, 40)] autorelease];
	_textView.text =[[NSUserDefaults standardUserDefaults] valueForKey:@"LastHash"];
	[self.view addSubview:_textView];
	
	_stateLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0, 80, screenSize.width, 40)]autorelease];
	[self.view addSubview:_stateLabel];
	_stateLabel.text = @"Not Connected!";
}

- (void)viewDidUnload
{
	[super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
