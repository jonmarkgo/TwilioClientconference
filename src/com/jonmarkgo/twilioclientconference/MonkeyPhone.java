
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
    private Device mDevice;
    private Connection mConnection;
    private Context mContext;

    public MonkeyPhone(Context context) {
        this.mContext = context;
        Log.d(context.getString(R.string.log_key), "new MonkeyPhone");

        Twilio.initialize(context, this /* Twilio.InitListener */);

    }

    public void shutdown() {
        Twilio.shutdown();
    }

    @Override
    /* Twilio.InitListener method */
    public void onInitialized() {
        Log.d(mContext.getString(R.string.log_key), "Twilio SDK is ready");
        new GetCapabilityTokenTask((TwilioClientConferenceActivity) mContext)
                .execute();

    }

    @Override
    /* Twilio.InitListener method */
    public void onError(Exception e) {
        Log.e(mContext.getString(R.string.log_key),
                "Twilio SDK couldn't start: " + e.getLocalizedMessage());
    }

    public void connect() {
        mConnection = mDevice
                .connect(null /* parameters */, null /* ConnectionListener */);
        if (mConnection == null)
            Log.w(mContext.getString(R.string.log_key),
                    "Failed to create new connection");
    }

    public void disconnect() {
        if (mConnection != null) {
            mConnection.disconnect();
            mConnection = null;
        }
    }

    @Override
    protected void finalize() {
        if (mConnection != null)
            mConnection.disconnect();
        if (mDevice != null)
            mDevice.release();
    }

    private class GetCapabilityTokenTask extends
            AsyncTask<String, Integer, String> {
        private ProgressDialog dialog;
        private Context context;

        public GetCapabilityTokenTask(TwilioClientConferenceActivity activity) {

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
                mDevice = Twilio.createDevice(token, null /* DeviceListener */);
                if (mDevice != null) {
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
