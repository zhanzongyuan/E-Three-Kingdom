package com.threegiants.e_three_kingdom;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The database helper of our project.
 */

public class DataBaseHelper extends SQLiteOpenHelper {

    // The path of .db file.
    private static String DB_PATH =
            Environment.getExternalStorageDirectory().getPath() +
            "/" +
            BuildConfig.APPLICATION_ID +
            "/databases/";

    // The name of .db file.
    private static String DB_NAME = "dictionary.db";

    // The database in assets.
    private SQLiteDatabase packageDatabase;

    // The context.
    private final Context mContext;

    SQLiteDatabase getPackageDatabase() {
        return packageDatabase;
    }

    /**
     * Constructor
     * Copy the .db file in assets if necessary.
     * Keeps a reference of the context which calls the helper.
     * @param context The context which calls the helper.
     */
    DataBaseHelper(Context context) {
        super(context, DB_NAME, null, 1);

        // Check if the .db file in external storage directory exists.
        String mPath = DB_PATH + DB_NAME;
        SQLiteDatabase checkDB =
                SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);
        if (checkDB == null) {
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error(e);
            }
        } else {
            checkDB.close();
        }

        // Open the database.
        packageDatabase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.OPEN_READONLY);

        this.mContext = context;
    }

    /**
     * Copy the .db file from assets/ to external storage.
     * @throws IOException Read and write .db file.
     */
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
