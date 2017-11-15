package com.threegiants.e_three_kingdom;

import android.graphics.Bitmap;

/**
 * Created by applecz on 2017/11/4.
 */

public class Character {
    private String name;
    private Bitmap head;
    private String short_detail;
    public Character(String n, Bitmap h, String sd) {
        name = n;
        head = h;
        short_detail = sd;
    }
    public String getName(){
        return name;
    }
    public Bitmap getHead(){
        return head;
    }
    public String getShortDetail(){
        return short_detail;
    }
}
