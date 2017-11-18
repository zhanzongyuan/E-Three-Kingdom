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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.euclid.library.EuclidActivity;
import com.yalantis.euclid.library.EuclidListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zzy on 2017/11/16.
 */

public class CharactersActivity extends EuclidActivity {

    public boolean storagePermissionsGranted = false;

    //Characters Data
    List<Character> characterData;
    private EditText mEditText;
    private EuclidListAdapter mAdapter;
    private ListView listView;
    private List<Map<String, Object>> profilesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = (ListView) findViewById(R.id.list_view);
        mEditText = (EditText) findViewById(R.id.search_text);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user change the text
                mAdapter.getFilter().filter(cs.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                //
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                //
            }
        });
        setListViewOnLongClicked();

    }

    @Override
    protected BaseAdapter getAdapter() {
        Map<String, Object> profileMap;
        profilesList = new ArrayList<>();

        int[] avatars = {
                R.drawable.niu_jin};
        String[] names = getResources().getStringArray(R.array.array_names);

        for (int i = 0; i < avatars.length; i++) {
            profileMap = new HashMap<>();
            profileMap.put(EuclidListAdapter.KEY_AVATAR, avatars[i]);
            profileMap.put(EuclidListAdapter.KEY_NAME, names[i]);
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, getString(R.string.lorem_ipsum_short));
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, getString(R.string.lorem_ipsum_long));
            profilesList.add(profileMap);
        }
        mAdapter = new EuclidListAdapter(this, R.layout.list_item, profilesList);
        return mAdapter;
    }

    private void setListViewOnLongClicked() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView text = (TextView)view.findViewById(R.id.text_view_name);
                mAdapter.removeData(text.getText().toString());
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
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
            int id = mCursor.getInt(0);
            String name = mCursor.getString(1);
            String gender = mCursor.getString(2);
            String birth = mCursor.getString(3);
            String homeTown = mCursor.getString(4);
            String camp = mCursor.getString(5);
            String shortDescription = mCursor.getString(6);
            String description = mCursor.getString(7);
            byte[] iconArr = mCursor.getBlob(8);
            Bitmap icon = BitmapFactory.decodeByteArray(iconArr, 0, iconArr.length);
            Character curCharacter = new Character(
                    id, name, gender, birth, homeTown, camp, shortDescription, description, icon
            );
            characterData.add(curCharacter);
        }

        mCursor.close();
        db.close();
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
