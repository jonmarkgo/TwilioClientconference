package com.jonmarkgo.twilioclientconference;

import java.io.BufferedReader;
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

public class MonkeyPhone implements Twilio.InitListener {
	private Device device;
	private Connection connection;
	private Context context;

	public MonkeyPhone(Context context) {
		this.context = context;
		Log.d(context.getString(R.string.log_key), "new MonkeyPhone");

		Twilio.initialize(context, this /* Twilio.InitListener */);

	}

	public void shutdown() {
		Twilio.shutdown();
	}

	@Override
	/* Twilio.InitListener method */
	public void onInitialized() {
		Log.d(context.getString(R.string.log_key), "Twilio SDK is ready");
		new GetCapabilityTokenTask((TwilioClientconferenceActivity) context)
				.execute();

	}

	@Override
	/* Twilio.InitListener method */
	public void onError(Exception e) {
		Log.e(context.getString(R.string.log_key),
				"Twilio SDK couldn't start: " + e.getLocalizedMessage());
	}

	public void connect() {
		connection = device
				.connect(null /* parameters */, null /* ConnectionListener */);
		if (connection == null)
			Log.w(context.getString(R.string.log_key),
					"Failed to create new connection");
	}

	public void disconnect() {
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}
	}

	@Override
	protected void finalize() {
		if (connection != null)
			connection.disconnect();
		if (device != null)
			device.release();
	}

	private class GetCapabilityTokenTask extends
			AsyncTask<String, Integer, String> {
		private ProgressDialog dialog;
		private Context context;

		public GetCapabilityTokenTask(TwilioClientconferenceActivity activity) {

			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			this.dialog.setMessage(context.getString(R.string.loading_message));
			this.dialog.show();
		}

		@Override
		protected String doInBackground(String... receivers) {
			HttpClient httpclient = new DefaultHttpClient();
			Log.d(context.getString(R.string.log_key), "getting cap token");
			try {
				HttpGet httpget = new HttpGet(
						context.getString(R.string.capability_url));
				HttpResponse response = httpclient.execute(httpget);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String token = reader.readLine();
				Log.d(context.getString(R.string.log_key), "Capability token: "
						+ token);
				return token;

			} catch (Exception e) {
				Log.e(context.getString(R.string.log_key),
						"Error in http connection " + e.toString());
				return new String("ERROR");
			}

		}

		@Override
		protected void onPostExecute(String token) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			try {
				device = Twilio.createDevice(token, null /* DeviceListener */);
				if (device != null) {
					Toast.makeText(
							context,
							context.getString(R.string.capability_success_message),
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context,
							context.getString(R.string.error_message),
							Toast.LENGTH_LONG).show();
				}
			} catch (Exception e) {
				Toast.makeText(context,
						context.getString(R.string.error_message),
						Toast.LENGTH_LONG).show();
				Log.e(context.getString(R.string.log_key),
						"Failed to obtain capability token: "
								+ e.getLocalizedMessage());
			}
		}
	}
}