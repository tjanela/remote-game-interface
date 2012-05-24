package com.blissapplications.java.remotegameinterface.engine;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cuub.android.kit.location.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.blissapplications.java.remotegameinterface.packets.OperationalProtocolPacket;
import com.blissapplications.java.remotegameinterface.packets.OperationalProtocolPacketType;

import cuub.android.kit.utils.Utils;

public class RemoteGameInterfaceEngine implements ILocationListener 
{
	private static RemoteGameInterfaceEngine DEFAULT_ENGINE;
	
	private RemoteGameInterfaceConfiguration configuration = null;
	private Socket socket;
	private InputStream reader;
	private OutputStream writer;
	private Thread thread;
	private List<IRemoteGameInterfaceEngineDelegate> delegates;
	private RemoteGameInterfaceState state;
	private RemoteGameInterfaceStatus status;
	private MyLocationManager lm;
	private boolean exitThread = false;
	private boolean checkingState = false;
	
	Activity activity;
	
	public RemoteGameInterfaceStatus getStatus()
	{
		return status;
	}
	
	public RemoteGameInterfaceState getState()
	{
		return state;
	}
	
	public void reset()
	{
		status = RemoteGameInterfaceStatus.Newborn;
		state = null;
	}
	
	public static RemoteGameInterfaceEngine getDefaultEngine()
	{
			if(DEFAULT_ENGINE == null)
			{
				DEFAULT_ENGINE = new RemoteGameInterfaceEngine();
			}
			return DEFAULT_ENGINE;
	}
	
	private RemoteGameInterfaceEngine()
	{
		delegates = new ArrayList<IRemoteGameInterfaceEngineDelegate>();
		status = RemoteGameInterfaceStatus.Newborn;
	}
	
	public void addDelegate(IRemoteGameInterfaceEngineDelegate delegate)
	{
			if(!delegates.contains(delegate))
			{
				delegates.add(delegate);
			}
	}
	
	public void removeDelegate(IRemoteGameInterfaceEngineDelegate delegate)
	{
			if(delegates.contains(delegate))
			{
				delegates.remove(delegate);
			}
	}
	
	public void configure(Context context, String configUrl)
	{
		if(Utils.isInternetAvailable(context))
		{
			String response = Utils.getJSONString(configUrl);
			
	    if (Utils.isStringBlank(response))
	    {
	    	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
					delegate.didNotConfigure(RemoteGameInterfaceError.CantLoadConfigFile);
				}
	    	return;
	    }
	    
	    response = Utils.formatStringToStartWithFistChar(response, '{');

	    try {
	      JSONObject configurationObject = new JSONObject(response);
	      
	      if (Utils.isJSONObjectBlank(configurationObject))
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
	      		delegate.didNotConfigure(RemoteGameInterfaceError.CantLoadConfigFile);
					}	
	      	return;
	      }
	      	
	      RemoteGameInterfaceConfiguration configuration = new RemoteGameInterfaceConfiguration();
	      
	      String infrastructure = null;
	      
	      try{
	      	infrastructure = configurationObject.getString("Infrastructure");
	      }catch(JSONException ex){}
	      
	      if(infrastructure == null)
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingInfrastructureKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      else if(infrastructure.equals("AdHoc"))
	      {
	      	configuration.Infrastructure = RemoteGameInterfaceInfrastucture.AdHoc;
	      }
	      else if(infrastructure.equals("Public"))
	      {
	      	configuration.Infrastructure = RemoteGameInterfaceInfrastucture.Public;
	      }
	      else
	      {
		     	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
			    	delegate.didNotConfigure(RemoteGameInterfaceError.MissingInfrastructureKeyOrValueOnConfiguration);
					}
		      return;
	      }
	      
	      
	      String availability = null;
	      
	      try{
	      	availability = configurationObject.getString("Availability");
	      }catch (JSONException e) {}
	      
	      if(availability == null){
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingAvailabilityKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      else if(availability.equals("BeforeEvent"))
	      {
	      	configuration.Availability = RemoteGameInterfaceAvailability.BeforeEvent;
	      }
	      else if(availability.equals("Rockin"))
	      {
	      	configuration.Availability = RemoteGameInterfaceAvailability.Rockin;
	      }
	      else if(availability.equals("AfterEvent"))
	      {
	      	configuration.Availability = RemoteGameInterfaceAvailability.AfterEvent;
	      }
	      else
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingAvailabilityKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      
	      try{
	      	configuration.AdHocAccessPointPassword = configurationObject.getString("AdHocAccessPointPassword");
	      }catch(JSONException ex){}
	      
	      try{
	      	configuration.AdHocAccessPointSSID = configurationObject.getString("AdHocAccessPointSSID");
	      }catch(JSONException ex){}
	      
	      try{
	      	configuration.AdHocAccessPointBSSID = configurationObject.getString("AdHocAccessPointBSSID");
	      }catch(JSONException ex){}
	      
	      if(configuration.Infrastructure.equals(RemoteGameInterfaceInfrastucture.AdHoc))
	      {
	      	if(configuration.AdHocAccessPointSSID == null ||
	      			configuration.AdHocAccessPointSSID.equals(""))
	      	{
	      		for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
						{
			    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingAdHocAccessPointKeyOrValueOnConfiguration);
						}
		      	return;
	      	}
	      }
	      
	      try
	      {
	      	configuration.Endpoint = configurationObject.getString("Endpoint");
	      }
	      catch(JSONException e)
	      {}
	      if(configuration.Endpoint == null || configuration.Endpoint.equals(""))
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
	      	{
	      		delegate.didNotConfigure(RemoteGameInterfaceError.MissingEndpointKeyOrValueOnConfiguration);
	      	}
	      	return;
	      }
	      
	      try
	      {
	      	configuration.AvailabilityRadius = configurationObject.getDouble("AvailabilityRadius");
	      }catch (JSONException e) {}
	      
	      if(configuration.Endpoint == null || configuration.Endpoint.equals(""))
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingEndpointKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      
	      try
	      {
	      	configuration.Longitude = configurationObject.getDouble("Longitude");
	      } 
	      catch(JSONException e)
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingLongitudeKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      
	      try
	      {
	      	configuration.Latitude = configurationObject.getDouble("Latitude");
	      } 
	      catch(JSONException e)
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingLatitudeKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      
	      try
	      {
	      	configuration.Port = configurationObject.getInt("Port");
	      }
	      catch(JSONException e)
	      {
	      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
					{
		    		delegate.didNotConfigure(RemoteGameInterfaceError.MissingPortKeyOrValueOnConfiguration);
					}
	      	return;
	      }
	      
	      setConfiguration(configuration);
	     
	      status = RemoteGameInterfaceStatus.Configured;
	      
	      for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
	    		delegate.didConfigure();
				}
	    }
	    catch(Exception e)
	    {
	    	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
	    		delegate.didNotConfigure(RemoteGameInterfaceError.CantLoadConfigFile);
				}
	    }
		}
		else
		{
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didNotConfigure(RemoteGameInterfaceError.CantLoadConfigFile);
			}
		}
	}

	public void checkState(final Activity callingActivity)
	{
		activity = callingActivity;
		if(!status.equals(RemoteGameInterfaceStatus.Configured))
		{
			return;
		}
		lm = new MyLocationManager(callingActivity, 10000, 100);
		
		if(!GPSAvailable())
		{
			state = RemoteGameInterfaceState.LocationNotAvailable;
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didCheckState();
			}
			return;
		}
		else if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
		{
	    //Ask the user to enable GPS
	    AlertDialog.Builder builder = new AlertDialog.Builder(callingActivity);
	    builder.setTitle("Geolocalização desligada");
	    builder.setMessage("Precisamos de obter a tua geolocalização.\nQueres abrir as definições do teu dispositivo?");
	    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            //Launch settings, allowing user to make a change
	            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            callingActivity.startActivity(i);
	        }
	    });
	    builder.setNegativeButton("Agora não", new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	state = RemoteGameInterfaceState.LocationNotAvailable;
	    			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
	    			{
	    				delegate.didCheckState();
	    			}
	    			return;
	        }
	    });
	    builder.create().show();
		}else{
			lm.setILocationListener(this);
			lm.startLocating();
		}
	}
	

	final Handler mHandler = new Handler();

	
	public void connect(String host, int port) throws Exception
	{
		
		if(socket != null)
		{
			reader.close();
			writer.close();
			socket.close();	
		}
		
		if(thread != null)
		{
			exitThread = Boolean.TRUE;
			Thread.yield();
			if(thread.isAlive()){
				thread.stop();
			}
			thread = null;
		}
		exitThread = Boolean.FALSE;
		socket = new Socket(host,port);
		writer = socket.getOutputStream();
		reader = socket.getInputStream();
		thread = new Thread(){
			
			@Override
			public void run() 
			{
				ByteBuffer request = null;
				OperationalProtocolPacket packet = null;
				while(!exitThread){
					try
					{
						request = readRequest();
						
						if(request == null || request.capacity() == 0)
						{
							exitThread = Boolean.TRUE;
							mHandler.post(new Runnable() {
					      public void run() {
					      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
									{
					      		delegate.didDisconnect(null);
									}
					      }
					    });
							continue;
						}
						
						packet = OperationalProtocolPacket.decode(request);
						
						if(packet == null)
						{
							exitThread = Boolean.TRUE;
							mHandler.post(new Runnable() {
					      public void run() {
					      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
									{
					      		delegate.didDisconnect(null);
									}
					      }
					    });
							continue;
						}
						
						String payload = new String(packet.getPayload());
						OperationalProtocolPacketType packetType = packet.getPacketType();
						
						if(packetType.equals(OperationalProtocolPacketType.RegisterControlClientResponse))
						{
							if(payload.equals("UNKNOWN_HASH"))
							{
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didNotRegister(new Exception("Código inválido!"));
										}
						      }
						    });
							}
							else if(payload.equals("ERROR"))
							{
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didNotRegister(new Exception("Ocorreu um erro!"));
										}
						      }
						    });
							}
							else if(payload.equals("ALREADY_REGISTERED"))
							{
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didNotRegister(new Exception("Código já registado!"));
										}
						      }
						    });
							}
							else {
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didRegister();
										}
						      }
						    });	
							}
							
						}
						else if(packetType.equals(OperationalProtocolPacketType.UnregisterControlClientRequest))
						{
							mHandler.post(new Runnable() {
					      public void run() {
					      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
									{
					      		delegate.didDisconnect(new Exception("Client disconnected."));
									}
					      }
					    });
						}
						else if(packetType.equals(OperationalProtocolPacketType.PayloadRequest) || 
								packetType.equals(OperationalProtocolPacketType.PayloadResponse))
						{
							if(payload.equals("OLEH")){
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didReceiveHandshakeResponse();
										}
						      }
						    });
							}
							else if(payload.startsWith("SCORE:")){
								String scoreString = payload.replace("SCORE:", "");
								final float score = Float.parseFloat(scoreString);
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didReceiveScore(score);
										}
						      }
						    });
							}
							else if(payload.startsWith("FINISH:")){
								String scoreString = payload.replace("FINISH:", "");
								final float score = Float.parseFloat(scoreString);
								mHandler.post(new Runnable() {
						      public void run() {
						      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
										{
						      		delegate.didReceiveFinish(score);
										}
						      }
						    });
							}
						}	
					}
					catch(final Exception ex)
					{
						
						exitThread = Boolean.TRUE;
						mHandler.post(new Runnable() {
				      public void run() {
				      	for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
								{
				      		delegate.didDisconnect(ex);
								}
				      }
				    });
					}
					Thread.yield();
				}
				Log.d("RGI", "Thread Exiting!");
			}
			
			public ByteBuffer readRequest() throws Exception{
				return readFromInputStream(reader,OperationalProtocolPacket.PACKET_MAX_SIZE);
			}
			
			protected ByteBuffer readFromInputStream(InputStream inputStream, Integer maxBytes) throws Exception{
				ByteBuffer buffer = ByteBuffer.allocate(maxBytes);
				buffer.mark();
				int codePoint;
				boolean magicFound = false;

				int packetSize = 0;
				byte[] ourMagic = new byte[OperationalProtocolPacket.MAGIC_FIELD_LENGTH];
				do {
					codePoint = inputStream.read();
					byte readByte = (byte)(codePoint & 0xff);
					buffer.put(readByte);
					if(buffer.position() >= OperationalProtocolPacket.PACKET_MIN_SIZE){
						buffer.position(buffer.position() - OperationalProtocolPacket.MAGIC_FIELD_LENGTH);
						buffer.get(ourMagic, 0, OperationalProtocolPacket.MAGIC_FIELD_LENGTH);
						if(Arrays.equals(ourMagic, OperationalProtocolPacket.MAGIC_FIELD)){
							magicFound = true;
							packetSize = buffer.position();
						}
					}
				}	while (!magicFound && buffer.position() < maxBytes && codePoint != -1);

				buffer.reset();
				
				ByteBuffer trimmedByteBuffer = ByteBuffer.allocate(packetSize);
				trimmedByteBuffer.mark();
				trimmedByteBuffer.put(buffer.array(),0,packetSize);
				trimmedByteBuffer.reset();

				return trimmedByteBuffer;
			}
		};
		
		if(checkingState)
		{
			checkingState = false;
			
			if(socket.isConnected())
			{
				state = RemoteGameInterfaceState.AllOK;
			}
			else
			{
				state = RemoteGameInterfaceState.NotAvailable;
			}
			
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didCheckState();
			}
			return;
		}
		
		if(socket.isConnected())
		{
			thread.start();
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didConnect();
			}
		}
		else
		{
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didNotConnect(new Exception());
			}
		}
	}
	public void disconnect()
	{
		try
		{
			writer.close();
			reader.close();
			socket.close();
		}
		catch(Exception e)
		{	}
		
		exitThread = Boolean.TRUE;
		thread = null;
		
		reader = null;
		writer = null;
		
		socket = null;
	}
	
	public void register(String code) throws Exception
	{
		OperationalProtocolPacket packet = OperationalProtocolPacket.getRegisterControlClientRequest(code);
		ByteBuffer byteBuffer = OperationalProtocolPacket.encode(packet);
		writer.write(byteBuffer.array());
		writer.flush();
	}
	
	public void sendHandshake() throws Exception
	{
		sendPayload("HELO");
	}
	
	public void sendStartGame(String playerName) throws Exception
	{
		sendPayload("START:" + playerName);
	}
	
	public void sendControlData(float x, float y, float z, boolean a, boolean b) throws Exception
	{
		sendPayload("<CONTROL>AX:" + x + "|AY:" + y + "|AZ:" + z + "|A:" + (a ? "1" : "0") + "|B:" + (b ? "1" : "0"));
		
	}
	
	private void sendPayload(String payload) throws Exception
	{
		OperationalProtocolPacket packet = OperationalProtocolPacket.getPayloadRequest(payload);
		ByteBuffer byteBuffer = OperationalProtocolPacket.encode(packet);
		writer.write(byteBuffer.array());
	}

	public RemoteGameInterfaceConfiguration getConfiguration() {
		return configuration;
	}

	private void setConfiguration(RemoteGameInterfaceConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void onLocationChanged(Location location) {
		
		Location remoteGameInterfaceDisplayLocation = new Location(location);
		
		remoteGameInterfaceDisplayLocation.setLongitude(getConfiguration().Longitude);
		remoteGameInterfaceDisplayLocation.setLatitude(getConfiguration().Latitude);
		if(location.distanceTo(remoteGameInterfaceDisplayLocation) > getConfiguration().AvailabilityRadius)
		{
			state = RemoteGameInterfaceState.NotCloseEnough;
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didCheckState();
			}
			lm.stopLocating();
			return;
		}
		
		if(getConfiguration().Infrastructure.equals(RemoteGameInterfaceInfrastucture.AdHoc))
		{
			if(!Utils.isConnectedToWiFi(activity))
			{
				state = RemoteGameInterfaceState.AdHocInfrastrucureAndNotConnectedToCorrectAccessPoint;
				for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
					delegate.didCheckState();
				}
				lm.stopLocating();
				return;
			}
			if(!Utils.getWiFiSSID(activity).equals(getConfiguration().AdHocAccessPointSSID)){
				state = RemoteGameInterfaceState.AdHocInfrastrucureAndNotConnectedToCorrectAccessPoint;
				for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
					delegate.didCheckState();
				}
				lm.stopLocating();
				return;
			}
			if(getConfiguration().AdHocAccessPointBSSID != null && 
					!getConfiguration().AdHocAccessPointBSSID.equals("") &&
					Utils.getWiFiBSSID(activity).equals(getConfiguration().AdHocAccessPointBSSID)){
				state = RemoteGameInterfaceState.AdHocInfrastrucureAndNotConnectedToCorrectAccessPoint;
				for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
				{
					delegate.didCheckState();
				}
				lm.stopLocating();
				return;
			}
		}
		
		checkingState = true;
		
		try
		{
			this.connect(getConfiguration().Endpoint, getConfiguration().Port);
			lm.stopLocating();
		}
		catch(Exception ex)
		{
			checkingState = false;
			state = RemoteGameInterfaceState.NotAvailable;
			for (IRemoteGameInterfaceEngineDelegate delegate : delegates) 
			{
				delegate.didCheckState();
			}
			lm.stopLocating();
			return;
		}
		
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	public boolean GPSAvailable() 
	{
    LocationManager loc_manager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
    List<String> str = loc_manager.getProviders(true);

    if(str.size()>0)
        return true;
    else
        return false;
}

	
}
