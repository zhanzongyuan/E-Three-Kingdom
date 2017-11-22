package com.threegiants.e_three_kingdom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.*;

import com.yalantis.euclid.library.EuclidActivity;
import com.yalantis.euclid.library.EuclidListAdapter;
import com.yalantis.euclid.library.EuclidState;

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
    private List<Map<String, Object>> profilesList; //listView的数据
    private EuclidListAdapter mAdapter;
    private ListView listView;
    private ImageButton returnButton; //返回按钮
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialView(); //初始化listview界面
        setListViewOnClickedEvent(); //设置listview点击事件
        setFavoriteButton(); //设置收藏按钮
    }

    private void initialView() {
        listView = (ListView) findViewById(R.id.list_view);
        mEditText = (EditText) findViewById(R.id.search_text);
        returnButton = (ImageButton) findViewById(R.id.return_button);
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
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                CharactersActivity.this.setResult(R.integer.CharactersActivity_return_MainActivity, intent);
                finish();
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                listView.setFocusable(true);
                listView.setFocusableInTouchMode(true);
                listView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(listView.getWindowToken(), 0);
                return false;
            }
        });
    }

    private void setFavoriteButton() {
        mButtonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView favorite_button = (ImageView) mButtonProfile.findViewById(R.id.favorite_button);
                TextView nameView = (TextView) findViewById(R.id.text_view_profile_name);
                String name = nameView.getText().toString();
                if (ifFavorite(name)) {
                    favorite_button.setImageResource(R.drawable.favorite_border);
                    addToFavoriteOrInverse(name, false);
                } else {
                    favorite_button.setImageResource(R.drawable.favorite);
                    addToFavoriteOrInverse(name, true);
                }
            }
        });
    }

    private void addToFavoriteOrInverse(String name, boolean if_favorite) {
        // TODO: 17-11-23 张涵玮任务：根据人物姓名将收藏信息if_favorite储存
    }

    private boolean ifFavorite(String name) {
        // TODO: 17-11-23 张涵玮任务：根据人物姓名获得其收藏信息
        return false;
    }

    @Override
    protected BaseAdapter getAdapter() {
        Map<String, Object> profileMap;
        profilesList = new ArrayList<>();

        MainActivity.verifyStoragePermissions(this);
        importData();

        int[] avatars = { R.drawable.niu_jin };
        String[] names = getResources().getStringArray(R.array.array_names);

        for (int i = 0; i < characterData.size(); i++) {
            Character curCharacter = characterData.get(i);
            profileMap = new HashMap<>();
            // TODO: 17-11-19 Use Bitmap as avatar. (EuclidListAdapter.java lines 83)
            profileMap.put(EuclidListAdapter.KEY_AVATAR, avatars[0]);
            profileMap.put(EuclidListAdapter.KEY_NAME, curCharacter.getName());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, curCharacter.getShortDescription());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, curCharacter.getDescription());
            profilesList.add(profileMap);
        }
        mAdapter = new EuclidListAdapter(this, R.layout.list_item, profilesList);
        return mAdapter;
    }

    private TextView text;
    private void setListViewOnClickedEvent() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                text = (TextView)view.findViewById(R.id.text_view_name);
                AlertDialog.Builder builder = new AlertDialog.Builder(CharactersActivity.this)
                        .setTitle("移除人物")
                        .setMessage("从人物列表移除"+text.getText().toString()+"?")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAdapter.removeData(text.getText().toString());
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
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
                case 0: // STORAGE_READ_PERMISSION
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.exit(0);
                    } else {
                        storagePermissionsGranted = true;
                    }
                case 1:
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        System.exit(0);
                    }
            }
        }
    }
}
