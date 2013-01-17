package com.jonmarkgo.twilioclientconference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.twilio.client.Connection;
import com.twilio.client.Device;
import com.twilio.client.Twilio;
 
public class MonkeyPhone implements Twilio.InitListener
{
    private Device device;
    private Connection connection;
    private Context context;
    public MonkeyPhone(Context context)
    {
    	this.context = context;
        Twilio.initialize(context, this /* Twilio.InitListener */);
    }
 
    @Override /* Twilio.InitListener method */
    public void onInitialized()
    {
        Log.d("sdk", "Twilio SDK is ready");
        new GetCapabilityTokenTask((TwilioClientconferenceActivity)context).execute();
        
    }
 
    @Override /* Twilio.InitListener method */
    public void onError(Exception e)
    {
        Log.e("sdk", "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }
 
    public void connect()
    {
        connection = device.connect(null /* parameters */, null /* ConnectionListener */);
        if (connection == null)
            Log.w("sdk", "Failed to create new connection");
    }
    public void disconnect()
    {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
    @Override
    protected void finalize()
    {
        if (connection != null)
            connection.disconnect();
        if (device != null)
            device.release();
    }
    
    private class GetCapabilityTokenTask extends AsyncTask<String, Integer, String> {
    	private ProgressDialog dialog;
    	private TwilioClientconferenceActivity activity;
    	private Context context;
    	 public GetCapabilityTokenTask(TwilioClientconferenceActivity activity) {
    		 this.activity = activity;
             context = activity;
             dialog = new ProgressDialog(context);
         }
    	  protected void onPreExecute() {
              this.dialog.setMessage("Loading...");
              this.dialog.show();
          }
       
    	@Override
        protected String doInBackground(String... receivers) {
            InputStream is = null;
            int count = receivers.length;
            long totalSize = 0;
            HttpClient httpclient = new DefaultHttpClient();
Log.d("sdk", "getting cap token");
            try {
            HttpGet httpget = new HttpGet("http://twilio.jonmarkgo.com/cap.php");
            HttpResponse response = httpclient.execute(httpget);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String token = reader.readLine();
            Log.d("cap token", token);
            return token;

        } catch (Exception e) {
            Log.e("HTTP", "Error in http connection " + e.toString());
            return new String("ERROR");
        }
         
        }
    	@Override
        protected void onPostExecute(String token) {
    		   
            if (dialog.isShowing()) {
            dialog.dismiss();
            }

      
    		Log.d("cap token 2", token);
    		try {
                device = Twilio.createDevice(token, null /* DeviceListener */);
                if (device != null) {
                    Toast.makeText(context, "READY!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            	Toast.makeText(context, "Error!", Toast.LENGTH_LONG).show();
                Log.e("sdk", "Failed to obtain capability token: " + e.getLocalizedMessage());
            }
        }
    }
}