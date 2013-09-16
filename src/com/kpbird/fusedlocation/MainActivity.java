package com.kpbird.fusedlocation;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener,LocationListener {

  private static final Logger              log              = LoggerFactory.getLogger(MainActivity.class);

	private String TAG = MainActivity.class.getSimpleName();
	
	private TextView txtConnectionStatus;
	private TextView txtLastKnownLoc;
	private EditText etLocationInterval;
  private EditText eDistance;
	private TextView txtLocationRequest;
  private ListView locationHistory;

  ArrayAdapter<String> arrayAdapter;
  ArrayList<locationInfo> locations = new ArrayList<locationInfo>();

	private LocationClient locationclient;
	private LocationRequest locationrequest;
	private Intent mIntentService;

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList("locations",locations);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    locations = savedInstanceState.getParcelableArrayList("locations");
    arrayAdapter.notifyDataSetChanged();
  }

  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

    if( savedInstanceState != null ){
      locations = savedInstanceState.getParcelableArrayList("locations");
    }
		txtConnectionStatus = (TextView) findViewById(R.id.txtConnectionStatus);
		txtLastKnownLoc = (TextView) findViewById(R.id.txtLastKnownLoc);
		etLocationInterval = (EditText) findViewById(R.id.etLocationInterval);
    eDistance = (EditText)findViewById(R.id.eDistance);
		txtLocationRequest = (TextView) findViewById(R.id.txtLocationRequest);
		
		mIntentService = new Intent(this,LocationService.class);

    locationHistory = (ListView)findViewById(R.id.locationHistory);
    arrayAdapter = new ArrayAdapter (this, android.R.layout.simple_list_item_2, android.R.id.text1, locations) {

      @Override
      public View getView( int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

        locationInfo data = locations.get(position);
        text1.setText(data.getCoordinates());
        text2.setText(data.getDate());

        return view;

      }
    };
    locationHistory.setAdapter(arrayAdapter);

		int resp =GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if(resp == ConnectionResult.SUCCESS){
			locationclient = new LocationClient(this,this,this);
			locationclient.connect();
		}
		else{
			Toast.makeText(this, "Google Play Service Error " + resp, Toast.LENGTH_LONG).show();
		}
	}
	
	public void buttonClicked(View v){
		if(v.getId() == R.id.btnLastLoc){
			if(locationclient!=null && locationclient.isConnected()){
				Location loc =locationclient.getLastLocation();
        if( loc != null ){
          Log.i(TAG, "Last Known Location :" + loc.getLatitude() + "," + loc.getLongitude());
          txtLastKnownLoc.setText(loc.getLatitude() + "," + loc.getLongitude());
        } else {
          // Maybe location is not enabled?
          startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
			}
		}
		if(v.getId() == R.id.btnStartRequest){
			if(locationclient!=null && locationclient.isConnected()){
				
				if(((Button)v).getText().equals("Start")){
					locationrequest = LocationRequest.create();
          locationrequest.setFastestInterval(5 * 1000);
          locationrequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
					locationrequest.setInterval(Long.parseLong(etLocationInterval.getText().toString()));
          locationrequest.setSmallestDisplacement(Float.valueOf(eDistance.getText().toString()));
					locationclient.requestLocationUpdates(locationrequest, this);
					((Button) v).setText("Stop");	
				}
				else{
					locationclient.removeLocationUpdates(this);
					((Button) v).setText("Start");
				}
				
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(locationclient!=null)
			locationclient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "onConnected");
		txtConnectionStatus.setText("Connection Status : Connected");
		
	}

	@Override
	public void onDisconnected() {
		Log.i(TAG, "onDisconnected");
		txtConnectionStatus.setText("Connection Status : Disconnected");
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "onConnectionFailed");
		txtConnectionStatus.setText("Connection Status : Fail");

	}

	@Override
	public void onLocationChanged(Location location) {
		if(location!=null){
			Log.i(TAG, "Location Request :" + location.getLatitude() + "," + location.getLongitude());
      String locationUpdate = location.getLatitude() + "," + location.getLongitude();
			txtLocationRequest.setText(locationUpdate);

      logLocationUpdate(location);
		}
	}

  public void logLocationUpdate(Location location) {
    String locInfo = location.toString();
    locInfo = locInfo.replaceFirst("^Location\\[", "");
    int sL = locInfo.length();
    if (sL > 1) {
      locInfo = locInfo.substring(0, sL - 1);
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.US);
      StringBuffer subtitle = new StringBuffer();
      String accuracy = Float.toString(location.getAccuracy());
      subtitle.append( sdf.format(location.getTime()) );
      subtitle.append( " " + accuracy + "m" );
      DecimalFormat df = new DecimalFormat("##.######", new DecimalFormatSymbols(Locale.ENGLISH));

      Double curLat = Double.parseDouble(df.format(location.getLatitude()));
      Double curLng = Double.parseDouble(df.format(location.getLongitude()));
      locations.add(new locationInfo(curLat + "," + curLng, subtitle.toString()));
      arrayAdapter.notifyDataSetChanged();

      StringBuffer whatToLog = new StringBuffer();
      whatToLog.append(sdf.format(location.getTime()) + ",");
      whatToLog.append(location.getProvider() + ",");
      whatToLog.append(locInfo);
      // logFile.logToFile(whatToLog.toString());
      log.info(whatToLog.toString());

    }
  }
}
