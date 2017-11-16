package com.threegiants.e_three_kingdom;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzy on 2017/11/16.
 */

public class CardDataListActivity extends AppCompatActivity {
    //Characters Data
    List<Character> characterData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    //import data from db
    protected void importData(){
        characterData=new ArrayList<>(); //Warn: here must use new ArrayList<>()

        // Open our database.
        DataBaseHelper mHelper = new DataBaseHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();

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

}
