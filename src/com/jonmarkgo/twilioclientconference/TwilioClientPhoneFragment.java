package com.jonmarkgo.twilioclientconference;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class TwilioClientPhoneFragment extends Fragment {
	private MonkeyPhone phone;
	private String phonenumber;
	private String pnsid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		phone = new MonkeyPhone(getActivity());
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_phone, parent, false);

		phonenumber = PreferenceManager.getDefaultSharedPreferences(
				getActivity()).getString(getString(R.string.phonenumber_key),
				null);
		pnsid = PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getString(getString(R.string.pnsid_key), null);
		getActivity().setTitle(phonenumber);

		Button joinButton = (Button) v
				.findViewById(R.id.conference_join_button);
		joinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(), getString(R.string.dial_message),
						Toast.LENGTH_LONG).show();
				phone.connect();

			}
		});

		Button endButton = (Button) v.findViewById(R.id.conference_end_button);
		endButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getActivity(),
						getString(R.string.hangup_message), Toast.LENGTH_LONG)
						.show();
				phone.disconnect();

			}
		});

		Button resetButton = (Button) v
				.findViewById(R.id.conference_reset_button);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new DeletePhoneNumberTask(
						(TwilioClientconferenceActivity) getActivity())
						.execute(pnsid);

			}
		});
		return v;
	}

	private class DeletePhoneNumberTask extends
			AsyncTask<String, Integer, Boolean> {
		private ProgressDialog dialog;
		private TwilioClientconferenceActivity activity;
		private Context context;

		public DeletePhoneNumberTask(TwilioClientconferenceActivity activity) {
			this.activity = activity;
			context = activity;
			dialog = new ProgressDialog(context);
		}

		protected void onPreExecute() {
			this.dialog.setMessage(getString(R.string.reset_message));
			this.dialog.show();
		}

		protected Boolean doInBackground(String... pnsid) {
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

				HttpDelete httpdelete = new HttpDelete(
						"https://api.twilio.com/2010-04-01/Accounts/"
								+ getString(R.string.account_sid)
								+ "/IncomingPhoneNumbers/" + pnsid[0] + ".json");
				// httpdelete.setEntity(new UrlEncodedFormEntity(pairs));
				HttpResponse response = httpclient.execute(httpdelete);
				if (response.getStatusLine().getStatusCode() == 204) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				Log.e(getString(R.string.log_key),
						"Error in http connection for DELETE " + e.toString());
				return false;
			}

		}

		protected void onPostExecute(final Boolean success) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			if (success) {
				Toast.makeText(context, getString(R.string.deleted_message),
						Toast.LENGTH_LONG).show();
				PreferenceManager.getDefaultSharedPreferences(getActivity())
						.edit().remove(getString(R.string.phonenumber_key))
						.commit();
				((TwilioClientconferenceActivity) getActivity())
						.switchToSetupFragment();
			} else {
				Toast.makeText(context, getString(R.string.error_message),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
