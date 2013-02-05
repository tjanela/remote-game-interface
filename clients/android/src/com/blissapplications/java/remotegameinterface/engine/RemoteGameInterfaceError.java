package com.blissapplications.java.remotegameinterface.engine;

public enum RemoteGameInterfaceError {
	CantLoadConfigFile,
	MissingInfrastructureKeyOrValueOnConfiguration,
	MissingAdHocAccessPointKeyOrValueOnConfiguration,
	MissingAvailabilityKeyOrValueOnConfiguration,
	MissingEndpointKeyOrValueOnConfiguration,
	MissingPortKeyOrValueOnConfiguration,
	MissingLatitudeKeyOrValueOnConfiguration,
	MissingLongitudeKeyOrValueOnConfiguration,
	MissingAvailabilityRadiusKeyOrValueOnConfiguration,
	//Registration and communication
	CouldNotConnectToEndpoint,
	CouldNotRegisterWithEndpoint,
	OperationNotPermittedInCurrentState,
	//Location
	ErrorWhileRetrievingLocation,
	ConnectionResetError
}