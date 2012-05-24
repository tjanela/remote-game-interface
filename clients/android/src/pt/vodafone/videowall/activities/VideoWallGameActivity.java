package pt.vodafone.videowall.activities;

import com.blissapplications.java.remotegameinterface.engine.IRemoteGameInterfaceEngineDelegate;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceEngine;
import com.blissapplications.java.remotegameinterface.engine.RemoteGameInterfaceError;
import com.blissapplications.remotegameinterface.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class VideoWallGameActivity extends Activity implements IRemoteGameInterfaceEngineDelegate, SensorEventListener
{
	public static final String LOG_TAG = "VideoWallGame";
	private SensorManager sensorManager;
  private Sensor accelerometer;
  
  private Button aButton;
  private Button bButton;
  
  private boolean aButtonPressed = false;
  private boolean bButtonPressed = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
  	super.onCreate(savedInstanceState);
  	setContentView(R.layout.video_wall_game_layout);
  	
  	sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
  	accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
  	
  	aButton = (Button) findViewById(R.id.aButton);
  	bButton = (Button) findViewById(R.id.bButton);
  	
  	aButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						aButtonPressed = true;
						break;
					default:
						aButtonPressed = false;
						break;
				}
				return true;
			}
			
		});
  	
  	bButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						bButtonPressed = true;
						break;
					default:
						bButtonPressed = false;
						break;
				}
				return true;
			}
		});
  }
  
	@Override
	protected void onResume() 
	{
		super.onResume();		
		RemoteGameInterfaceEngine.getDefaultEngine().addDelegate(this);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		RemoteGameInterfaceEngine.getDefaultEngine().removeDelegate(this);
		sensorManager.unregisterListener(this);
	}
	
	/* Control */

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
		{
      float x = event.values[1] / SensorManager.GRAVITY_EARTH;
      float y = - event.values[0] / SensorManager.GRAVITY_EARTH;
      float z = event.values[2] / SensorManager.GRAVITY_EARTH;

      try
      {
      	RemoteGameInterfaceEngine.getDefaultEngine().sendControlData(x, y, z, aButtonPressed, bButtonPressed);
      }
      catch(Exception e)
      {
      	Log.e(LOG_TAG, "Error occurred while sending control data: ",e);
      }  
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	/* */

	@Override
	public void didReceiveFinish(float score)
	{ 
		sensorManager.unregisterListener(this);
		RemoteGameInterfaceEngine.getDefaultEngine().disconnect();
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Fim de jogo");
		dialogBuilder.setMessage("A tua pontuação:\n"+((int)Math.round(score)));
		dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				VideoWallGameActivity.this.finish();				
			}
		});
		
		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
	}

	@Override
	public void didDisconnect(Exception ex)
	{ 
		sensorManager.unregisterListener(this);
		RemoteGameInterfaceEngine.getDefaultEngine().disconnect();
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Ocorreu um erro");
		dialogBuilder.setMessage("A ligação foi interrompida!\nPor favor tenta novamente.");
		dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				VideoWallGameActivity.this.finish();				
			}
		});
		
		AlertDialog dialog = dialogBuilder.create();
		dialog.show();
	}
	
	/*
	 * Not used
	 */

	@Override
	public void didReceiveScore(float score) { }
	
	@Override
	public void didConfigure(){ }

	@Override
	public void didNotConfigure(RemoteGameInterfaceError reason){ }

	@Override
	public void didConnect(){ }

	@Override
	public void didCheckState(){ }

	@Override
	public void didNotConnect(Exception ex){ }

	@Override
	public void didRegister(){ }

	@Override
	public void didNotRegister(Exception ex){ }

	@Override
	public void didReceiveHandshakeResponse(){ }
	
}
