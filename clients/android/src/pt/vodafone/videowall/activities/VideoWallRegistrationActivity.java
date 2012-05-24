package pt.vodafone.videowall.activities;

import com.blissapplications.java.remotegameinterface.engine.IRemoteGameInterfaceEngineDelegate;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceAvailability;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceConfiguration;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceEngine;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceError;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceState;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceStatus;
import com.blissapplications.remotegameinterface.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VideoWallRegistrationActivity extends Activity implements IRemoteGameInterfaceEngineDelegate
{
	public final static String LOG_TAG = "RGI";
	
	private TextView greyTextView;
	private TextView redTextView;
	private EditText playerNameEditText;
	private EditText codeEditText;
	private Button connectButton;
	private Button startGameButton;
	private Button refreshButton;
	private Button seeInMapButton;
	private ProgressBar progressBar;
	
	private String playerName;
	private String code;

  
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_wall_registration_layout);
    
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
		
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		hideAllControls();
		
		greyTextView.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setClickable(false);
		greyTextView.setText("A ligar...");
		
		
		try
		{
			RemoteGameInterfaceEngine.getDefaultEngine().addDelegate(this);
			
			refresh(); 
			
		}
		catch(Exception ex)
		{
			Log.e(LOG_TAG,"Error: ", ex);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		try
		{
			RemoteGameInterfaceEngine.getDefaultEngine().removeDelegate(this);
		}
		catch(Exception ex)
		{
			
		}
	}
	
	public void didConfigure()
	{
		hideAllControls();
		
		greyTextView.setVisibility(View.VISIBLE);
		refreshButton.setClickable(false);
		progressBar.setVisibility(View.VISIBLE);
		refreshButton.setVisibility(View.VISIBLE);
		greyTextView.setText("A ligar...");
		try
		{
			
			RemoteGameInterfaceAvailability availability = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Availability;
			
			if(availability.equals(RemoteGameInterfaceAvailability.BeforeEvent))
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.VISIBLE);
				greyTextView.setVisibility(View.VISIBLE);
				
				greyTextView.setText("Poderás jogar este jogo durante o festival, no espaço\nVodafone Showcases.");
				redTextView.setText("Esperamos por ti!");
			}
			else if(availability.equals(RemoteGameInterfaceAvailability.AfterEvent))
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				redTextView.setVisibility(View.VISIBLE);
				greyTextView.setVisibility(View.VISIBLE);
				
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
			
		}
	}
	
	public void didCheckState()
	{
		hideAllControls();
		
		refreshButton.setVisibility(View.VISIBLE);
		refreshButton.setClickable(false);
		
		RemoteGameInterfaceState state = RemoteGameInterfaceEngine.getDefaultEngine().getState();
		
		switch (state) {
			case AdHocInfrastrucureAndNotConnectedToCorrectAccessPoint:
			{
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
				
			}
			break;
			case AllOK:
			{
				refreshButton.setClickable(true);
				
				codeEditText.setVisibility(View.VISIBLE);
				playerNameEditText.setVisibility(View.VISIBLE);
				connectButton.setVisibility(View.VISIBLE);
				
				greyTextView.setText("Escolhe um nome e insere o código que vês na Play Wall para começares a jogar.");
				redTextView.setText("");
				greyTextView.setVisibility(View.VISIBLE);
			}
			break;
			case LocationNotAvailable:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				
				greyTextView.setText("Serviço de localização inactivo.");
				redTextView.setText("Activa o serviço de localização nas Definições.");
			}
			break;
			case NotAvailable:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				
				greyTextView.setText("O jogo não está disponível neste momento.");
				redTextView.setText("Por favor tenta mais tarde!");
			}
			break;
			case NotCloseEnough:
			{
				refreshButton.setClickable(true);
				refreshButton.setVisibility(View.VISIBLE);
				
				
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
		showError("Ocorreu um erro!\nPor favor tenta novamente.", "", true);
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
		catch(Exception e)
		{
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
		hideAllControls();
		
		startGameButton.setVisibility(View.VISIBLE);
		startGameButton.setClickable(true);
		greyTextView.setVisibility(View.VISIBLE);
		greyTextView.setText("Carrega em \"Começar a Jogar\" para\ncomeçares a jogar.");
		
		
	}
	
	/* Not Used*/
	public void didReceiveScore(float score){}
	
	public void didReceiveFinish(float score){}
	
	/* Private Helper Methods*/
	
	private void showError(String greyText, String redText, boolean showConnectButton)
	{
		hideAllControls();
		
		if(showConnectButton)
		{
			playerNameEditText.setVisibility(View.VISIBLE);
			codeEditText.setVisibility(View.VISIBLE);
			
			connectButton.setVisibility(View.VISIBLE);
			connectButton.setClickable(true);
		}
		else
		{
			refreshButton.setVisibility(View.VISIBLE);
		}
		
		if(greyText != null && !greyText.equals(""))
		{
			greyTextView.setText(greyText);
			greyTextView.setVisibility(View.VISIBLE);
		}
		
		if(redText != null && !redText.equals(""))
		{
			redTextView.setText(redText);
			redTextView.setVisibility(View.VISIBLE);
		}
		
	}

	private void refresh(){
		hideAllControls();
		
		refreshButton.setClickable(false);
		refreshButton.setVisibility(View.VISIBLE);
		hideAllControls();
		RemoteGameInterfaceStatus status = RemoteGameInterfaceEngine.getDefaultEngine().getStatus();
		RemoteGameInterfaceState state = RemoteGameInterfaceEngine.getDefaultEngine().getState();
		
		if(status.equals(RemoteGameInterfaceStatus.Newborn))
		{
			RemoteGameInterfaceEngine.getDefaultEngine().configure();
			progressBar.setVisibility(View.VISIBLE);
			greyTextView.setVisibility(View.VISIBLE);
			greyTextView.setText("A ligar...");
		}
		else if(!state.equals(RemoteGameInterfaceState.AllOK))
		{
			RemoteGameInterfaceEngine.getDefaultEngine().checkState(VideoWallRegistrationActivity.this);
			progressBar.setVisibility(View.VISIBLE);
			greyTextView.setVisibility(View.VISIBLE);
			greyTextView.setText("A ligar...");
		}
		else
		{
			refreshButton.setClickable(true);
			greyTextView.setText("Escolhe um nome e insere o código que vês na Play Wall para começares a jogar.");
			playerNameEditText.setVisibility(View.VISIBLE);
			codeEditText.setVisibility(View.VISIBLE);
			connectButton.setVisibility(View.VISIBLE);
			connectButton.setClickable(true);
		}
		
	}
	
	private void setRefreshButtonListeners()
	{
		refreshButton.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View v) 
			{
				refresh();
			}
		});
	}
	
	private void setSeeInMapButtonListeners()
	{
		seeInMapButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
			}
		});
	}
	
	private void setStartGameButtonListeners()
	{
		startGameButton.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View v) 
			{
				try
				{
					RemoteGameInterfaceEngine.getDefaultEngine().sendStartGame(playerName);
					startActivity(new Intent(VideoWallRegistrationActivity.this, VideoWallGameActivity.class));
				}
				catch (Exception e) 
				{
					Log.e(LOG_TAG, "Error occurred: ", e);
					showError("Ocorreu um erro!\nPor favor tenta novamente.", "", true);
				}
			}
		});
	}
	
	private void setConnectButtonListeners()
	{
		connectButton.setOnClickListener(new View.OnClickListener() 
		{
			
			public void onClick(View v) 
			{
				hideAllControls();
				
				connectButton.setClickable(false);
				String host = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Endpoint;
				int port = RemoteGameInterfaceEngine.getDefaultEngine().getConfiguration().Port;
				
				playerName = playerNameEditText.getText().toString();
				code = codeEditText.getText().toString();
				
				playerNameEditText.setVisibility(View.VISIBLE);
				codeEditText.setVisibility(View.VISIBLE);
				
				progressBar.setVisibility(View.VISIBLE);
				connectButton.setVisibility(View.VISIBLE);
				connectButton.setClickable(false);
				
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
	
	private void hideAllControls()
	{
		connectButton.setVisibility(View.GONE);
		seeInMapButton.setVisibility(View.GONE);
		refreshButton.setVisibility(View.GONE);
		startGameButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		playerNameEditText.setVisibility(View.GONE);
		codeEditText.setVisibility(View.GONE);
		greyTextView.setVisibility(View.GONE);
		redTextView.setVisibility(View.GONE);
	}
	
}