package com.threegiants.e_three_kingdom;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    public static final String STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Verify the storage permissions for an activity.
     * Only READ_EXTERNAL_STORAGE for now.
     * @param activity The activity which need to be verified.
     */
    public static void verifyStoragePermissions(Activity activity) {
        try {
            int permission = ActivityCompat.checkSelfPermission(
                    activity,
                    STORAGE_PERMISSION
            );
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{ STORAGE_PERMISSION },
                        0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
