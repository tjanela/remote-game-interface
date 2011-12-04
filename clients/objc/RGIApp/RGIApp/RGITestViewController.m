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
	[_textView becomeFirstResponder];
	_clientStatus = kRGIControlClientStatus_Connected;
}

- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didNotConnectWithError:(NSError*)error{
	_stateLabel.text = @"Not Connected!";
	NSLog(@"Error: %@",error);
}

- (void) remoteGameInterfaceRegisteredSuccessfully:(RGIRemoteGameInterface*)remoteGameInterface{
	_stateLabel.text = @"Registered!";
	_clientStatus = kRGIControlClientStatus_Registered;
}

- (void) remoteGameInterface:(RGIRemoteGameInterface*)remoteGameInterface didReceivePacket:(RGIPacket*)packet{
}

#pragma mark - RGI Glue

- (void) connectButton_touchUpInside:(id)sender {
	switch(_clientStatus){
		case kRGIControlClientStatus_Disconnected:
		{
			NSLog(@"Connecting...");
			_clientStatus = kRGIControlClientStatus_Connecting;
			[[RGIRemoteGameInterface sharedInterface] connectToServer];
			break;
		}
		case kRGIControlClientStatus_Connected:{
			NSLog(@"Registering with hash '%@'...",_textView.text);
			_clientStatus = kRGIControlClientStatus_Registering;
			[[RGIRemoteGameInterface sharedInterface] registerWithHash:_textView.text];
			
		}
		case kRGIControlClientStatus_Registered:{
			NSLog(@"Sengin payload...");
			[[RGIRemoteGameInterface sharedInterface] sendPayload:[@"HELO" dataUsingEncoding:NSUTF8StringEncoding]];
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
	[self.view addSubview:_textView];
	
	_stateLabel = [[[UILabel alloc] initWithFrame:CGRectMake(0, 80, screenSize.width, 40)]autorelease];
	[self.view addSubview:_stateLabel];
	_stateLabel.text = @"Not Connected!";
}

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
}
*/

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
