
package com.jonmarkgo.twilioclientconference;

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
    private MonkeyPhone mPhone;
    private String mPhoneNumber;
    private String mPNSID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhone = new MonkeyPhone(getActivity());
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
            Bundle savedInstanceState) {
        getActivity().setTitle(mPhoneNumber);

        View v = inflater.inflate(R.layout.fragment_phone, parent, false);

        mPhoneNumber = PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getString(getString(R.string.phonenumber_key),
                null);
        mPNSID = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pnsid_key), null);

        Button joinButton = (Button) v
                .findViewById(R.id.conference_join_button);
        joinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.dial_message),
                        Toast.LENGTH_LONG).show();
                mPhone.connect();

            }
        });

        Button endButton = (Button) v.findViewById(R.id.conference_end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        getString(R.string.hangup_message), Toast.LENGTH_LONG)
                        .show();
                mPhone.disconnect();

            }
        });

        Button resetButton = (Button) v
                .findViewById(R.id.conference_reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new DeletePhoneNumberTask(
                        (TwilioClientConferenceActivity) getActivity())
                        .execute(mPNSID);

            }
        });
        return v;
    }

    private class DeletePhoneNumberTask extends
            AsyncTask<String, Integer, Boolean> {

        // Hmm. It's uncommon to put GUI items inside an AsyncTask, but as long
        // as you're also
        // passing in the context like you're doing, this should be OK.
        private ProgressDialog mDialog;
        private Context mContext;

        public DeletePhoneNumberTask(TwilioClientConferenceActivity activity) {
            mContext = activity;
            mDialog = new ProgressDialog(mContext);
        }

        protected void onPreExecute() {
            this.mDialog.setMessage(getString(R.string.reset_message));
            this.mDialog.show();
        }

        protected Boolean doInBackground(String... pnsid) {
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
                HttpResponse response = httpclient.execute(httpdelete);
                if (response.getStatusLine().getStatusCode() == 204) {
                    return true;
                } else {
                    Log.e(getString(R.string.log_key), "Delete response code: "
                            + response.getStatusLine().getStatusCode());
                    return false;
                }
            } catch (Exception e) {
                Log.e(getString(R.string.log_key),
                        "Error in http connection for DELETE " + e.toString());
                return false;
            }

        }

        protected void onPostExecute(final Boolean success) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (success) {
                Toast.makeText(mContext, getString(R.string.deleted_message),
                        Toast.LENGTH_LONG).show();
                mPhone.shutdown();
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit().remove(getString(R.string.phonenumber_key))
                        .remove(getString(R.string.pnsid_key)).commit();
                ((TwilioClientConferenceActivity) getActivity())
                        .switchToSetupFragment();
            } else {
                Toast.makeText(mContext, getString(R.string.error_message),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
