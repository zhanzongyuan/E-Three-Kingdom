package com.threegiants.e_three_kingdom;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.renderscript.ScriptGroup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by r0bert on 17-11-14.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    // The path of .db file.
    private static String DB_PATH = Environment.getExternalStorageDirectory().getPath();

    // The name of .db file.
    private static String DB_NAME = "dictionary";

    // The database.
    private SQLiteDatabase mDatabase;

    // The context.
    private final Context mContext;

    /**
     * Constructor
     * Copy the .db file in assets if necessary.
     * Keeps a reference of the context which calls the helper.
     * @param context The context which calls the helper.
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);

        // Check if the .db file in external storage directory exists.
        String mPath = DB_PATH + DB_NAME;
        SQLiteDatabase checkDB = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        if (checkDB == null) {
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error(e);
            }
        } else {
            checkDB.close();
        }

        this.mContext = context;
    }

    private void copyDataBase() throws IOException {

        // Open the local .db file in assets.
        InputStream mInput = mContext.getAssets().open(DB_NAME);

        // Open the empty .db file.
        String outPath = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outPath);

        // Copy the .db file.
        byte[] buf = new byte[1024];
        int length;
        while ((length = mInput.read(buf)) > 0) {
            mOutput.write(buf, 0, length);
        }

        mOutput.flush();
        mOutput.close();
        mInput.close();

    }

    /**
     * Nothing to do for now, since the only things to do is to read characters.
     * @param db The database object.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        ;
    }

    /**
     * Noting to do for now, since no upgrades will be made.
     * @param db The database object.
     * @param oldVersion The old version number of the database.
     * @param newVersion The new version number of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ;
    }
}
