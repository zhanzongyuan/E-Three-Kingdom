package com.threegiants.e_three_kingdom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.*;

import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
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

    // DataBaseHelper instance.
    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialView(); //初始化listview界面
        setListViewOnClickedEvent(); //设置listview点击事件
        setFavoriteButton(); //设置收藏按钮
        setAddNoteButton();
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

    /**
     * Set click to add note button
     */
    private void setAddNoteButton() {
        ImageView addNote = (ImageView) findViewById(R.id.add_note_button);
        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNoteEditDialog(v);
                dialog.show();
            }
        });
    }

    /**
     * Make dialog to edit note
     * @param v
     */
    private void setNoteEditDialog(View v) {
        String originalName = mTextViewProfileName.getText().toString();
        int index = 0;
        for (int i = 0; i < characterData.size(); i++)
            if (characterData.get(i).getName().equals(originalName)) {
                index = i;
                break;
            }
        final int finalObjectIndex = index;

        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        View contview = factory.inflate(R.layout.edit_description_dialog, null);

        TextView dialogTitle = (TextView) contview.findViewById(R.id.dialog_title);
        dialogTitle.setText("笔记：");

        //Get editDialog editText
        final EditText editNote = (EditText) contview.findViewById(R.id.edit_description);
        editNote.setText(characterData.get(finalObjectIndex).getNote());

        Button btOK = (Button) contview.findViewById(R.id.edit_ok);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                characterData.get(finalObjectIndex).setNote(editNote.getText().toString());
                Toast.makeText(CharactersActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });
        dialog=new AlertDialog.Builder(CharactersActivity.this).setView(contview).create();
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

    /**
     * Add or remove a character from favorite.
     * @param name The name of the character.
     * @param if_favorite true to add, false to remove.
     */
    private void addToFavoriteOrInverse(String name, boolean if_favorite) {
        SQLiteDatabase db =  dataBaseHelper.getPackageDatabase();

        if (if_favorite) {
            db.execSQL(
                    "UPDATE characters " +
                    "SET isFavourite = 1 " +
                    "WHERE name = '" + name + "';"
            );
        } else {
            db.execSQL(
                    "UPDATE characters " +
                    "SET isFavourite = 0 " +
                    "WHERE name = '" + name + "';"
            );
        }
    }

    /**
     * Check if a character is favourite.
     * @param name The name of the character.
     * @return isFavourite.
     */
    private boolean ifFavorite(String name) {
        SQLiteDatabase db = dataBaseHelper.getPackageDatabase();

        // Check if the character is favourite.
        Cursor c = db.rawQuery(
                "SELECT isFavourite FROM characters WHERE name = '" + name + "';",
                null
        );
        c.moveToNext();
        boolean charFavourite = c.getInt(0) == 1;
        c.close();
        return charFavourite;
    }

    @Override
    protected void onStop() {
        super.onStop();
        syncCharacters();
    }

    @Override
    protected void initList() {
        mListViewAnimationAdapter = new SwingLeftInAnimationAdapter(getAdapter());
        mListViewAnimationAdapter.setAbsListView(mListView);
        mListViewAnimator = mListViewAnimationAdapter.getViewAnimator();
        if (mListViewAnimator != null) {
            mListViewAnimator.setAnimationDurationMillis(getAnimationDurationCloseProfileDetails());
            mListViewAnimator.disableAnimations();
        }
        mListView.setAdapter(mListViewAnimationAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mState = EuclidState.Opening;
                choosed_id = id;
                showProfileDetails((Map<String, Object>) parent.getItemAtPosition(position), view);
                Toast.makeText(CharactersActivity.this, "长按内容可纠错", Toast.LENGTH_SHORT).show();

                // Initialize star.
                ImageView favorite_button = (ImageView) mButtonProfile.findViewById(com.yalantis.euclid.library.R.id.favorite_button);
                if (ifFavorite(mTextViewProfileName.getText().toString())) {
                    favorite_button.setImageResource(R.drawable.favorite);
                } else {
                    favorite_button.setImageResource(R.drawable.favorite_border);
                }
            }
        });
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
                                removeDataFromDB(text.getText().toString());
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        //Set click and long-click edit windows for base info
        final View baseInfoText = findViewById(R.id.first_edit_block);
        baseInfoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CharactersActivity.this, "长按内容可纠错", Toast.LENGTH_SHORT).show();
            }
        });
        baseInfoText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setBaseInfoEditDialog(v);
                dialog.show();
                return false;
            }
        });

        //Set click and long-click edit windows for description
        View descriptionText = findViewById(R.id.text_view_profile_description);
        descriptionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CharactersActivity.this, "长按内容可纠错", Toast.LENGTH_SHORT).show();
            }
        });
        descriptionText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setDescriptionEditDialog(v);
                dialog.show();
                return false;
            }
        });
    }

    /**
     * Make dialog to edit character base info
     * @param v
     */
    private void setBaseInfoEditDialog(View v) {
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        View contview = factory.inflate(R.layout.edit_base_info_dialog, null);

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
                        String name = editName.getText().toString();
                        String gender = editGender.getText().toString();
                        String birth = editBirth.getText().toString();
                        String hometown = editHometown.getText().toString();
                        String camp = editCamp.getText().toString();

                        characterData.get(i).setName(name);
                        characterData.get(i).setGender(gender);
                        characterData.get(i).setBirth(birth);
                        characterData.get(i).setHomeTown(hometown);
                        characterData.get(i).setCamp(camp);
                        //Refresh detail page
                        mTextViewProfileName.setText(name);
                        mTextViewProfileGender.setText(gender);
                        mTextViewProfileBirth.setText(birth);
                        mTextViewProfileHometown.setText(hometown);
                        mTextViewProfileCamp.setText(camp);

                        Map<String, Object> editedMap = getCharacterMapFormat(characterData.get(i));
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
     * Make dialog to edit character description
     * @param v
     */
    private void setDescriptionEditDialog(View v) {
        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        View contview = factory.inflate(R.layout.edit_description_dialog, null);

        //Get editDialog editText
        final EditText editDescription = (EditText) contview.findViewById(R.id.edit_description);

        editDescription.setText(mTextViewProfileDescription.getText());

        Button btOK = (Button) contview.findViewById(R.id.edit_ok);
        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change character list
                //Update mAdapter

                String originalName = mTextViewProfileName.getText().toString();
                for (int i = 0; i < characterData.size(); i++) {
                    if (characterData.get(i).getName().equals(originalName)) {
                        String description = editDescription.getText().toString();

                        characterData.get(i).setDescription(description);
                        //Refresh detail page
                        mTextViewProfileDescription.setText(description);

                        Map<String, Object> editedMap = getCharacterMapFormat(characterData.get(i));
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
        db.execSQL("DELETE FROM characters WHERE name = '" + name + "';");
    }

    /**
     * Import data from db
     */
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
            String note = mCursor.getString(9);
            Bitmap icon = BitmapFactory.decodeByteArray(iconArr, 0, iconArr.length);
            Character curCharacter = new Character(
                    id, name, gender, birth, homeTown, camp, shortDescription, description, icon,
                    note
            );
            characterData.add(curCharacter);
        }

        mCursor.close();
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
            profileMap.put(EuclidListAdapter.KEY_AVATAR, curCharacter.getIcon());
            profileMap.put(EuclidListAdapter.KEY_NAME, curCharacter.getName());
            profileMap.put(EuclidListAdapter.KEY_GENDER, curCharacter.getGender());
            profileMap.put(EuclidListAdapter.KEY_BIRTH, curCharacter.getBirth());
            profileMap.put(EuclidListAdapter.KEY_HOMETOWN, curCharacter.getHomeTown());
            profileMap.put(EuclidListAdapter.KEY_CAMP, curCharacter.getCamp());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, curCharacter.getShortDescription());
            profileMap.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, curCharacter.getDescription());
            profileMap = getCharacterMapFormat(curCharacter);
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

    private Map<String, Object> getCharacterMapFormat(Character character) {
        Map<String, Object> map = new HashMap<>();
        map.put(EuclidListAdapter.KEY_AVATAR, character.getIcon());
        map.put(EuclidListAdapter.KEY_NAME, character.getName());
        map.put(EuclidListAdapter.KEY_GENDER, character.getGender());
        map.put(EuclidListAdapter.KEY_BIRTH, character.getBirth());
        map.put(EuclidListAdapter.KEY_HOMETOWN, character.getHomeTown());
        map.put(EuclidListAdapter.KEY_CAMP, character.getCamp());
        map.put(EuclidListAdapter.KEY_DESCRIPTION_SHORT, character.getShortDescription());
        map.put(EuclidListAdapter.KEY_DESCRIPTION_FULL, character.getDescription());
        return map;

    }

    /**
     * Rewrite the data to the database
     */
    private void syncCharacters() {
        SQLiteDatabase db = dataBaseHelper.getPackageDatabase();
        for (int i = 0; i < characterData.size(); i++) {
            Character buf = characterData.get(i);
            db.execSQL(
                    "UPDATE characters " +
                    "SET name ='" + buf.getName() + "', " +
                        "gender = '" + buf.getGender() + "', " +
                        "birth = '" + buf.getBirth() + "', " +
                        "homeTown = '" + buf.getHomeTown() + "', " +
                        "camp = '" + buf.getCamp() + "', " +
                        "description = '" + buf.getDescription() + "', " +
                        "note = '" + buf.getNote() + "' " +
                    "WHERE _id = " + buf.getId() + ";"
            );
        }
    }
}
