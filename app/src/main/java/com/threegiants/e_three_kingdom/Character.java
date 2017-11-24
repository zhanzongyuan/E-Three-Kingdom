package com.threegiants.e_three_kingdom;

import android.graphics.Bitmap;
import android.text.BoringLayout;

/**
 * Class Character
 * Keep down a single character's information.
 */

public class Character {

    private int id;
    private String name;
    private String gender;
    private String birth;
    private String homeTown;
    private String camp;
    private String shortDescription;
    private String description;
    private Bitmap icon;
    private String note;

    Character(
        int i, String n, String g, String b, String ht, String c, String sd, String d, Bitmap ic,
        String no
    ) {
        id = i;
        name = n;
        gender = g;
        birth = b;
        homeTown = ht;
        camp = c;
        shortDescription = sd;
        description = d;
        icon = ic;
        note = no;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getBirth() { return birth; }
    public String getHomeTown() { return homeTown; }
    public String getCamp() { return camp; }
    public String getShortDescription() { return shortDescription; }
    public String getDescription() { return description; }
    public Bitmap getIcon() { return icon; }
    public String getNote() { return note; }

    public void setId(int i) { id = i; }
    public void setName(String n){ name = n; }
    public void setGender(String g) { gender = g; }
    public void setBirth(String b) { birth = b; }
    public void setHomeTown(String ht) { homeTown = ht; }
    public void setCamp(String c) { camp = c; }
    public void setShortDescription(String sd) { shortDescription = sd; }
    public void setDescription(String d) { description = d; }
    public void setIcon(Bitmap ic){ icon = ic; }
    public void setNote(String no) { note = no; }
}
