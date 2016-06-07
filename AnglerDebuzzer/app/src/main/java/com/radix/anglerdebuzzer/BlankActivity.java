package com.radix.anglerdebuzzer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Gets the service up. Closes after.
 */
public class BlankActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);

        // start the service
        startService(new Intent(this, BackgroundService.class));

        // close the activity since our job is done
        finish();
    }
}
