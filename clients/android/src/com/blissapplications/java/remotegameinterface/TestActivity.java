package com.blissapplications.java.remotegameinterface;

import com.blissapplications.remotegameinterface.R;

import com.blissapplications.java.remotegameinterface.packets.*;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TestActivity extends Activity implements IRemoteGameInterfaceEngineDelegate , SensorEventListener
{
	public final static String LOG_TAG = "RGI";
	
	private TextView greyTextView;
	private TextView redTextView;
	private Button refreshButton;
	private Button seeInMapButton;
	private EditText playerNameEditText;
	private EditText codeEditText;
	private Button connectButton;
	private Button startGameButton;
	private ProgressBar progressBar;
	
	private String playerName;
	private String code;

	private SensorManager mSensorManager;
  private Sensor mAccelerometer;

  
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    
		greyTextView = (TextView) findViewById(R.id.GreyTextView);
		redTextView = (TextView) findViewById(R.id.RedTextView);
		
		playerNameEditText = (EditText) findViewById(R.id.PlayerNameEditText);
		codeEditText = (EditText) findViewById(R.id.CodeEditText);
		
		progressBar = (ProgressBar) findViewById(R.id.ProgressBar);
		
		refreshButton = (Button) findViewById(R.id.RefreshButton);
		seeInMapButton = (Button) findViewById(R.id.SeeInMap);
		connectButton = (Button) findViewById(R.id.ConnectButton);
		startGameButton = (Button) findViewById(R.id.StartGameButton);
		
		setRefreshButtonListeners();
		setSeeInMapButtonListeners();
		setConnectButtonListeners();
		setStartGameButtonListeners();
		
		greyTextView.setText("A ligar...");
		
		try
		{
			RemoteGameInterfaceEngine.getDefaultEngine().addDelegate(this);
			RemoteGameInterfaceEngine.getDefaultEngine().configure();
		}
		catch(Exception ex)
		{
			Log.e(LOG_TAG,"Error: ", ex);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	private void setRefreshButtonListeners()
	{
		refreshButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				refreshButton.setClickable(false);
				RemoteGameInterfaceStatus status = RemoteGameInterfaceEngine.getDefaultEngine().getStatus();
				RemoteGameInterfaceState state = RemoteGameInterfaceEngine.getDefaultEngine().getState();
				
				if(status.equals(RemoteGameInterfaceStatus.Newborn)){
					RemoteGameInterfaceEngine.getDefaultEngine().configure();
					progressBar.setVisibility(View.VISIBLE);
					playerNameEditText.setVisibility(View.GONE);
					codeEditText.setVisibility(View.GONE);
					seeInMapButton.setVisibility(View.GONE);
					connectButton.setVisibility(View.GONE);
					redTextView.setVisibility(View.GONE);
					greyTextView.setText("A ligar...");
				}
				else if(!state.equals(RemoteGameInterfaceState.AllOK))
				{
					RemoteGameInterfaceEngine.getDefaultEngine().checkState(TestActivity.this);
					progressBar.setVisibility(View.VISIBLE);
					playerNameEditText.setVisibility(View.GONE);
					codeEditText.setVisibility(View.GONE);
					seeInMapButton.setVisibility(View.GONE);
					connectButton.setVisibility(View.GONE);
					redTextView.setVisibility(View.GONE);
					greyTextView.setText("A ligar...");
				}
				else
				{
					refreshButton.setClickable(true);
					greyTextView.setText("Escolhe um nome e insere o código que vês na Play Wall para começares a jogar.");
					playerNameEditText.setVisibility(View.VISIBLE);
					codeEditText.setVisibility(View.VISIBLE);
					connectButton.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					seeInMapButton.setVisibility(View.GONE);
					redTextView.setVisibility(View.GONE);
				}
				
			}
		});
	}
	
	private void setSeeInMapButtonListeners()
	{
		seeInMapButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				//TODO: Open Map Activity
				
			}
			
		});
	}
	
	private void setStartGameButtonListeners(){
		startGameButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				startGameButton.setClickable(false);
				try
				{
					RemoteGameInterfaceEngine.getDefaultEngine().sendStartGame(playerName);
					mSensorManager.registerListener(TestActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
				}
				catch(Exception e)
				{
					showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
				}
			}
		});
	}
	
	private void setConnectButtonListeners()
	{
		connectButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				connectButton.setClickable(false);
				String host = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Endpoint;
				int port = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Port;
				
				playerName = playerNameEditText.getText().toString();
				code = codeEditText.getText().toString();
				
				progressBar.setVisibility(View.VISIBLE);
				
				connectButton.setVisibility(View.GONE);
				seeInMapButton.setVisibility(View.GONE);
				refreshButton.setVisibility(View.GONE);
				
				try
				{
					RemoteGameInterfaceEngine.getDefaultEngine().connect(host, port);
				}
				catch(Exception e)
				{
					showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
				}
				
			}
			
		});
	}
	
	public void didConfigure()
	{
		refreshButton.setClickable(false);
		try
		{
			
			RemoteGameInterfaceAvailability availability = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Availability;
			
			if(availability.equals(RemoteGameInterfaceAvailability.BeforeEvent))
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.VISIBLE);
				greyTextView.setVisibility(View.VISIBLE);
				seeInMapButton.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
				codeEditText.setVisibility(View.GONE);
				connectButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				startGameButton.setVisibility(View.GONE);
				
				greyTextView.setText("Poderás jogar este jogo durante o festival, no espaço\nVodafone Showcases.");
				redTextView.setText("Esperamos por ti!");
			}
			else if(availability.equals(RemoteGameInterfaceAvailability.AfterEvent))
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.VISIBLE);
				greyTextView.setVisibility(View.VISIBLE);
				seeInMapButton.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
				codeEditText.setVisibility(View.GONE);
				connectButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				startGameButton.setVisibility(View.GONE);
				
				greyTextView.setText("Este jogo esteve disponível durante o festival, no espaço\nVodafone Showcases.");
				redTextView.setText("Fica para o ano ;)");
			}
			else if(availability.equals(RemoteGameInterfaceAvailability.Rockin))
			{
				RemoteGameInterfaceEngine.getDefaultEngine().checkState(this);
			}
		}
		catch (Exception e) 
		{
			
			greyTextView.setText("Ocorreu um erro.");
			redTextView.setText("Por favor tenta mais tarde.");
			greyTextView.setVisibility(View.VISIBLE);
			redTextView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			playerNameEditText.setVisibility(View.GONE);
			codeEditText.setVisibility(View.GONE);
			seeInMapButton.setVisibility(View.GONE);
			
		}
	}
	
	public void didCheckState()
	{
		refreshButton.setClickable(false);
		
		RemoteGameInterfaceState state = RemoteGameInterfaceEngine.getDefaultEngine().getState();
		
		switch (state) {
			case AdHocInfrastrucureAndNotConnectedToCorrectAccessPoint:
			{				
				progressBar.setVisibility(View.GONE);
				seeInMapButton.setVisibility(View.GONE);
				greyTextView.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.VISIBLE);
				
				refreshButton.setVisibility(View.VISIBLE);
				refreshButton.setClickable(true);
				
				RemoteGameInterfaceConfiguration configuration = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration();
				String redText = "Nome da Rede:\n{WIRELESS_NETWORK}\n\nPassword:\n{WIRELESS_PASSWORD}".
						replaceAll("{WIRELESS_NETWORK}",  configuration.AdHocAccessPointSSID).
						replaceAll("{WIRELESS_PASSWORD}", configuration.AdHocAccessPointPassword);
				
				redTextView.setText(redText);
				greyTextView.setText("Liga-te à nossa rede WiFi para estabelecers uma ligação.");
				
				codeEditText.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
			}
			break;
			case AllOK:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.GONE);
				seeInMapButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				
				codeEditText.setVisibility(View.VISIBLE);
				playerNameEditText.setVisibility(View.VISIBLE);
				connectButton.setVisibility(View.VISIBLE);
				
				greyTextView.setText("Escolhe um nome e insere o código que vês na Play Wall para começares a jogar.");
				redTextView.setText("");
				greyTextView.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.GONE);
			}
			break;
			case LocationNotAvailable:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				seeInMapButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				
				codeEditText.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
				connectButton.setVisibility(View.GONE);
				
				greyTextView.setText("Serviço de localização inactivo.");
				redTextView.setText("Activa o serviço de localização nas Definições.");
			}
			break;
			case NotAvailable:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				seeInMapButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				
				codeEditText.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
				connectButton.setVisibility(View.GONE);
				
				greyTextView.setText("O jogo não está disponível neste momento.");
				redTextView.setText("Por favor tenta mais tarde!");
			}
			break;
			case NotCloseEnough:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				seeInMapButton.setVisibility(View.GONE);
				progressBar.setVisibility(View.GONE);
				
				codeEditText.setVisibility(View.GONE);
				playerNameEditText.setVisibility(View.GONE);
				connectButton.setVisibility(View.GONE);
				
				greyTextView.setText("Desloca-te ao espaço\nVodafone Showcases\ne aproxima-te da\nPlay Wall\npara jogares!");
				greyTextView.setVisibility(View.VISIBLE);
				redTextView.setText("");
				redTextView.setVisibility(View.VISIBLE);

			}
			break;
			default:
				break;
		}
		
	}
	
	public void didNotConfigure(RemoteGameInterfaceError reason)
	{
		refreshButton.setClickable(true);
		Log.d(LOG_TAG, "Did Not Configure");
	}
	
	public void didConnect()
	{
		Log.d(LOG_TAG, "Did Connect");
		try
		{
			RemoteGameInterfaceEngine.getDefaultEngine().register(code);
		}
		catch(Exception e)
		{
			showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
		}
	}
	
	public void didNotConnect(Exception ex)
	{
		showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
	}
	
	public void didDisconnect(Exception ex)
	{
		Log.e(LOG_TAG, "Did Disconnect");
	}
	
	public void didRegister()
	{
		Log.e(LOG_TAG, "Did Register");
		try
		{
			RemoteGameInterfaceEngine.getDefaultEngine().sendHandshake();
		}
		catch(Exception e){
			RemoteGameInterfaceEngine.getDefaultEngine().disconnect();
			showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
		}
	}
	
	public void didNotRegister(Exception ex)
	{
		RemoteGameInterfaceEngine.getDefaultEngine().disconnect();
		showError("A ligação foi interrompida!\nPor favor tenta novamente.", "", true);
	}
	
	public void didReceiveHandshakeResponse(){
		progressBar.setVisibility(View.GONE);
		refreshButton.setVisibility(View.GONE);
		seeInMapButton.setVisibility(View.GONE);
		redTextView.setVisibility(View.GONE);
		playerNameEditText.setVisibility(View.GONE);
		codeEditText.setVisibility(View.GONE);
		
		startGameButton.setVisibility(View.VISIBLE);
		startGameButton.setClickable(true);
		greyTextView.setVisibility(View.VISIBLE);
		greyTextView.setText("Carrega em \"Começar a Jogar\" para\ncomeçares a jogar.");
		
		
	}
	public void didReceiveScore(float score){}
	public void didReceiveFinish(float score){}
	
	private void showError(String greyText, String redText, boolean showConnectButton)
	{
		progressBar.setVisibility(View.GONE);
		connectButton.setVisibility(View.GONE);
		seeInMapButton.setVisibility(View.GONE);
		
		if(showConnectButton)
		{
			refreshButton.setVisibility(View.GONE);
			seeInMapButton.setVisibility(View.GONE);
			connectButton.setVisibility(View.VISIBLE);
			
			playerNameEditText.setVisibility(View.VISIBLE);
			codeEditText.setVisibility(View.VISIBLE);
			connectButton.setClickable(true);
		}
		else
		{
			refreshButton.setVisibility(View.VISIBLE);
			seeInMapButton.setVisibility(View.GONE);
			connectButton.setVisibility(View.GONE);

			playerNameEditText.setVisibility(View.GONE);
			codeEditText.setVisibility(View.GONE);
		}
		
		if(greyText != null && !greyText.equals(""))
		{
			greyTextView.setText(greyText);
		}
		else
		{
			greyTextView.setVisibility(View.GONE);
		}
		
		if(redText != null && !redText.equals(""))
		{
			redTextView.setText(redText);
		}
		else
		{
			redTextView.setVisibility(View.GONE);
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
      float x=event.values[0] / SensorManager.GRAVITY_EARTH;
      float y=event.values[1] / SensorManager.GRAVITY_EARTH;
      float z=event.values[2] / SensorManager.GRAVITY_EARTH;

      try
      {
      	RemoteGameInterfaceEngine.getDefaultEngine().sendControlData(x, y, z, false,false);
      }
      catch(Exception e)
      {
      	Log.e(LOG_TAG,"Error occurred while sending data: ",e);
      }
      
      
  }
		
	}
}