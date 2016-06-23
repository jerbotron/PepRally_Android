package com.peprally.jeremy.peprally.utils;

import android.support.annotation.NonNull;

public class Team implements Comparable<Team> {
    public String name;
    public int photoId;

    public Team(String name, int photoId) {
        this.name = name;
        this.photoId = photoId;
    }

    @Override
    public int compareTo(@NonNull Team another) {
        return name.compareTo(another.name);
    }
}
