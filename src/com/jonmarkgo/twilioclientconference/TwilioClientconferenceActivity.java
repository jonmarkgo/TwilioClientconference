
package com.jonmarkgo.twilioclientconference;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class TwilioClientConferenceActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            String phonenumber = PreferenceManager.getDefaultSharedPreferences(
                    this).getString(getString(R.string.phonenumber_key), null);
            String pnsid = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(getString(R.string.pnsid_key), null);
            if (phonenumber != null && pnsid != null) {
                Log.d(getString(R.string.log_key), "opening phone fragment");
                fragment = new TwilioClientPhoneFragment();
            } else {
                Log.d(getString(R.string.log_key), "opening setup fragment");
                fragment = new TwilioClientSetupFragment();
            }
            manager.beginTransaction().add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    public void switchToPhoneFragment() {
        Fragment fragment = new TwilioClientPhoneFragment();

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragmentContainer, fragment);

        transaction.commit();

    }

    public void switchToSetupFragment() {
        Fragment fragment = new TwilioClientSetupFragment();

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.fragmentContainer, fragment);

        transaction.commit();

    }
}
