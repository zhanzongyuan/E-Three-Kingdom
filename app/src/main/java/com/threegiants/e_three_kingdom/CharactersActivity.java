package com.threegiants.e_three_kingdom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
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

    // DataBaseHelper instance.
    private DataBaseHelper dataBaseHelper;

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
                // TODO: 17-11-25 点击收藏按钮有bug，点击闪退，可能是数据库的问题 
                if (ifFavorite(name)) {
                    favorite_button.setImageResource(R.drawable.favorite_border);
                    addToFavoriteOrInverse(name, false);
                    Toast.makeText(CharactersActivity.this, "长按内容可纠错", Toast.LENGTH_SHORT).show();
                } else {
                    favorite_button.setImageResource(R.drawable.favorite);
                    addToFavoriteOrInverse(name, true);
                    Toast.makeText(CharactersActivity.this, "长按内容可纠错", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Add or remove a character from favorite.
     * @param name The name of the character.
     * @param if_favorite true to add, false to remove.
     */
    private void addToFavoriteOrInverse(String name, boolean if_favorite) {
        SQLiteDatabase db =  dataBaseHelper.getPackageDatabase();

        if (if_favorite) {
            db.execSQL(
                    "UPDATE characters" +
                    "SET isFavorite = 1" +
                    "WHERE name = " + name + ";"
            );
        } else {
            db.execSQL(
                    "UPDATE characters" +
                    "SET isFavorite = 0" +
                    "WHERE name = " + name + ";"
            );
        }
        db.close();
    }

    /**
     * Check if a character is favourite.
     * @param name The name of the character.
     * @return isFavourite.
     */
    private boolean ifFavorite(String name) {
        SQLiteDatabase db = dataBaseHelper.getPackageDatabase();

        // Check if the character is in the table.
        Cursor c = db.rawQuery(
                "SELECT isFavourite FROM characters WHERE name = " + name + " ;",
                null
        );
        boolean charExist = c.getInt(0) == 1;
        c.close();
        db.close();
        return charExist;
    }

    private TextView text;
    private Dialog dialog;
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
                                // TODO: 17-11-25 数据删除有bug，点击删除后闪退，应该是数据库的问题，在内存测试时没问题 
                                removeDataFromDB(text.getText().toString());
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        //Set long-click edit windows
        final View nameText = findViewById(R.id.first_edit_block);
        nameText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setEditDialog(v);
                dialog.show();
                return false;
            }
        });
    }
    private void setEditDialog(View v){
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        // 引入一个外部布局
        View contview = factory.inflate(R.layout.edit_dialog_layout, null);

        //Get editDialog editText
        final EditText editName = (EditText) contview.findViewById(R.id.edit_name);
        final EditText editGender = (EditText) contview.findViewById(R.id.edit_gender);
        final EditText editBirth = (EditText) contview.findViewById(R.id.edit_birth);
        final EditText editHometown = (EditText) contview.findViewById(R.id.edit_hometown);
        final EditText editCamp = (EditText) contview.findViewById(R.id.edit_camp);

        editName.setText(mTextViewProfileName.getText());
        editGender.setText(mTextViewProfileGender.getText());
        editBirth.setText(mTextViewProfileBirth.getText());
        editHometown.setText(mTextViewProfileHometown.getText());
        editCamp.setText(mTextViewProfileCamp.getText());

        Button btOK = (Button) contview.findViewById(R.id.edit_ok);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change character list
                //Update mAdapter

                String originalName = mTextViewProfileName.getText().toString();
                for (int i = 0; i < characterData.size(); i++) {
                    if (characterData.get(i).getName().equals(originalName)) {
                        Map<String, Object> editedMap = new HashMap<>();

                        String name = editName.getText().toString();
                        String gender = editGender.getText().toString();
                        String birth = editBirth.getText().toString();
                        String hometown = editHometown.getText().toString();
                        String camp = editCamp.getText().toString();

                        editedMap.put(EuclidListAdapter.KEY_NAME, name);
                        editedMap.put(EuclidListAdapter.KEY_GENDER, gender);
                        editedMap.put(EuclidListAdapter.KEY_BIRTH, birth);
                        editedMap.put(EuclidListAdapter.KEY_HOMETOWN, hometown);
                        editedMap.put(EuclidListAdapter.KEY_CAMP, camp);
                        editedMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, characterData.get(i).getDescription());
                        editedMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, characterData.get(i).getShortDescription());
                        editedMap.put(EuclidListAdapter.KEY_AVATAR, characterData.get(i).getIcon());

                        mTextViewProfileName.setText(name);
                        mTextViewProfileGender.setText(gender);
                        mTextViewProfileBirth.setText(birth);
                        mTextViewProfileHometown.setText(hometown);
                        mTextViewProfileCamp.setText(camp);

                        mAdapter.editData(i, editedMap);
                        mAdapter.notifyDataSetChanged();

                        break;
                    }
                }
                //Update Database


                Toast.makeText(CharactersActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        dialog=new AlertDialog.Builder(CharactersActivity.this).setView(contview).create();
    }


    /**
     * Permanently remove the character from the database.
     * @param name The name of the character needs to be removed.
     */
    private void removeDataFromDB(String name) {
        SQLiteDatabase db = dataBaseHelper.getPackageDatabase();
        db.execSQL("DELETE FROM characters WHERE name = " + name + ";");
        db.close();
    }

    // Import data from db
    protected void importData(){
        characterData=new ArrayList<>();

        // Open our database.
        dataBaseHelper = new DataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getPackageDatabase();

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
            String note = mCursor.getString(10);
            Bitmap icon = BitmapFactory.decodeByteArray(iconArr, 0, iconArr.length);
            Character curCharacter = new Character(
                    id, name, gender, birth, homeTown, camp, shortDescription, description, icon,
                    note
            );
            characterData.add(curCharacter);
        }

        mCursor.close();
        db.close();
    }

    /**
     * The function gets data by function importData()
     * @return The adapter for this activity to create listview
     */
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
            // TODO: 17-11-25 张涵玮任务：人物图片显示位置不对，打开详情有些人物的头部被遮盖，请调整图片 
            profileMap.put(EuclidListAdapter.KEY_AVATAR, curCharacter.getIcon());
            profileMap.put(EuclidListAdapter.KEY_NAME, curCharacter.getName());
            profileMap.put(EuclidListAdapter.KEY_GENDER, curCharacter.getGender());
            profileMap.put(EuclidListAdapter.KEY_BIRTH, curCharacter.getBirth());
            profileMap.put(EuclidListAdapter.KEY_HOMETOWN, curCharacter.getHomeTown());
            profileMap.put(EuclidListAdapter.KEY_CAMP, curCharacter.getCamp());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, curCharacter.getShortDescription());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, curCharacter.getDescription());
            profilesList.add(profileMap);
        }
        mAdapter = new EuclidListAdapter(this, R.layout.list_item, profilesList);
        return mAdapter;
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
