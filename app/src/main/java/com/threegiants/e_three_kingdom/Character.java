package com.threegiants.e_three_kingdom;

import android.graphics.Bitmap;

/**
 * Created by applecz on 2017/11/4.
 */

public class Character {
    //id、name、icon、gender、birth、hometown、camp
    //id is unique
    private String id;
    private String name;
    private Bitmap icon;
    private String gender;
    private String birth;
    private String home_town;
    private String camp;
    private String description;
    public Character(String i, String n, Bitmap ic, String g, String b, String ht, String c, String d) {
        id=i;
        name = n;
        icon = ic;
        gender=g;
        home_town=ht;
        camp=c;
        description=d;
    }
    public String getId() {return id;}
    public String getName(){
        return name;
    }
    public Bitmap getIcon(){
        return icon;
    }
    public String getGender(){
        return gender;
    }
    public String getBirth(){
        return birth;
    }
    public String getHomeTown(){
        return home_town;
    }
    public String getCamp(){
        return camp;
    }
    public String getDescription() {return description;}
    public void setName(String n){name=n;}
    public void setIcon(Bitmap ic){
        icon=ic;
    }
    public void setGender(String g){
        gender=g;
    }
    public void setBirth(String b){
        birth=b;
    }
    public void setHomeTown(String ht){
        home_town=ht;
    }
    public void setCamp(String c){
        camp=c;
    }
    public void setDescription(String d){description=d;}
}
