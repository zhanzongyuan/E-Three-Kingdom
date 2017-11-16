package com.threegiants.e_three_kingdom;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzy on 2017/11/16.
 */

public class CharactersActivity extends AppCompatActivity {

    public static final String STORAGE_PERMISSION = "android.permission.READ_EXTERNAL_STORAGE";

    public boolean storagePermissionsGranted = false;

    //Characters Data
    List<Character> characterData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verify the storage permission.
        verifyStoragePermissions(this);

        // Import data from db.
        importData();
    }


    // Import data from db
    protected void importData(){
        characterData=new ArrayList<>();

        // Open our database.
        DataBaseHelper mHelper = new DataBaseHelper(this);
        SQLiteDatabase db = mHelper.getPackageDatabase();

        // Query all characters.
        Cursor mCursor = db.rawQuery("SELECT * FROM characters", null);

        // Generate character object for each row, and add it to characterData.
        while (mCursor.moveToNext()) {
            String name = mCursor.getString(1);
            String short_detail = mCursor.getString(2);
            byte[] iconArr = mCursor.getBlob(3);
            Bitmap icon = BitmapFactory.decodeByteArray(iconArr, 0, iconArr.length);
            //need to update @
            //id、name、icon、gender、birth、hometown、camp
            //id is unique
            Character curCharacter = new Character("", name, icon, short_detail, "", "", "");
            characterData.add(curCharacter);
        }

        mCursor.close();
        db.close();
    }


    /**
     * Verify the storage permissions.
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

    /**
     * Event handler when receive the allow/denied choice
     * of permissions from user.
     * @param requestCode The request code.
     * @param permissions The permissions array.
     * @param grantResults The grant results array.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            switch(i) {
                case 0: // STORAGE_PERMISSION
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.exit(0);
                    } else {
                        storagePermissionsGranted = true;
                    }

            }
        }
    }

}
