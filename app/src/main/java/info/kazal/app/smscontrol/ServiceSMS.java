/**
 * 
 */
package info.kazal.app.smscontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * @author HaiderAli
 *
 */
public class ServiceSMS extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub	
		Bundle bundle = arg1.getExtras();        
		SmsMessage[] messages = null;
		String messageBody = "";            
		
		if (bundle != null)	{
			senderNumber = "";
			Object[] pdus = (Object[]) bundle.get("pdus");
			messages = new SmsMessage[pdus.length];
			for (int counter = 0; counter < messages.length; counter++)	{
				messages[counter] = SmsMessage.createFromPdu((byte[])pdus[counter]);
				if (counter == 0) {				
					senderNumber = messages[counter].getOriginatingAddress();					
				} 
				messageBody += messages[counter].getMessageBody().toString();                	
			}								
			this.abortBroadcast();									
			if (messageBody.compareToIgnoreCase("Your Location") == 0) {            							
				locationManager = (LocationManager)	arg0.getSystemService(Context.LOCATION_SERVICE);    				
				locationListener = new LocationFinder();				
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 1000, locationListener);																
			}
			else if(messageBody.compareToIgnoreCase("Call Me") == 0)
			{
				try 
				{
				    Intent callIntent = new Intent(Intent.ACTION_CALL);
				    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    callIntent.setData(Uri.parse("tel:" + senderNumber));
				    arg0.startActivity(callIntent);
				}
				catch (Exception e) 
				{
				    Toast.makeText(arg0, "Cannot make phone call", Toast.LENGTH_SHORT).show();
				}
			}
			else if(messageBody.compareToIgnoreCase("Play Music") == 0)
			{
				if(mediaPlayer == null)
				{
					mediaPlayer = MediaPlayer.create(arg0, Settings.System.DEFAULT_RINGTONE_URI);					
				}
				mediaPlayer.setVolume(1, 1);
				mediaPlayer.start();
			}
			else if(messageBody.compareToIgnoreCase("Stop Music") == 0)
			{
				try {
					if(mediaPlayer == null)
					{
						return;
					}
					else if(mediaPlayer != null || mediaPlayer.isPlaying())
					{						
						mediaPlayer.stop();
						mediaPlayer.reset();
						mediaPlayer.release();
						mediaPlayer = null;
					}
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block					
				}								
			}
		}
	}
	
	private class LocationFinder implements LocationListener
	{

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			if (location != null) {				
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(senderNumber, null,	"Lattitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude() + ", Altitude: " + location.getAltitude() + ", Accuracy: " + location.getAccuracy(), null, null);                
				locationManager.removeUpdates(locationListener);                
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private String senderNumber;
	private static MediaPlayer mediaPlayer; 
}
