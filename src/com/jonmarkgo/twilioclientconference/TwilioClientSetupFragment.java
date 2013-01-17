package com.jonmarkgo.twilioclientconference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class TwilioClientSetupFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_conference_setup, parent,
				false);

		Button setupButton = (Button) v
				.findViewById(R.id.conference_setup_button);
		setupButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new BuyPhoneNumberTask(
						(TwilioClientconferenceActivity) getActivity())
						.execute();

			}
		});

		return v;
	}

	public void savePhoneNumber(String pnsid, String phonenumber) {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
				.putString(getString(R.string.phonenumber_key), phonenumber)
				.putString(getString(R.string.pnsid_key), pnsid).commit();
		((TwilioClientconferenceActivity) getActivity())
				.switchToPhoneFragment();
		Log.d(getString(R.string.log_key), "Phone number: " + phonenumber);
		Log.d(getString(R.string.log_key), "PNSid: " + pnsid);

	}

	private String inputStreamToString(InputStream is) {
		String s = "";
		String line = "";

		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		try {
			while ((line = rd.readLine()) != null) {
				s += line;
			}
		} catch (Exception e) {

		}
		// Return full string
		return s;
	}

	private class BuyPhoneNumberTask extends
			AsyncTask<String, Integer, Boolean> {
		private ProgressDialog dialog;
		private TwilioClientconferenceActivity activity;
		private Context context;

		public BuyPhoneNumberTask(TwilioClientconferenceActivity activity) {
			this.activity = activity;
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			this.dialog.setMessage(getString(R.string.setup_message));
			this.dialog.show();
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (success) {
				Toast.makeText(context, getString(R.string.purchased_message),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, getString(R.string.error_message),
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected Boolean doInBackground(String... unused) {
			InputStream is = null;

			long totalSize = 0;
			HttpClient httpclient = new DefaultHttpClient();

			((AbstractHttpClient) httpclient).getCredentialsProvider()
					.setCredentials(
							new AuthScope(null, -1),
							new UsernamePasswordCredentials(
									getString(R.string.account_sid),
									getString(R.string.auth_token)));

			try {
				ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("AreaCode",
						getString(R.string.area_code)));
				pairs.add(new BasicNameValuePair("VoiceApplicationSid",
						getString(R.string.app_sid)));
				HttpPost httppost = new HttpPost(
						"https://api.twilio.com/2010-04-01/Accounts/"
								+ getString(R.string.account_sid)
								+ "/IncomingPhoneNumbers.json");
				httppost.setEntity(new UrlEncodedFormEntity(pairs));
				HttpResponse response = httpclient.execute(httppost);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String json = reader.readLine();

				JSONObject jsonObject = new JSONObject(json);
				savePhoneNumber(jsonObject.getString("sid"),
						jsonObject.getString("friendly_name"));

				Log.d(getString(R.string.error_message), jsonObject.toString());

				return true;
			} catch (Exception e) {

				Log.e(getString(R.string.error_message),
						"Error in http connection POST " + e.toString());
				return false;
			}
		}
	}
}
